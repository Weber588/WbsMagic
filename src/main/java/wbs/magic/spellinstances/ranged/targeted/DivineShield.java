package wbs.magic.spellinstances.ranged.targeted;

import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import wbs.magic.annotations.FailableSpell;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spells.SpellConfig;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.particles.SphereParticleEffect;

@Spell(name = "Divine Shield",
        description = "The target creature is given a 1 time immunity to any amount of damage.")
@FailableSpell("If the targeted creature does not take damage within the duration of Divine Shield, the effect will fade.")
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 60)
public class DivineShield extends TargetedSpell {

    public static final NamespacedKey DIVINE_SHIELD_KEY = new NamespacedKey(plugin, "divine_shield");

    public DivineShield(SpellConfig config, String directory) {
        super(config, directory);

        duration = config.getDouble("duration");

        effect.setRadius(1);
    }

    private double duration;
    private SphereParticleEffect effect = new SphereParticleEffect();

    @Override
    protected <T extends LivingEntity> void castOn(SpellCaster caster, T target) {
        PersistentDataContainer container = target.getPersistentDataContainer();
        container.set(DIVINE_SHIELD_KEY, PersistentDataType.STRING, caster.getName());

        MagicEntityEffect marker = new MagicEntityEffect(target, caster, this) {
            @Override
            public boolean tick() {
                super.tick();

                effect.play(Particle.CRIT, location);
                return false;
            }

            @Override
            protected void onRemove() {
                String divineCaster = container.get(DIVINE_SHIELD_KEY, PersistentDataType.STRING);
                if (divineCaster != null && divineCaster.equalsIgnoreCase(caster.getName())) {
                    container.remove(DIVINE_SHIELD_KEY);
                }
            }
        };

        marker.run();
    }

    @Override
    public String toString() {
        String asString = super.toString();

        asString += "\n&rDuration: &7" + duration + " seconds";

        return asString;
    }
}
