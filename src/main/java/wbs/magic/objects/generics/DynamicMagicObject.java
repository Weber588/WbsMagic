package wbs.magic.objects.generics;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.magic.SpellCaster;
import wbs.magic.WbsMagic;
import wbs.magic.events.objects.DynamicObjectBounceEvent;
import wbs.magic.events.objects.DynamicObjectPhysicsEvent;
import wbs.magic.events.objects.MagicObjectMoveEvent;
import wbs.magic.spells.SpellInstance;
import wbs.utils.util.WbsMath;

import java.util.Objects;
import java.util.function.Predicate;

public abstract class DynamicMagicObject extends KinematicMagicObject {

    // Distance per tick
    @NotNull
    private Vector velocity = new Vector();
    // Acceleration per tick
    @NotNull
    private Vector acceleration = new Vector();

    @NotNull
    private final Vector gravity = new Vector(0, 0, 0);

    private boolean doCollisions = true;
    @NotNull
    private FluidCollisionMode fluidCollisionMode = FluidCollisionMode.NEVER;

    private boolean doBounces = false;
    private int maxBounces = Integer.MAX_VALUE;
    private int currentBounces = 0;

    private boolean hitEntities = true;
    private Predicate<Entity> entityPredicate = entity -> true;
    private double hitBoxSize = 0;

    public DynamicMagicObject(Location location, SpellCaster caster, SpellInstance castingSpell) {
        super(location, caster, castingSpell);
    }

    // ================================== //
    //           Step Management          //
    // ================================== //

    private int stepsTaken = 0;

    // How many steps per tick on average
    private double stepsPerTick = 1;

    // How much error has been introduced so far by using int instead of double
    private double error = 0;
    // How much error increases by every step
    private double errorPerStep;

    @Override
    protected void onRun() {
        super.onRun();
    }

    @Override
    protected final boolean tick() {
        error += errorPerStep;

        // How many steps run every tick before considering error
        int baseStepsPerTick = (int) stepsPerTick;
        errorPerStep = stepsPerTick - baseStepsPerTick;

        int stepsThisTick = baseStepsPerTick;
        if (error >= 1) {
            stepsThisTick+= 1;
            error -= 1;
        }

        boolean cancel;
        Vector accelerationPerStep = perStep(acceleration.clone());
        for (int step = 0; step < stepsThisTick; step++) {
            stepsTaken++;

            applyPhysics(accelerationPerStep);
            cancel = move();
            cancel |= step(step, stepsThisTick);

            if (cancel) return true;
        }
        acceleration.multiply(0);

        return false;
    }

    /**
     * Runs each physics step.
     * @param step Which step this is on the current tick.
     * @param stepsThisTick How many steps will run in the current tick
     * @return Whether or not to cancel. True to prevent future steps,
     * and expire the magic object
     */
    protected boolean step(int step, int stepsThisTick) {
        return false;
    }

    // ================================== //
    //          Movement/Physics          //
    // ================================== //

    /**
     * Called stepsPerTick times per tick.
     */
    private boolean move() {
        boolean cancel = beforeMove();

        Vector velocityThisStep = perStep(velocity);
        Location newLocation;

        if (doCollisions) {
            RayTraceResult result;

            if (hitEntities) {
                result = world.rayTraceBlocks(
                                getLocation(),
                                velocity,
                                velocityThisStep.length(),
                                fluidCollisionMode,
                                true
                        );
            } else {
                result = world.rayTrace(
                        getLocation(),
                        velocity,
                        velocityThisStep.length(),
                        fluidCollisionMode,
                        true,
                        hitBoxSize,
                        entityPredicate
                );
            }

            if (result == null) {
                newLocation = getLocation().add(velocityThisStep);
            } else {
                Location hitLocation = result.getHitPosition().toLocation(world);
                if (result.getHitBlock() != null) {
                    Block hitBlock = Objects.requireNonNull(result.getHitBlock());
                    BlockFace face = Objects.requireNonNull(result.getHitBlockFace());

                    if (doBounces) {

                        if (currentBounces < maxBounces) {
                            currentBounces++;

                            DynamicObjectBounceEvent event = new DynamicObjectBounceEvent(this, hitLocation, face.getDirection());

                            Bukkit.getPluginManager().callEvent(event);

                            if (event.isCancelled()) {
                                newLocation = getLocation().add(velocityThisStep);
                                cancel |= hitBlock(hitLocation, hitBlock, face);
                            } else {
                                velocity = WbsMath.reflectVector(velocity, face.getDirection());
                                velocityThisStep = WbsMath.reflectVector(velocityThisStep, face.getDirection());

                                double distanceToBounce = hitLocation.distance(getLocation());
                                double distanceLeft = velocityThisStep.length() - distanceToBounce;

                                velocityThisStep = WbsMath.scaleVector(velocityThisStep, distanceLeft);

                                newLocation = getLocation().add(velocityThisStep);

                                onBounce();
                            }
                        } else {
                            newLocation = getLocation().add(velocityThisStep);
                            cancel |= hitBlock(hitLocation, hitBlock, face);
                        }
                    } else {
                        newLocation = getLocation().add(velocityThisStep);
                        cancel |= hitBlock(hitLocation, hitBlock, face);
                    }
                } else {
                    newLocation = getLocation().add(velocityThisStep);
                }

                if (result.getHitEntity() != null) {
                    cancel |= hitEntity(hitLocation, (LivingEntity) result.getHitEntity());
                }
            }
        } else {
            newLocation = getLocation().add(velocityThisStep);
        }

        MagicObjectMoveEvent event = new MagicObjectMoveEvent(this, newLocation);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return cancel;

        setLocation(event.getNewLocation());

        cancel |= afterMove();

        return cancel;
    }

