package wbs.magic.spellinstances.ranged.targeted;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.EntityGenerator;
import wbs.magic.WbsMagic;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.annotations.SpellSettings;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.spells.SpellConfig;
import wbs.magic.targeters.SelfTargeter;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMaterials;
import wbs.utils.util.WbsMath;

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
@SpellOption(optionName = "targeter", type = SpellOptionType.STRING, defaultString = "SELF")
public class ShootEntitySpell extends TargetedSpell {

    public ShootEntitySpell(SpellConfig config, String directory) {
        super(config, directory);

        speed = config.getDouble("speed");
        spread = config.getDouble("spread");
        amount = config.getInt("amount");
        delay = (int) (config.getDouble("delay") * 20);

        entityGenerator = new EntityGenerator(config, WbsMagic.getInstance().settings, directory);
    }

    private final double speed;
    private final double spread;
    private final int amount;
    private final int delay;

    private final EntityGenerator entityGenerator;

    @Override
    protected <T extends LivingEntity> boolean preCast(SpellCaster caster, Set<T> targets) {
        if (disabled) return true;

        if (targets.size() == 1) {
            caster.sendActionBar("Firing &h" + amount + " " + entityGenerator.getEntityName() + (amount != 1 ? "s" : "") + "&r!");
        } else {
            caster.sendActionBar("Firing &h" + amount + " " + entityGenerator.getEntityName() + (amount != 1 ? "s" : "") + "&r at " + targets.size() + " creatures!");
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
        Entity entity = entityGenerator.spawn(caster.getEyeLocation(), target, caster.getPlayer());

        if (entity == null) {
            disabled = true;
            return;
        }

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
