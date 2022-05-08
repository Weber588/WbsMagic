package wbs.magic.spells.ranged;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.objects.MagicSpawnedBlock;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.FailableSpell;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellSettings;
import wbs.magic.SpellCaster;
import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.pluginhooks.WbsRegionUtils;

import java.util.LinkedList;
import java.util.List;

@Spell(name = "Conjure Bridge",
        cost = 50,
        cooldown = 90,
        description = "Creates a bridge in front of the caster to walk across."
)
@SpellSettings(canBeConcentration = true, concentrationByDefault = false)
@FailableSpell("If there are no spots for a bridge, " +
        "the player is not on the ground, " +
        "or the player can't build there, the spell will fail.")
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 5)
@SpellOption(optionName = "blocks-per-second", type = SpellOptionType.DOUBLE, defaultDouble = 20)
@SpellOption(optionName = "width", type = SpellOptionType.DOUBLE, defaultDouble = 3)
@SpellOption(optionName = "max-slope", type = SpellOptionType.DOUBLE, defaultDouble = 0.5)
@SpellOption(optionName = "temporary", type = SpellOptionType.BOOLEAN, defaultBool = true)
@SpellOption(optionName = "do-sounds", type = SpellOptionType.BOOLEAN, defaultBool = true)
@SpellOption(optionName = "use-line-of-sight", type = SpellOptionType.BOOLEAN, defaultBool = false)
@SpellOption(optionName = "material", type = SpellOptionType.STRING, defaultString = "PURPLE_STAINED_GLASS", enumType = Material.class)
@SpellOption(optionName = "max-blocks", type = SpellOptionType.INT, defaultInt = Integer.MAX_VALUE)

@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 10, aliases = {"distance", "max-distance"})
public class ConjureBridge extends RangedSpell {

    public ConjureBridge(SpellConfig config, String directory) {
        super(config, directory);

        maxBlocks = config.getInt("max-blocks");

        // Keeping blocksPerTick as double, despite being in ticks to allow smearing
        blocksPerTick = config.getDouble("blocks-per-second") / 20;

        width = Math.max(0, config.getDouble("width") - 1);
        maxSlope = config.getDouble("max-slope");

        temporary = config.getBoolean("temporary");

        // If not temporary, force duration to be unlimited
        if (temporary) {
            duration = (int) (config.getDouble("duration") * 20);
        } else {
            duration = -1;
        }

        doSounds = config.getBoolean("do-sounds");
        useLineOfSight = config.getBoolean("use-line-of-sight");

        String materialString = config.getString("material");
        material = WbsEnums.materialFromString(materialString);

        if (material == null) {
            logError("Invalid material: " + materialString, directory + "/material");
            material = Material.PURPLE_STAINED_GLASS;
        }

    }

    private final int maxBlocks;
    private final int duration;
    private final double blocksPerTick;
    private final double width;
    private final double maxSlope;
    private final boolean temporary;
    private final boolean doSounds;
    private final boolean useLineOfSight;
    private Material material;

    @Override
    public boolean cast(CastingContext context) {
        SpellCaster caster = context.caster;
        Player player = caster.getPlayer();
        if (!player.isOnGround()) {
            caster.sendActionBar("You must be on the ground!");
            return false;
        }

        Block initial = player.getLocation().subtract(0, 0.5, 0).getBlock();

        List<Block> bridgeLocations = populateBlocks(player, initial);

        if (bridgeLocations == null) {
            return false;
        }

        if (bridgeLocations.isEmpty()) {
            caster.sendActionBar("No valid spots!");
            return false;
        }

        if (blocksPerTick <= 0) {
            for (Block block : bridgeLocations) {
                spawnBlock(block, caster);
            }
        } else {
            int localBlocksPerTick = (int) blocksPerTick;
            double errorStep = blocksPerTick - localBlocksPerTick;

            if (isConcentration) {
                caster.setConcentration(this);
            }

            new BukkitRunnable() {
                int i = 0;
                double error = 0;

                @Override
                public void run() {
                    if (isConcentration) {
                        if (!caster.isConcentratingOn(ConjureBridge.this)) {
                            cancel();
                            return;
                        }
                    }

                    error += errorStep;

                    int blocksThisTick = localBlocksPerTick;
                    if (error >= 1) {
                        blocksThisTick+= (int) error;
                        error -= (int) error;
                    }

                    for (int j = 0; j < blocksThisTick; j++) {
                        if (i >= bridgeLocations.size()) {
                            cancel();
                            if (isConcentration) caster.stopConcentration();
                            return;
                        }

                        Block block = bridgeLocations.get(i);
                        spawnBlock(block, caster);

                        i++;
                    }
                }
            }.runTaskTimer(plugin, 0, 1);
        }

        return true;
    }

