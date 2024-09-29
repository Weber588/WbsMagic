package wbs.magic.spells.ranged.targeted;

import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.magic.SpellCaster;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.options.DoubleOptions.DoubleOption;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.spells.framework.LivingEntitySpell;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.RadiusTargeter;
import wbs.utils.util.particles.*;
import wbs.utils.util.providers.NumProvider;
import wbs.utils.util.providers.generator.num.CycleGenerator;

import java.util.Collection;

@Spell(name = "Planar Binding", description = "Binds a creature to this plane, preventing it from teleporting!")
@DoubleOption(optionName = "duration", defaultValue = 15)
@TargeterOption(optionName = "targeter", defaultType = RadiusTargeter.class, defaultRange = 15)
public class PlanarBinding extends StatusSpell {
    private static final PlanarBindingListener LISTENER = new PlanarBindingListener();

    public PlanarBinding(SpellConfig config, String directory) {
        super(config, directory);

        effect = new SpiralParticleEffect();
        effect.setRadius(0.5)
                .setAbout(new Vector(0, 1, 0))
                .setSpeed(0.2)
                .setAmount(1);

        tryRegisterListener(LISTENER);
    }

    private final SpiralParticleEffect effect;

    @Override
    protected @NotNull MagicEntityEffect generateEffect(LivingEntity target, @NotNull SpellCaster caster) {
        CircleParticleEffect clonedEffect = effect.clone()
                .setRotation(new NumProvider(new CycleGenerator(0, 360, 30, 0)));

        clonedEffect.setRadius(target.getWidth() * 2 / 3);

        return new MagicEntityEffect(target, caster, PlanarBinding.this) {
            @Override
            protected boolean onTick(Entity entity) {
                clonedEffect.buildAndPlay(Particle.WITCH, getLocation());
                return super.onTick(entity);
            }
        };
    }

    @SuppressWarnings("unused")
    private static class PlanarBindingListener implements Listener {
        @EventHandler(priority= EventPriority.HIGHEST,ignoreCancelled=true)
        public void teleportPlanarBinding(EntityTeleportEvent event) {
            Entity entity = event.getEntity();
            if (!(entity instanceof LivingEntity)) {
                return;
            }

            Collection<MagicEntityEffect> effects = MagicEntityEffect.getEffects(entity);

            for (MagicEntityEffect effect : effects) {
                if (effect.getSpell() instanceof PlanarBinding) {
                    event.setCancelled(true);
                }
            }
        }

        @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
        public void teleportPlanarBinding(PlayerTeleportEvent event) {
            Player player = event.getPlayer();

            SpellCaster eventCaster = null;

            PlayerTeleportEvent.TeleportCause cause = event.getCause();
            switch (cause) {
                case ENDER_PEARL:
                case CHORUS_FRUIT:
                case PLUGIN:
                    break;
                default:
                    return;
            }

            if (SpellCaster.isRegistered(player)) {
                eventCaster = SpellCaster.getCaster(player);

                if (cause == PlayerTeleportEvent.TeleportCause.PLUGIN && !eventCaster.isMagicTeleporting()) {
                    // Some other non-magical teleport - don't try to cancel it
                    return;
                }
            }

            Collection<MagicEntityEffect> effects = MagicEntityEffect.getEffects(player);

            for (MagicEntityEffect effect : effects) {
                if (effect.getSpell() instanceof PlanarBinding) {
                    event.setCancelled(true);

                    if (eventCaster != null) {
                        eventCaster.sendActionBar(effect.caster.getName() + "'s &h" + effect.getSpell().getName()
                                + "&r prevents you from teleporting!");
                    }
                    break;
                }
            }
        }
    }
}
