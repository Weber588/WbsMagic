package wbs.magic.spells;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.magic.SpellCaster;
import wbs.magic.WbsMagic;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spells.framework.CastingContext;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMath;
import wbs.utils.util.pluginhooks.WbsRegionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Spell(name = "Throw Block",
        cost = 15,
        cooldown = 3,
        description = "Throws a block, as configured in each spell."
)
@SpellOption(optionName = "material", type = SpellOptionType.STRING, defaultString = "PURPLE_STAINED_GLASS", enumType = Material.class)
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 1.5)
@SpellOption(optionName = "stick-to-walls", type = SpellOptionType.BOOLEAN, defaultBool = false)
@SpellOption(optionName = "amount", type = SpellOptionType.INT)
@SpellOption(optionName = "delay", type = SpellOptionType.DOUBLE, defaultDouble = 0.25)
@SpellOption(optionName = "spread", type = SpellOptionType.DOUBLE, defaultDouble = 0)
@SpellOption(optionName = "drop-item-on-fail", type = SpellOptionType.BOOLEAN, defaultBool = false)
@SpellOption(optionName = "fluid-collision", type = SpellOptionType.STRING, defaultString = "SOURCE_ONLY", enumType = FluidCollisionMode.class)
public class ThrowBlock extends SpellInstance {

    public static final NamespacedKey THROWN_BY_KEY = new NamespacedKey(WbsMagic.getInstance(), "thrown_by");
    public static final NamespacedKey STICK_TO_WALLS_KEY = new NamespacedKey(WbsMagic.getInstance(), "stick_to_walls");

    public ThrowBlock(SpellConfig config, String directory) {
        super(config, directory);

        String materialString = config.getString("material");
        Material checkMaterial = WbsEnums.getEnumFromString(Material.class, materialString);

        if (checkMaterial == null) {
            logError("Invalid material: " + materialString, directory + "/material");
            material = Material.PURPLE_STAINED_GLASS;
        } else {
            material = checkMaterial;
        }

        if (!material.isBlock()) {
            throw new InvalidConfigurationException("Material must be a block type. Invalid material: " + WbsEnums.toPrettyString(material));
        }

        speed = config.getDouble("speed");
        stickToWalls = config.getBoolean("stick-to-walls");
        dropItemOnFail = config.getBoolean("drop-item-on-fail");
        spread = config.getDouble("spread");
        amount = config.getInt("amount");
        delay = (int) (config.getDouble("delay") * 20);

        String fluidCollisionString = config.getString("fluid-collision");
        FluidCollisionMode checkFluidCollision = WbsEnums.getEnumFromString(FluidCollisionMode.class, fluidCollisionString);

        if (checkFluidCollision == null) {
            logError("Invalid fluid collision mode: " + fluidCollisionString +
                    ". Valid options: " + WbsEnums.toStringList(FluidCollisionMode.class), directory + "/fluid-collision");
            fluidCollisionMode = FluidCollisionMode.NEVER;
        } else {
            fluidCollisionMode = checkFluidCollision;
        }
    }

    private final Material material;
    private final double speed;
    private final boolean stickToWalls;
    private final boolean dropItemOnFail;

    private final double spread;
    private final int amount;
    private final int delay;

    private final FluidCollisionMode fluidCollisionMode;