    /**
     * Make this object bounce off a plane defined by
     * the provided normal
     * @param normal The normal representing the plane to bounce off
     * @return Whether or not the bounce was successful.
     */
    public boolean bounce(Vector normal) {
        DynamicObjectBounceEvent event = new DynamicObjectBounceEvent(this, getLocation(), normal);

        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            setDirection(WbsMath.reflectVector(velocity, normal));

            onBounce();
        }

        return !event.isCancelled();
    }

    /**
     * Called stepsPerTick times per tick.
     * Calculates velocity and decreases acceleration.
     */
    private void applyPhysics(Vector accelerationThisStep) {
        DynamicObjectPhysicsEvent event = new DynamicObjectPhysicsEvent(this, accelerationThisStep);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        accelerationThisStep = event.getAccelerationThisStep();

        velocity.add(accelerationThisStep);
        velocity.add(perStep(gravity));
    }

    /**
     * Called before moving each step
     * @return True to make the object expire
     */
    protected boolean beforeMove() {
        return false;
    }

    /**
     * Called after moving each step
     * @return True to make the object expire
     */
    protected boolean afterMove() {
        return false;
    }

    protected void onBounce() {

    }

    /**
     * @return Whether or not to expire. True to make the object expire
     */
    protected boolean hitBlock(Location hitLocation, Block hitBlock, BlockFace hitFace) {
        return false;
    }

    /**
     * @return Whether or not to expire. True to make the object expire
     */
    protected boolean hitEntity(Location hitLocation, LivingEntity hitEntity) {
        return false;
    }

    // ================================== //
    //           Utility Methods          //
    // ================================== //

    public void applyForce(Vector acceleration) {
        this.acceleration.add(acceleration);
    }

    protected Vector perStep(Vector perTicks) {
        return perTicks.clone().multiply(1 / stepsPerTick);
    }

    // ================================== //
    //           Getters/Setters          //
    // ================================== //

    public Vector getVelocity() {
        return velocity.clone();
    }

    public DynamicMagicObject setVelocity(Vector velocityInTicks) {
        velocity = velocityInTicks;
        return this;
    }
    public DynamicMagicObject setVelocityInSeconds(Vector velocityInSeconds) {
        velocity = velocityInSeconds.clone().multiply(0.05);
        return this;
    }

    public DynamicMagicObject setSpeed(double speedInTicks) {
        velocity = scaleVector(velocity, speedInTicks);
        return this;
    }

    public DynamicMagicObject setSpeedInSeconds(double speedInSeconds) {
        velocity = scaleVector(velocity, speedInSeconds / 20);
        return this;
    }

    public DynamicMagicObject setDirection(Vector direction) {
        velocity = WbsMath.scaleVector(direction.clone(), velocity.length());
        return this;
    }

    public Vector getAcceleration() {
        return acceleration.clone();
    }

    public DynamicMagicObject setAcceleration(Vector acceleration) {
        this.acceleration = acceleration;
        return this;
    }

    public double getStepsPerTick() {
        return stepsPerTick;
    }

    /**
     * Set how many times this object calculates steps
     * in a tick (on average).
     * @param stepsPerTick The number of times to calculate
     *                     physics per tick on average.
     */
    public DynamicMagicObject setStepsPerTick(double stepsPerTick) {
        this.stepsPerTick = stepsPerTick;
        return this;
    }

    public DynamicMagicObject setStepsPerSecond(double stepsPerSecond) {
        this.stepsPerTick = stepsPerSecond / 20;
        return this;
    }

    public double getGravity() {
        return gravity.getY();
    }

    public DynamicMagicObject setGravity(double gravityPerTick) {
        gravity.setY(-gravityPerTick);
        return this;
    }

    public DynamicMagicObject setGravityInSeconds(double gravityPerSecond) {
        gravity.setY(-gravityPerSecond / 20);
        return this;
    }

    public boolean doCollisions() {
        return doCollisions;
    }

    public DynamicMagicObject setDoCollisions(boolean doCollisions) {
        this.doCollisions = doCollisions;
        return this;
    }

    @NotNull
    public FluidCollisionMode getFluidCollisionMode() {
        return fluidCollisionMode;
    }

    public DynamicMagicObject setFluidCollisionMode(@NotNull FluidCollisionMode fluidCollisionMode) {
        this.fluidCollisionMode = fluidCollisionMode;
        return this;
    }

    public boolean doBounces() {
        return doBounces;
    }

    public DynamicMagicObject setDoBounces(boolean doBounces) {
        this.doBounces = doBounces;
        return this;
    }

    public int getMaxBounces() {
        return maxBounces;
    }

    public DynamicMagicObject setMaxBounces(int maxBounces) {
        this.maxBounces = maxBounces;
        return this;
    }

    public boolean isDoBounces() {
        return doBounces;
    }

    public boolean isHitEntities() {
        return hitEntities;
    }

    public DynamicMagicObject setHitEntities(boolean hitEntities) {
        this.hitEntities = hitEntities;
        return this;
    }

    public Predicate<Entity> getEntityPredicate() {
        return entityPredicate;
    }

    public DynamicMagicObject setEntityPredicate(Predicate<Entity> entityPredicate) {
        this.entityPredicate = entityPredicate;
        return this;
    }

    public double getHitBoxSize() {
        return hitBoxSize;
    }

    public DynamicMagicObject setHitBoxSize(double hitBoxSize) {
        this.hitBoxSize = hitBoxSize;
        return this;
    }

    public int stepsTaken() {
        return stepsTaken;
    }
}
