package wbs.magic.spells.ranged.targeted;

import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import wbs.magic.SpellCaster;
import wbs.magic.objects.AlignmentType;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.FailableSpell;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.utils.util.particles.SphereParticleEffect;

import java.util.Collection;

@Spell(name = "Divine Shield",
        description = "The target creature is given a 1 time immunity to any amount of damage.")
@FailableSpell("If the targeted creature does not take damage within the duration of Divine Shield, the effect will fade.")
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 60)
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.DIVINE, enumType = AlignmentType.class)
public class DivineShield extends StatusSpell {
    private static final Listener LISTENER = new DivineShieldListener();

    public DivineShield(SpellConfig config, String directory) {
        super(config, directory);

        effect.setRadius(1);
        tryRegisterListener(LISTENER);
    }

    private final SphereParticleEffect effect = new SphereParticleEffect();

    @Override
    protected @NotNull MagicEntityEffect generateEffect(LivingEntity target, @NotNull SpellCaster caster) {
        return new MagicEntityEffect(target, caster, this) {
            @Override
            public boolean onTick(Entity entity) {
                effect.play(Particle.CRIT, location);
                return false;
            }
        };
    }

    @SuppressWarnings("unused")
    private static class DivineShieldListener implements Listener {
        @EventHandler(priority= EventPriority.HIGHEST,ignoreCancelled=true)
        public void damageDivineShield(EntityDamageByEntityEvent event) {
            Entity entity = event.getEntity();
            if (!(entity instanceof LivingEntity)) {
                return;
            }

            Collection<MagicEntityEffect> effects = MagicEntityEffect.getEffects(entity);

            for (MagicEntityEffect effect : effects) {
                if (effect.getSpell() instanceof DivineShield) {
                    event.setCancelled(true);
                    if (entity instanceof Player) {
                        SpellCaster caster = effect.getCaster();
                        if (entity.equals(caster.getPlayer())) {
                            caster.sendActionBar("Your &h" + effect.getSpell().getName() + "&r fades away...");
                        } else {
                            plugin.sendActionBar("&h" + caster.getName() + "'s " + effect.getSpell().getName() + "&r fades away...", (Player) entity);
                        }
                    }
                    break; // Only pop one shield - they could have multiple
                }
            }
        }
    }
}
