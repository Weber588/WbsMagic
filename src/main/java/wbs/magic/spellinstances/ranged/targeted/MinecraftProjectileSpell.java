package wbs.magic.spellinstances.ranged.targeted;

import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import wbs.magic.annotations.DamageSpell;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.spells.SpellConfig;
import wbs.magic.targeters.SelfTargeter;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMath;
import wbs.utils.util.configuration.WbsConfigReader;

import java.util.Set;

@Spell(name = "Generic Projectile",
        cost = 5,
        cooldown = 5,
        description = "Fire a minecraft projectile, as configured."
)
@SpellOption(optionName = "projectile", type = SpellOptionType.STRING, defaultString = "ARROW", enumType = EntityType.class)
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 1)
@SpellOption(optionName = "amount", type = SpellOptionType.INT)
@SpellOption(optionName = "delay", type = SpellOptionType.DOUBLE, defaultDouble = 0.5)
@SpellOption(optionName = "spread", type = SpellOptionType.DOUBLE, defaultDouble = 0.1)
@SpellOption(optionName = "on-fire", type = SpellOptionType.BOOLEAN, defaultBool = false, aliases = {"burning", "fire"})
@SpellOption(optionName = "allow-explosions", type = SpellOptionType.BOOLEAN, defaultBool = false)
// Overrides
@SpellOption(optionName = "targeter", type = SpellOptionType.STRING, defaultString = "SELF")
public class MinecraftProjectileSpell extends TargetedSpell {

    @SuppressWarnings("unchecked")
    public MinecraftProjectileSpell(SpellConfig config, String directory) {
        super(config, directory);

        speed = config.getDouble("speed");
        spread = config.getDouble("spread");
        amount = config.getInt("amount");
        delay = (int) (config.getDouble("delay") * 20);
        onFire = config.getBoolean("on-fire");
        allowExplosions = config.getBoolean("allow-explosions");

        String projectileString = config.getString("projectile");
        EntityType projectileType = WbsEnums.getEnumFromString(EntityType.class, projectileString);

        if (projectileType == null) {
            logError("Invalid projectile: " + projectileString, directory);
            projectileType = EntityType.ARROW;
        } else {
            Class<? extends Entity> clazz = projectileType.getEntityClass();

            if (clazz == null || !Projectile.class.isAssignableFrom(clazz)) {
                logError("Invalid projectile: " + projectileString, directory);
                projectileType = EntityType.ARROW;
            }
        }

        projectileName = WbsEnums.toPrettyString(projectileType);

        projectileClass = (Class<? extends Projectile>) projectileType.getEntityClass();
    }

    private final Class<? extends Projectile> projectileClass;
    private final String projectileName;
    private final double speed;
    private final double spread;
    private final int amount;
    private final int delay;
    private final boolean onFire;
    private final boolean allowExplosions;

    @Override
    protected <T extends LivingEntity> boolean preCast(SpellCaster caster, Set<T> targets) {
        if (disabled) return true;

        if (targeter instanceof SelfTargeter || targets.size() == 1) {
            caster.sendActionBar("Firing &h" + amount + " " + projectileName + (amount != 1 ? "s" : "") + "&r!");
        }

        if (delay <= 0) {
            for (int i = 0; i < amount; i++) {
                for (LivingEntity target : targets) {
                    castOn(caster, target);
                }
            }
        } else {
            new BukkitRunnable() {
                int amountSoFar = 0;

                @Override
                public void run() {
                    for (LivingEntity target : targets) {
                        castOn(caster, target);
                    }

                    amountSoFar++;

                    if (amountSoFar >= amount) {
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0, delay);
        }

        return true;
    }

    private boolean disabled = false;

    @Override
    protected <T extends LivingEntity> void castOn(SpellCaster caster, T target) {

        Projectile proj;
        try {
            proj = target.getWorld().spawn(caster.getEyeLocation(), projectileClass);
        } catch (IllegalArgumentException e) {
            disabled = true;
            logger.warning(e.getMessage());
            return;
        }
        proj.setShooter(caster.getPlayer());

        if (proj instanceof ShulkerBullet) {
            ShulkerBullet bullet = (ShulkerBullet) proj;

            bullet.setTarget(target);
            return;
        }

        if (proj instanceof AbstractArrow) {
            AbstractArrow arrow = (AbstractArrow) proj;
            arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
            arrow.setTicksLived(60 * 20 - 10 * 20); // 1 minute - 10 seconds
        }

        if (proj instanceof Explosive) {
            Explosive explosive = (Explosive) proj;
            explosive.setIsIncendiary(onFire && allowExplosions);
            if (!allowExplosions) explosive.setYield(0);
        }

        Vector velocity;
        if (targeter instanceof SelfTargeter) {
            velocity = caster.getFacingVector();
        } else {
            velocity = target.getEyeLocation()
                    .subtract(caster.getEyeLocation())
                    .toVector();
        }

        velocity.add(WbsMath.randomVector(spread))
                .normalize()
                .multiply(speed);

        proj.setVelocity(velocity);

        if (proj instanceof Fireball) {
            ((Fireball) proj).setDirection(velocity);
        }

        if (onFire) {
            proj.setFireTicks(100000);
        }

    }
}
