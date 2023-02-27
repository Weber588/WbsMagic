package wbs.magic.spells.ranged.targeted;

import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import wbs.magic.generators.EntityGenerator;
import wbs.magic.WbsMagic;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellSettings;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.targeters.RadiusTargeter;
import wbs.magic.targeters.SelfTargeter;
import wbs.magic.SpellCaster;
import wbs.utils.util.WbsMath;

import java.util.Collection;
import java.util.Set;

@Spell(name = "Generic Projectile",
        cost = 5,
        cooldown = 5,
        description = "Fire a minecraft projectile, as configured."
)
@SpellSettings(isEntitySpell = true)
@SpellOption(optionName = "projectile", type = SpellOptionType.STRING, defaultString = "ARROW", enumType = EntityType.class)
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 1)
@SpellOption(optionName = "amount", type = SpellOptionType.INT)
@SpellOption(optionName = "delay", type = SpellOptionType.DOUBLE, defaultDouble = 0.25)
@SpellOption(optionName = "spread", type = SpellOptionType.DOUBLE, defaultDouble = 0.1)

// Overrides
@TargeterOptions.TargeterOption(optionName = "targeter", defaultType = SelfTargeter.class, defaultRange = 60)
public class ShootEntitySpell extends TargetedSpell {

    public ShootEntitySpell(SpellConfig config, String directory) {
        super(config, directory);

        speed = config.getDouble("speed");
        spread = config.getDouble("spread");
        amount = config.getInt("amount");
        delay = (int) (config.getDouble("delay") * 20);

        entityGenerator = new EntityGenerator(config, directory);
    }

    private final double speed;
    private final double spread;
    private final int amount;
    private final int delay;

    private final EntityGenerator entityGenerator;

    @Override
    public boolean preCastEntity(CastingContext context, Collection<LivingEntity> targets) {
        SpellCaster caster = context.caster;
        if (disabled) return true;

        if (targets.size() == 1) {
            caster.sendActionBar("Firing &h" + amount + " " + entityGenerator.getEntityName() + (amount != 1 ? "s" : "") + "&r!");
        } else {
            caster.sendActionBar("Firing &h" + amount + " " + entityGenerator.getEntityName() + (amount != 1 ? "s" : "") + "&r at " + targets.size() + " creatures!");
        }

        if (delay <= 0) {
            for (int i = 0; i < amount; i++) {
                for (LivingEntity target : targets) {
                    castOn(context, target);
                }
            }
        } else {
            new BukkitRunnable() {
                int amountSoFar = 0;

                @Override
                public void run() {
                    for (LivingEntity target : targets) {
                        castOn(context, target);
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
    public void castOn(CastingContext context, LivingEntity target) {
        SpellCaster caster = context.caster;
        Entity entity = entityGenerator.spawn(caster.getEyeLocation(), target, caster.getPlayer());

        if (entity == null) {
            disabled = true;
            logError("Invalid entity tried to spawn: " + entityGenerator.getEntityName(), "Runtime - " + registeredSpell.getName());
            return;
        }

        MagicEntityEffect marker = new MagicEntityEffect(entity, caster, this);

        marker.setExpireOnDeath(true);
        marker.setRemoveOnExpire(true);

        marker.run();

        if (target.equals(caster.getPlayer())) {
            if (entity instanceof Mob) {
                ((Mob) entity).setTarget(null);
            }
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

        entity.setVelocity(velocity);

        if (entity instanceof Fireball) {
            ((Fireball) entity).setDirection(velocity);
        }
    }


    @Override
    public String toString() {
        String asString = super.toString();

        asString += "\n&rSpeed: &7" + speed;
        asString += "\n&rAmount: &7" + amount;
        asString += "\n&rDelay: &7" + delay * 20;
        asString += "\n" + entityGenerator.toString();

        return asString;
    }
}
