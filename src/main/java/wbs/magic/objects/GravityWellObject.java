package wbs.magic.objects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import wbs.magic.objects.generics.MagicObject;
import wbs.magic.objects.generics.ProjectileObject;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.RingParticleEffect;

import java.util.List;
import java.util.Set;

public class GravityWellObject extends MagicObject {
    public GravityWellObject(Location location, SpellCaster caster, SpellInstance castingSpell) {
        super(location, caster, castingSpell);

        ringEffect.setRelative(true);
        ringEffect.setAmount(10);

        effect.setAmount(1);
        effect.setXYZ(0);
    }

    private final RingParticleEffect ringEffect = new RingParticleEffect();
    private final NormalParticleEffect effect = new NormalParticleEffect();

    private double distance;
    private double force; // The force at 1 block away
    private boolean ignoreCaster; // The force at 1 block away

    private boolean targetEntities;
    private boolean targetProjectiles;

    private int duration;
    private int age = 0;

    @Override
    protected void onRun() {
        ringEffect.setSpeed(force);
    }

    @Override
    protected boolean tick() {
        if (age >= duration) {
            return true;
        }

        if (castingSpell.isConcentration() && !caster.isConcentratingOn(castingSpell)) {
            caster.concentrationBroken();
            return true;
        }

        if (age % 3 == 0) {
            effect.play(Particle.SQUID_INK, getLocation());
        }

        age++;

        ringEffect.setRotation(age * 2);
        ringEffect.buildAndPlay(Particle.PORTAL, getLocation());

        if (targetProjectiles) {
            List<MagicObject> objects = MagicObject.getNearbyActive(getLocation(), distance);

            for (MagicObject obj : objects) {
                if (obj instanceof ProjectileObject) {
                    ProjectileObject proj = (ProjectileObject) obj;

                    Vector velocity = proj.getDirection();

                    proj.setDirection(applyGravity(velocity, proj.getLocation(), null));
                }
            }

            Set<Projectile> nearbyProjEntities = WbsEntities.getNearbySpherical(getLocation(), distance, Projectile.class);

            for (Projectile proj : nearbyProjEntities) {
                Vector velocity = proj.getVelocity();

                proj.setVelocity(applyGravity(velocity, proj.getLocation(), null));
            }
        }

        if (targetEntities) {
            Player exclude = null;
            if (ignoreCaster) {
                exclude = caster.getPlayer();
            }

            Set<LivingEntity> nearbyEntities = WbsEntities.getNearbyLiving(getLocation(), distance, exclude);

            for (LivingEntity entity : nearbyEntities) {
                Vector velocity = entity.getVelocity();

                entity.setVelocity(applyGravity(velocity, entity.getLocation(), entity));
            }
        }

        return false;
    }

    private Vector applyGravity(Vector current, Location location, @Nullable Entity entity) {
        Vector pullForce = getLocation().subtract(location).toVector();
        double distSquared = pullForce.lengthSquared();
        pullForce.normalize().multiply(force / distSquared);

        Vector newVelocity = current.clone().add(pullForce);

        double currentLength = current.length();
        if (currentLength > force || newVelocity.length() > force) { // Don't speed up after reaching speed else
            newVelocity.normalize().multiply(currentLength);
            if (entity != null && entity.getLocation().distanceSquared(getLocation()) <= 1) {
                entity.setFallDistance(0);
            }
        }

        return newVelocity;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setForce(double force) {
        this.force = force;
    }

    public void setIgnoreCaster(boolean ignoreCaster) {
        this.ignoreCaster = ignoreCaster;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setTargetEntities(boolean targetEntities) {
        this.targetEntities = targetEntities;
    }

    public void setTargetProjectiles(boolean targetProjectiles) {
        this.targetProjectiles = targetProjectiles;
    }
}
