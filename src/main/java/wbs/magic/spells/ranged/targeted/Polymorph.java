package wbs.magic.spells.ranged.targeted;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;

import wbs.magic.objects.AlignmentType;
import wbs.magic.spellmanagement.configuration.RequiresPlugin;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellSettings;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.SpellManager;
import wbs.magic.SpellCaster;

import wbs.magic.spellmanagement.configuration.options.EnumOptions;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.targeters.RadiusTargeter;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.WbsRunnable;
import wbs.utils.util.particles.NormalParticleEffect;

import java.util.Collection;
import java.util.Set;

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
// Overrides
@TargeterOptions.TargeterOption(optionName = "targeter", defaultRange = 100)
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.NEGATIVE, enumType = AlignmentType.class)
public class Polymorph extends TargetedSpell {
    public Polymorph(SpellConfig config, String directory) {
        super(config, directory);

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
    public boolean preCastEntity(CastingContext context, Collection<LivingEntity> targets) {
        SpellCaster caster = context.caster;
        if (!Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
            System.out.println(this.getClass().getName() + " requires LibsDisguises!");
            SpellManager.unregisterSpell(this.getClass());
        }
        return false;
    }

    @Override
    public void castOn(CastingContext context, LivingEntity target) {
        SpellCaster caster = context.caster;

        AttributeModifier speedMod = new AttributeModifier(
                Attribute.MOVEMENT_SPEED.getKey(),
                mobSpeed,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
        );
        AttributeModifier damageMod = new AttributeModifier(
                Attribute.ATTACK_DAMAGE.getKey(),
                -1,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
        );

        AttributeInstance speedAttr = target.getAttribute(Attribute.MOVEMENT_SPEED);
        AttributeInstance dmgAttr = target.getAttribute(Attribute.ATTACK_DAMAGE);

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

    @Override
    public String toString() {
        String asString = super.toString();

        asString += "\n&rDuration: &7" + duration + " seconds";
        asString += "\n&rMob speed: &7" + mobSpeed;

        return asString;
    }
}
