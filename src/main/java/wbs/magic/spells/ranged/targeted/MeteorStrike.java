package wbs.magic.spells.ranged.targeted;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.magic.DamageType;
import wbs.magic.SpellCaster;
import wbs.magic.objects.AlignmentType;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.DamageSpell;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.options.BoolOptions.BoolOption;
import wbs.magic.spellmanagement.configuration.options.DoubleOptions.DoubleOption;
import wbs.magic.spellmanagement.configuration.options.LocationTargeterOptions.LocationTargeterOption;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.spells.framework.LocationTargetedSpell;
import wbs.magic.targeters.location.LocationTargeter;
import wbs.utils.util.particles.NormalParticleEffect;

@Spell(name = "Meteor Strike",
        description = "Strike a location with a meteor from the sky that damages and ignites all nearby creatures.",
        alignment = AlignmentType.Name.NEGATIVE
)
@DamageSpell(defaultDamage = 5,
        damageTypes = {DamageType.Name.FIRE, DamageType.Name.EXPLOSION}
)
@LocationTargeterOption(optionName = "targeter", defaultRange = 100)
@DoubleOption(optionName = "speed", defaultValue = 0.2)
@DoubleOption(optionName = "yield", defaultValue = 1)
@DoubleOption(optionName = "fire-duration", defaultValue = 5)
@BoolOption(optionName = "create-fire", defaultValue = false)
public class MeteorStrike extends SpellInstance implements LocationTargetedSpell {
    private static final MeteorStrikeListener LISTENER = new MeteorStrikeListener();

    public MeteorStrike(SpellConfig config, String directory) {
        super(config, directory);

        damage = config.getDouble("damage");
        targeter = config.getLocationTargeter("targeter");
        speed = Math.abs(config.getDouble("speed"));
        yield = Math.abs(config.getDouble("yield"));
        createFire = config.getBoolean("create-fire");
        fireTicks = config.getDurationFromDouble("fire-duration");

        explodeFireEffect.setXYZ(yield);
        explodeFireEffect.setSpeed(yield / 15);

        tryRegisterListener(LISTENER);
    }

    private final LocationTargeter targeter;
    private final double damage;
    private final double speed;
    private final double yield;
    private final int fireTicks;
    private final boolean createFire;

    private final NormalParticleEffect explodeFireEffect = new NormalParticleEffect();

    @Override
    public void castOnLocation(@NotNull CastingContext context, @NotNull World world, @NotNull Location target) {
        Location aboveTarget = target.clone().add(0, 10, 0);

        LargeFireball fireball = world.spawn(aboveTarget, LargeFireball.class, spawned -> {
            spawned.setYield(0);
            Vector velocity = new Vector(0, -speed, 0);
            spawned.setDirection(velocity);
            spawned.setVelocity(velocity);
        });

        MagicEntityEffect fireballEffect = new MagicEntityEffect(fireball, context.caster, this) {
            @Override
            protected void onRemove() {
                // Call before MagicEntityEffect onRemove, to check if the fireball is valid (i.e. is it
                // being removed because it was dispelled/otherwise removed, or did it hit something?)
                if (!getEntity().isValid()) {
                    detonate(getLocation(), world, caster);
                }

                super.onRemove();
            }
        };

        fireballEffect.setRemoveOnExpire(true);
        fireballEffect.setExpireOnDeath(true);

        fireballEffect.run();
    }

    private void detonate(Location location, World world, SpellCaster caster) {
        caster.setCurrentDamageSource(getDamageSource());

        explodeFireEffect.play(Particle.FLAME, location);
        world.createExplosion(location, (float) yield, createFire, false, caster.getPlayer());

        caster.setCurrentDamageSource(null);
    }

    @Override
    public LocationTargeter getTargeter() {
        return targeter;
    }

    private static class MeteorStrikeListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onExplosionDamage(EntityDamageByEntityEvent event) {
            if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                return;
            }

            Entity attacker = event.getDamager();

            if (attacker instanceof Player) {
                Player attackingPlayer = (Player) attacker;

                if (SpellCaster.isRegistered(attackingPlayer)) {
                    SpellCaster caster = SpellCaster.getCaster(attackingPlayer);
                    SpellInstance spell = caster.getCurrentDamageSpell();
                    if (spell instanceof MeteorStrike) {
                        MeteorStrike meteorStrike = (MeteorStrike) spell;

                        // Don't need to handle DamageType modifiers - this is an actual explosion in the world.
                        // We're just updating the damage to be more exact here.
                        event.setDamage(meteorStrike.damage);

                        Entity target = event.getEntity();
                        target.setFireTicks(meteorStrike.fireTicks);
                    }
                }
            }
        }
    }
}
