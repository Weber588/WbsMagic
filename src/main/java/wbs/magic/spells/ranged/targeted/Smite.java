package wbs.magic.spells.ranged.targeted;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import wbs.magic.SpellCaster;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.DamageSpell;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.options.BoolOptions.BoolOption;
import wbs.magic.spellmanagement.configuration.options.DoubleOptions;
import wbs.magic.spellmanagement.configuration.options.DoubleOptions.DoubleOption;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.spells.framework.LivingEntitySpell;
import wbs.magic.targeters.GenericTargeter;
import wbs.utils.util.pluginhooks.WbsRegionUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Spell(name = "Smite", description = "Strikes targets with lightning, with configurable damage!")
@DamageSpell(defaultDamage = 4, deathFormat = "%victim% was struck by %attacker%'s lightning!")
@TargeterOption(optionName = "targeter", defaultRange = 100)
@BoolOption(optionName = "convert-mobs", defaultValue = false)
@BoolOption(optionName = "create-fire", defaultValue = false)
@DoubleOption(optionName = "burn-duration", defaultValue = 1)
public class Smite extends SpellInstance implements LivingEntitySpell {
    private final static int FIRE_RADIUS = 2;
    private final static Material SOUL_FIRE = Material.getMaterial("SOUL_FIRE");

    public Smite(SpellConfig config, String directory) {
        super(config, directory);

        targeter = config.getTargeter("targeter");
        convertMobs = config.getBoolean("convert-mobs");
        createFire = config.getBoolean("create-fire");
        damage = config.getDouble("damage");
        fireTicks = (int) (config.getDouble("burn-duration") * 20);
    }

    private final GenericTargeter targeter;
    private final boolean convertMobs;
    private final boolean createFire;
    private final double damage;
    private final int fireTicks;

    @Override
    public void castOn(CastingContext context, LivingEntity target) {
        SpellCaster caster = context.caster;

        Location strikeLoc = target.getLocation();

        Set<Block> fireBlocks = getFireBlocks(strikeLoc);

        // Damage before lightning, so we can control the amount. Damage tick will protect them if
        // a real lightning strike is used.
        if (damage > 0) {
            target.setFireTicks(fireTicks);
            caster.damage(target, damage, this);
        }

        if (convertMobs) {
            target.getWorld().strikeLightning(strikeLoc);

            // Extinguish fire created by real lightning
            Set<Block> postFireBlocks = getFireBlocks(strikeLoc);
            for (Block fire : postFireBlocks) {
                if (!fireBlocks.contains(fire)) {
                    fire.setType(Material.AIR);
                }
            }
        } else {
            // Don't convert mobs; just use an effect
            target.getWorld().strikeLightningEffect(strikeLoc);

        }

        // Create fire artificially either way, to account for region protections
        if (createFire) {
            strikeFire(strikeLoc, caster.getPlayer());
        }
    }

    private void strikeFire(Location loc, Player player) {
        int blockX = loc.getBlockX();
        int blockY = loc.getBlockY();
        int blockZ = loc.getBlockZ();
        World world = loc.getWorld();
        Objects.requireNonNull(world);

        // 100 / (typical number of surface blocks)
        double chanceToIgnite = 100.0 / (FIRE_RADIUS * FIRE_RADIUS);

        for (int x = blockX - FIRE_RADIUS; x < blockX + FIRE_RADIUS; x++) {
            for (int y = blockY - FIRE_RADIUS; y < blockY - FIRE_RADIUS; y++) {
                for (int z = blockZ - FIRE_RADIUS; z < blockZ - FIRE_RADIUS; z++) {
                    if (chance(chanceToIgnite)) {
                        Block block = world.getBlockAt(x, y, z);
                        Block blockBelow = block.getRelative(BlockFace.UP);
                        if (block.getType().isAir()) {
                            if (blockBelow.getType().isSolid()) {
                                if (WbsRegionUtils.canBuildAt(block.getLocation(), player)) {
                                    block.setType(Material.FIRE);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Set<Block> getFireBlocks(Location loc) {
        int blockX = loc.getBlockX();
        int blockY = loc.getBlockY();
        int blockZ = loc.getBlockZ();
        World world = loc.getWorld();
        Objects.requireNonNull(world);

        Set<Block> fireBlocks = new HashSet<>();

        // Get fire instances in 2 block radius to remove any created by lightning
        for (int x = blockX - FIRE_RADIUS; x < blockX + FIRE_RADIUS; x++) {
            for (int y = blockY - FIRE_RADIUS; y < blockY - FIRE_RADIUS; y++) {
                for (int z = blockZ - FIRE_RADIUS; z < blockZ - FIRE_RADIUS; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == Material.FIRE || block.getType() == SOUL_FIRE) {
                        fireBlocks.add(block);
                    }
                }
            }
        }

        return fireBlocks;
    }

    @Override
    public GenericTargeter getTargeter() {
        return targeter;
    }
}