    @Override
    public boolean cast(CastingContext context) {
        SpellCaster caster = context.caster;
        if (delay <= 0) {
            for (int i = 0; i < amount; i++) {
                throwBlock(
                        caster,
                        caster.getFacingVector(speed)
                                .add(WbsMath.randomVector(spread))
                                .normalize()
                                .multiply(speed)
                );
            }
        } else {
            new BukkitRunnable() {
                int amountSoFar = 0;

                @Override
                public void run() {
                    throwBlock(
                            caster,
                            caster.getFacingVector(speed)
                                    .add(WbsMath.randomVector(spread))
                                    .normalize()
                                    .multiply(speed)
                    );

                    amountSoFar++;

                    if (amountSoFar >= amount) {
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0, delay);
        }
        return true;
    }

    private void throwBlock(SpellCaster caster, Vector direction) {
        World world = Objects.requireNonNull(caster.getLocation().getWorld());

        BlockData initialData =
                adjustDataByDirection(
                        material.createBlockData(),
                        getFaceFromDirection(caster.getFacingVector())
                );

        FallingBlock block = world.spawnFallingBlock(caster.getEyeLocation(), initialData);
        block.setVelocity(direction);
        block.setDropItem(dropItemOnFail);
        block.setHurtEntities(false);

        PersistentDataContainer container = block.getPersistentDataContainer();
        container.set(THROWN_BY_KEY, PersistentDataType.STRING, caster.getName());
        container.set(STICK_TO_WALLS_KEY, PersistentDataType.STRING, stickToWalls + "");

        MagicEntityEffect marker = new MagicEntityEffect(block, caster, this);

        marker.run();

        new BukkitRunnable() {

            @Override
            public void run() {
                if (marker.isExpired()) {
                    cancel();
                    block.remove();
                }

                if (stickToWalls) {
                    Vector velocity = block.getVelocity();
                    double distanceCheck = 2;
                    velocity.normalize().multiply(distanceCheck);

                    RayTraceResult result;
                    try {
                        result = world.rayTraceBlocks(block.getLocation(), velocity, distanceCheck, fluidCollisionMode, true);
                    } catch (IllegalArgumentException e) {
                        return;
                    }

                    if (result == null) return;

                    BlockFace face = Objects.requireNonNull(result.getHitBlockFace());
                    Block currentBlock =
                            result.getHitPosition().toLocation(world)
                                    .add(face.getDirection().normalize().multiply(0.05))
                                    .getBlock();

                    if (currentBlock.getType().isAir()) {
                        block.remove();
                        cancel();

                        if (!WbsRegionUtils.canBuildAt(currentBlock.getLocation(), caster.getPlayer())) {
                            caster.sendActionBar("&wCan't build there!");
                            if (dropItemOnFail)
                                world.dropItem(block.getLocation(), new ItemStack(material, 1));
                            return;
                        }

                        Material updatedMaterial = material;

                        switch (updatedMaterial) {
                            case TORCH:
                                if (face.getDirection().getY() == 0) {
                                    updatedMaterial = Material.WALL_TORCH;
                                }
                                break;
                            case REDSTONE_TORCH:
                                if (face.getDirection().getY() == 0) {
                                    updatedMaterial = Material.REDSTONE_WALL_TORCH;
                                }
                                break;
                        }

                        currentBlock.setType(updatedMaterial);

                        currentBlock.setBlockData(
                                adjustDataByDirection(currentBlock.getBlockData(), face)
                        );

                        switch (updatedMaterial) {
                            case WALL_TORCH:
                            case REDSTONE_WALL_TORCH:
                                Directional directional = (Directional) currentBlock.getBlockData();

                                BlockFace finalFacing = directional.getFacing();
                                Vector toWallBlock = finalFacing.getDirection();

                                Block wallBlock = currentBlock.getLocation().subtract(toWallBlock).getBlock();
                                if (wallBlock.getType().isAir()) {
                                    if (dropItemOnFail) {
                                        currentBlock.breakNaturally();
                                    } else {
                                        currentBlock.setType(Material.AIR);
                                    }
                                }
                                break;
                            case TORCH:
                            case REDSTONE_TORCH:
                                if (currentBlock.getLocation().subtract(0, 1, 0).getBlock().getType().isAir()) {
                                    if (dropItemOnFail) {
                                        currentBlock.breakNaturally();
                                    } else {
                                        currentBlock.setType(Material.AIR);
                                    }
                                }
                                break;
                        }
                    }
                }

            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private BlockData adjustDataByDirection(BlockData data, BlockFace face) {
        try {
            if (data instanceof Directional) {
                Directional directional = (Directional) data;

                directional.setFacing(getAxisFaceFromDirection(face.getDirection()));
            }

            if (data instanceof Rotatable) {
                Rotatable rotatable = (Rotatable) data;

                rotatable.setRotation(face);
            }

            if (data instanceof Orientable) {
                Orientable orientable = (Orientable) data;

                orientable.setAxis(axisFromBlockFace(face));
            }
        } catch (IllegalArgumentException ignored) {} // Some blocks can't face certain directions

        return data;
    }

    @NotNull
    private BlockFace getFaceFromDirection(Vector direction) {
        direction = direction.clone().normalize();

        BlockFace closest = BlockFace.UP;
        double distance = Double.MAX_VALUE;
        for (BlockFace check : BlockFace.values()) {
            double checkDistance = check.getDirection().distanceSquared(direction);
            if (checkDistance < distance) {
                closest = check;
                distance = checkDistance;
            }
        }

        return closest;
    }

    private final static List<BlockFace> axisFaces =
            Arrays.asList(
                    BlockFace.UP,
                    BlockFace.DOWN,
                    BlockFace.NORTH,
                    BlockFace.EAST,
                    BlockFace.SOUTH,
                    BlockFace.WEST
            );

    @NotNull
    private BlockFace getAxisFaceFromDirection(Vector direction) {
        direction = direction.clone().normalize();

        BlockFace closest = BlockFace.UP;
        double distance = Double.MAX_VALUE;
        for (BlockFace check : axisFaces) {
            double checkDistance = check.getDirection().distanceSquared(direction);
            if (checkDistance < distance) {
                closest = check;
                distance = checkDistance;
            }
        }

        return closest;
    }

    private Axis axisFromBlockFace(BlockFace face) {
        Vector direction = face.getDirection();

        if (direction.getX() != 0) {
            return Axis.X;
        } else if (direction.getY() != 0) {
            return Axis.Y;
        } else {
            return Axis.Z;
        }
    }

    @Override
    public String toString() {
        String asString = super.toString();

        asString += "\n&rSpeed: &7" + speed;
        asString += "\n&rMaterial: &7" + WbsEnums.toPrettyString(material);
        asString += "\n&rStick to walls? &7" + stickToWalls;

        asString += "\n&rAmount: &7" + amount;
        if (amount > 1) {
            asString += "\n&rDelay: &7" + delay;
        }
        asString += "\n&rSpread: &7" + spread;
        asString += "\n&rFluid Collision Mode: &7" + fluidCollisionMode;
        asString += "\n&rDrop item on fail? &7" + dropItemOnFail;

        return asString;
    }
}
