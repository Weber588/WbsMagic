package wbs.magic.spells.ranged.targeted;

import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import wbs.magic.SpellCaster;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.options.DoubleOptions.DoubleOption;
import wbs.utils.util.particles.LineParticleEffect;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Spell(name = "Empathic Link", description = "Your health is linked to another creatures, and you take a percentage of their damage.")
@DoubleOption(optionName = "percent", defaultValue = 50)
public class EmpathicLink extends StatusSpell {
    private final static EmpathicLinkListener LISTENER = new EmpathicLinkListener();

    private final double percentage;
    private final LineParticleEffect effect = new LineParticleEffect();

    public EmpathicLink(SpellConfig config, String directory) {
        super(config, directory);

        percentage = config.getDouble("percent");

        effect.setScaleAmount(true);
        effect.setAmount(1);
        
        tryRegisterListener(LISTENER);
    }

    private static class EmpathicLinkListener implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onDamage(EntityDamageEvent event) {
            Entity entity = event.getEntity();

            Collection<MagicEntityEffect> effects = MagicEntityEffect.getEffects(entity, EmpathicLink.class);

            if (!effects.isEmpty()) {
                // Map of caster to the percentage they're taking (may exceed 100 if the caster has multiple
                // links for some reason - this is fine as we'll use weighted calculation to distribute damage)
                Map<SpellCaster, Double> castersTakingDamage = new HashMap<>();
                LivingEntity livingEntity = (LivingEntity) entity;

                final double initialDamage = event.getDamage();
                double damageTaken = initialDamage;
                for (MagicEntityEffect effect : effects) {
                    EmpathicLink spell = (EmpathicLink) effect.getSpell();
                    SpellCaster caster = effect.caster;

                    damageTaken *= (spell.percentage / 100);
                    castersTakingDamage.computeIfPresent(caster, (key, current) -> spell.percentage + current);
                    castersTakingDamage.putIfAbsent(caster, spell.percentage);
                    
                    spell.effect.play(Particle.CRIT_MAGIC, livingEntity.getEyeLocation(), caster.getEyeLocation());
                }

                double damageToDistribute = initialDamage - damageTaken;

                double totalPercentage = castersTakingDamage.values()
                        .stream()
                        .mapToDouble(Double::doubleValue)
                        .sum();

                for (SpellCaster caster : castersTakingDamage.keySet()) {
                    double fractionOfTotal = castersTakingDamage.get(caster) / totalPercentage;
                    caster.getPlayer().damage(damageToDistribute * fractionOfTotal);
                }
            }
        }
    }
}
