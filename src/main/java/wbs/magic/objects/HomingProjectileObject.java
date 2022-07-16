package wbs.magic.objects;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import wbs.magic.SpellCaster;
import wbs.magic.WbsMagic;
import wbs.magic.events.objects.MagicObjectMoveEvent;
import wbs.magic.objects.colliders.Collision;
import wbs.magic.spells.SpellInstance;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.selector.RadiusSelector;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class HomingProjectileObject extends MagicEntityEffect {
    public HomingProjectileObject(Entity entity, SpellCaster caster, SpellInstance castingSpell) {
        super(entity, caster, castingSpell);

        setMoveEntityWithObject(false);
        setExpireOnDeath(true);
        setRemoveOnExpire(false);

        selector.exclude(caster.getPlayer());
        setTargetPredicate(SpellInstance.VALID_TARGETS_PREDICATE);
    }

    private int updateRate = 1;
    private double anglePerTick = 3; // 60 degrees per second
    private final RadiusSelector<Entity> selector =
            new RadiusSelector<>(Entity.class);

    private double lifetimeAngle = 180;
    private double angleSoFar = 0;
    private double homingRange = 10;

    @Override
    protected boolean tick() {
        boolean expire = super.tick();
        if (expire) {
            return true;
        }

        if (getEntity() instanceof AbstractArrow) {
            AbstractArrow arrow = (AbstractArrow) getEntity();
            if (arrow.isInBlock()) return true;
        }

        if (getAge() % updateRate == 0) {
            List<Entity> possibleTargets = selector.select(getLocation());

            Vector velocity = getEntity().getVelocity();
            Optional<Entity> target = possibleTargets.stream().min(
                    Comparator.comparingDouble(check -> {
                        Vector toTarget = getVectorToTarget(check);
                        return Math.abs(toTarget.angle(velocity));
                    })
            );

            if (target.isPresent()) {
                return moveTowards(target.get());
            }
        }

        return false;
    }

    /**
     * Change direction, but not speed, to try to hit the target.
     * To avoid slowly moving upwards and ignoring gravity effects,
     * we restrict this to only have control over XZ direction, or Y
     * direction if it's moving down (i.e. to prevent going over an entity's head)
     * @param target The entity to try to hit
     * @return Whether or not tracking can continue.
     */
    private boolean moveTowards(Entity target) {
        Vector velocity = getEntity().getVelocity();

        Vector projToTarget = getVectorToTarget(target);

        double resultY = projToTarget.getY();
        // If proj is below target, assume it's on the same level - don't fly upwards
        if (resultY > 0) {
            projToTarget.setY(0);
        }

        Vector perp = velocity.getCrossProduct(projToTarget);

        double angleToRotate = Math.toDegrees(velocity.angle(projToTarget));
        angleToRotate = Math.min(angleToRotate, anglePerTick);

        Vector newVelocity = WbsMath.rotateVector(velocity, perp, angleToRotate);

        // Calculate how much the arrow will tilt downwards by the time it reaches
        // the target, and prevent it looking down unless needed (distance / speed)
        // (assuming a straight line - technically could turn more since a curved path
        // is longer, but calculation only needs to be approximate
        double timeToHit = projToTarget.length() / velocity.length();
        final double GRAV_PER_TICK = 0.05;

        double yChangeBeforeHit = timeToHit * GRAV_PER_TICK;

        // Correct for horizontal angle, to prevent it from angling up and floating
        double xzAngle = Math.asin(velocity.getY() / velocity.length());
        double xzLength = Math.sqrt(newVelocity.getX() * newVelocity.getX() + newVelocity.getZ() * newVelocity.getZ());
        double newY = Math.min(newVelocity.getY() + yChangeBeforeHit, Math.tan(xzAngle) * xzLength);
        newVelocity.setY(newY);

        getEntity().setVelocity(newVelocity);

    //    angleSoFar += angleToRotate;

        return angleSoFar >= lifetimeAngle;
    }

    private Vector getVectorToTarget(Entity target) {
        if (target instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) target;
            return livingTarget.getEyeLocation().subtract(getLocation()).toVector();
        } else {
            return target.getLocation().subtract(getLocation()).toVector();
        }
    }

    @Override
    public void onCollide(MagicObjectMoveEvent moveEvent, Collision collision) {
        super.onCollide(moveEvent, collision);
        this.remove(true); // removeOnExpire is set to false - this just disables homing.
    }

    public void setHomingRange(double homingRange) {
        this.homingRange = homingRange;
        selector.setRange(homingRange);
    }

    public void setTargetPredicate(Predicate<Entity> targetPredicate) {
        selector.setPredicate(targetPredicate.and(this::canTarget));
    }

    private boolean canTarget(Entity target) {
        if (target == caster.getPlayer()) return false;
        if (target == getEntity()) return false;
        Vector toEntity = getVectorToTarget(target);

        // Check if the projectile has line of sight to the target
        RayTraceResult traceResult = getEntity().getWorld().rayTrace(getLocation(), toEntity, homingRange, FluidCollisionMode.NEVER, true, 0, check -> check.equals(target));

        if (traceResult == null) return true;

        return traceResult.getHitBlock() == null;
    }

    public void setUpdateRate(int updateRate) {
        this.updateRate = updateRate;
    }

    public void setAnglePerTick(double anglePerTick) {
        this.anglePerTick = anglePerTick;
    }

    public void setLifetimeAngle(double lifetimeAngle) {
        this.lifetimeAngle = lifetimeAngle;
    }
}