    private void spawnBlock(Block block, SpellCaster caster) {
        block.setType(material);
        if (doSounds) {
            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1);
        }

        if (duration <= 0 && !temporary) {
            return;
        }

        MagicSpawnedBlock magicBlock = new MagicSpawnedBlock(block, caster, this);

        if (duration >= 0) {
            magicBlock.setDuration(duration);
        }
        magicBlock.setRemoveBlockOnExpire(temporary);
        magicBlock.setExpireOnMaterialChange(true);
        magicBlock.setPersistent(!temporary);

        magicBlock.run();
    }

    private LinkedList<Block> populateBlocks(Player player, Block initial) {
        LinkedList<Block> blocks = new LinkedList<>();
        Location initialLoc = initial.getLocation().add(0.5, 0.5, 0.5);

        Vector facing = WbsEntityUtil.getFacingVector(player);

        Vector direction = WbsMath.limitToSlope(facing, maxSlope);
        direction.normalize().multiply(0.5);

        Vector facingFlat = facing.clone().setY(0).normalize();

        Vector perp;
        if (direction.angle(facingFlat) == 0) {
            perp = direction.clone().crossProduct(new Vector(0, 1, 0));
        } else {
            perp = facingFlat.clone().crossProduct(direction);
        }

        perp.normalize().multiply(0.5);
        Vector negativePerp = perp.clone().multiply(-1);

        Location currentLocation = initialLoc.clone();

        double localDistance = range;

        World world = currentLocation.getWorld();
        assert world != null;

        RayTraceResult rayCast = world.rayTraceBlocks(player.getEyeLocation(), direction, localDistance);
        if (rayCast != null) {
            Location hitPos = rayCast.getHitPosition().toLocation(world);
            localDistance = currentLocation.distance(hitPos);

            direction = hitPos.toVector().subtract(initialLoc.toVector());
            direction = WbsMath.limitToSlope(direction, maxSlope);
            direction.normalize().multiply(0.5);
        } else {
            if (useLineOfSight) {
                sendActionBar("You must have line of sight to a block!", player);
                return null;
            }
        }

        if (localDistance > range) localDistance = range;

        int blocksPlaced = 0;

        while (currentLocation.distanceSquared(initialLoc) <= localDistance * localDistance) {
            if (blocksPlaced >= maxBlocks) break;

            currentLocation.add(direction);

            Block currentBlock = currentLocation.getBlock();
            if (!blocks.contains(currentLocation.getBlock()) && validSpot(player, currentBlock)) {
                blocks.add(currentBlock);
                blocksPlaced++;
            }

            for (Block block : getBlocksOnPath(perp, width / 2, currentLocation)) {
                if (!blocks.contains(block) && blocksPlaced < maxBlocks && validSpot(player, block)) {
                    blocks.add(block);
                    blocksPlaced++;
                }
            }
            for (Block block : getBlocksOnPath(negativePerp, width / 2, currentLocation)) {
                if (!blocks.contains(block) && blocksPlaced < maxBlocks && validSpot(player, block)) {
                    blocks.add(block);
                    blocksPlaced++;
                }
            }
        }

        return blocks;
    }

    private List<Block> getBlocksOnPath(Vector direction, double distance, Location initialLoc) {
        List<Block> blocks = new LinkedList<>();

        Location currentLocation = initialLoc.clone().add(direction);
        while (currentLocation.distanceSquared(initialLoc) <= distance * distance) {
            if (!blocks.contains(currentLocation.getBlock())) {
                blocks.add(currentLocation.getBlock());
            }

            currentLocation.add(direction);
        }

        return blocks;
    }


    private boolean validSpot(Player player, Block check) {
        return (check.getType().isAir() && WbsRegionUtils.canBuildAt(check.getLocation(), player));
    }

    @Override
    public String toString() {
        String asString = super.toString();

        asString += "\n&rDuration: &7" + (duration / 20) + " seconds";
        asString += "\n&rBlocks per second: &7" + blocksPerTick * 20;
        asString += "\n&rWidth: &7" + width;
        asString += "\n&rMax slope: &7" + maxSlope;
        asString += "\n&rNeeds line of sight? &7" + useLineOfSight;
        asString += "\n&rMaterial: &7" + WbsEnums.toPrettyString(material);

        return asString;
    }
}
