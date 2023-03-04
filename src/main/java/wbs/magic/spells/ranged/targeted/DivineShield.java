package wbs.magic.spells.ranged.targeted;

import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import wbs.magic.objects.AlignmentType;
import wbs.magic.spellmanagement.configuration.FailableSpell;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.SpellCaster;
import wbs.magic.spellmanagement.configuration.options.EnumOptions;
import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.particles.SphereParticleEffect;

@Spell(name = "Divine Shield",
        description = "The target creature is given a 1 time immunity to any amount of damage.")
@FailableSpell("If the targeted creature does not take damage within the duration of Divine Shield, the effect will fade.")
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 60)
@EnumOptions.EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.DIVINE, enumType = AlignmentType.class)
public class DivineShield extends TargetedSpell {

    public DivineShield(SpellConfig config, String directory) {
        super(config, directory);

        duration = (int) (config.getDouble("duration") * 20);

        effect.setRadius(1);
    }

    private final int duration;
    private final SphereParticleEffect effect = new SphereParticleEffect();

    @Override
    public void castOn(CastingContext context, LivingEntity target) {
        SpellCaster caster = context.caster;

        MagicEntityEffect marker = new MagicEntityEffect(target, caster, this) {
            @Override
            public boolean onTick(Entity entity) {
                effect.play(Particle.CRIT, location);
                return false;
            }
        };

        marker.setMaxAge(duration);

        marker.run();
    }

    @Override
    public String toString() {
        String asString = super.toString();

        asString += "\n&rDuration: &7" + (duration / 20.0) + " seconds";

        return asString;
    }
}
