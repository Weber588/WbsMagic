package wbs.magic.spellinstances.ranged.targeted;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import wbs.magic.annotations.RequiresPlugin;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.annotations.SpellSettings;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spells.SpellConfig;
import wbs.magic.spells.SpellManager;
import wbs.magic.targeters.LineOfSightTargeter;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.WbsEntities;
import wbs.utils.util.WbsRunnable;
import wbs.utils.util.particles.NormalParticleEffect;

@Spell(name = "Polymorph",
        cost = 150,
        cooldown = 120,
        description = "Transform the target into a sheep for the duration of the spell"
)
@SpellSettings(canBeConcentration = true)
@RequiresPlugin("LibsDisguises")
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 60)
@SpellOption(optionName = "mob-speed", type = SpellOptionType.DOUBLE, defaultDouble = -0.5)
@SpellOption(optionName = "break-on-damage", type = SpellOptionType.BOOLEAN, defaultBool = true)
public class Polymorph extends TargetedSpell {
    
    private static final LineOfSightTargeter TARGETER = new LineOfSightTargeter();
    private static final double DEFAULT_RANGE = 100;

    public Polymorph(SpellConfig config, String directory) {
        super(config, directory, DEFAULT_RANGE, TARGETER);

        duration = (long) (config.getDouble("duration") * 20);
        mobSpeed = config.getDouble("mob-speed");
        breakOnDamage = config.getBoolean("break-on-damage");

        poofEffect.setAmount(25);
    }

    private final long duration;
    private final double mobSpeed;
    private final boolean breakOnDamage;

    private final NormalParticleEffect poofEffect = new NormalParticleEffect()
            .setSpeed(0.05).setXYZ(0);

    @Override
    protected <T extends LivingEntity> void castOn(SpellCaster caster, T target) {
        if (!Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
            System.out.println(this.getClass().getName() + " requires LibsDisguises!");
            SpellManager.unregisterSpell(this.getClass());
            return;
        }

        AttributeModifier speedMod = new AttributeModifier(
                Attribute.GENERIC_MOVEMENT_SPEED.name(),
                mobSpeed,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
        );
        AttributeModifier damageMod = new AttributeModifier(
                Attribute.GENERIC_ATTACK_DAMAGE.name(),
                0,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
        );

        AttributeInstance speedAttr = target.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        AttributeInstance dmgAttr = target.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);

        speedAttr.addModifier(speedMod);
        dmgAttr.addModifier(damageMod);

        MobDisguise disguise = new MobDisguise(DisguiseType.SHEEP);

        disguise.setEntity(target);
        disguise.startDisguise();

        poofEffect.play(Particle.CLOUD, WbsEntities.getMiddleLocation(target));

        if (isConcentration) caster.setConcentration(this);

        MagicEntityEffect marker = new MagicEntityEffect(target, caster, this);

        new WbsRunnable() {
            double health = target.getHealth();
            int age = 0;

            @Override
            public void run() {
                if (marker.isExpired()) {
                    cancel();
                }

                if (breakOnDamage) {
                    if (target.getHealth() < health) {
                        cancel();
                    } else if (target.getHealth() > health) { // Can heal but not take damage
                        health = target.getHealth();
                    }
                }

                if ((isConcentration && !caster.isConcentratingOn(Polymorph.this)) || target.isDead()) {
                    cancel();
                }

                age++;

                if (age >= duration) {
                    cancel();
                }
            }

            @Override
            protected void finish() {
                if (!target.isDead()) {
                    speedAttr.getModifiers().forEach(speedAttr::removeModifier);
                    dmgAttr.getModifiers().forEach(dmgAttr::removeModifier);
                }

                disguise.stopDisguise();
                poofEffect.play(Particle.CLOUD, WbsEntities.getMiddleLocation(target));
            }
        }.runTaskTimer(plugin, 0L, 1);

    }
}
