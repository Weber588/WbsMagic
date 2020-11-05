package wbs.magic.objects.generics;

import java.util.function.Predicate;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import wbs.magic.exceptions.MagicObjectExistsException;
import wbs.magic.spellinstances.SpellInstance;
import wbs.magic.spellinstances.ranged.projectile.ProjectileSpell;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.WbsMath;
import wbs.utils.util.WbsSoundGroup;

public abstract class ProjectileObject extends DynamicMagicObject {

	public LivingEntity hitEntity = null;
	public Location hitLocation = null;
	public int step = 0;
	
	protected double speed = 25; // in blocks per second
	
	protected double hitbox = 0.5;
	protected double range = 60;
	protected Vector fireDirection;
	
	protected double speedInTicks = speed / 20; // Makes math easier
	protected double stepSize = 0.5; // The distance to travel every time tick() is called.
	
	protected WbsSoundGroup hitSound;

	public ProjectileObject(Location location, SpellCaster caster, SpellInstance castingSpell) {
		super(location, caster, castingSpell);
	}
	
	private Vector gravity = new Vector(0, 0, 0);
	
	@Override
	public final void run() {
		if (timerID != -1) {
			throw new MagicObjectExistsException();
		}
		
		speedInTicks = speed / 20;
		
		boolean hasGravity = false;
		if (gravity.length() != 0) {
			hasGravity = true;
		}
		final boolean useGravity = hasGravity;
		
		location = spawnLocation.clone();
		timerID = new BukkitRunnable() {
			final FluidCollisionMode fluidMode = FluidCollisionMode.NEVER;
			final Predicate<Entity> predicate = caster.getPredicate();

			final Vector direction = scaleVector(fireDirection, stepSize);
			
			final Vector localGravity = gravity.clone().multiply(1 / (speedInTicks/stepSize));
			
			boolean cancel = false;
			@Override
	        public void run() {
				for (int i = 0; i < speedInTicks/stepSize; i++) {
					if (!cancel) {
						
						step++;

						RayTraceResult traceResult = world.rayTrace(location, direction, stepSize, fluidMode, true, hitbox, predicate);
	
						if (traceResult != null) { // If something was hit
							Vector hitPos = traceResult.getHitPosition();
							
							Entity hit = traceResult.getHitEntity();
							if (hit != null) { // Hit an entity
								// Predicate ensures safe cast
								hitEntity = (LivingEntity) hit;
								hitLocation = new Location(world, hitPos.getX(), hitPos.getY(), hitPos.getZ());
								cancel = cancel || hitEntity();
							} else { // Block was hit
								hitLocation = new Location(world, hitPos.getX(), hitPos.getY(), hitPos.getZ());
								cancel = cancel || hitBlock();
							}
						}
						location.add(direction);

						if (useGravity) {
							direction.add(localGravity);
						}
						
						cancel = cancel || tick();
						
						if (location.distance(spawnLocation) > range) {
							cancel = true;
							maxDistanceReached();
						}
						
						if (cancel || isExpired) {
							fizzle();
						}
					}
				}
	        }
	    }.runTaskTimer(plugin, 0L, 1L).getTaskId();
	    
	//    plugin.broadcast("timerID = " + timerID);
	}
	
	/**
	 * Called when the projectile has reached its max distance from its spawnLocation.
	 * When this is called, the projectile is already cancelled
	 */
	public void maxDistanceReached() {
		
	}
	
	/**
	 * Called when the projectile hits an entity
	 * @return Whether or not to cancel. True to make the projectile expire.
	 */
	public boolean hitEntity() {
		
		return false;
	}
	
	/**
	 * Called when the projectile hits a block
	 * @return Whether or not to cancel. True to make the projectile expire.
	 */
	public boolean hitBlock() {

		return false;
	}
	
	// Configure universal options from spell
	public ProjectileObject configure(ProjectileSpell spell) {
		castingSpell = spell;
		hitbox = spell.getHitbox();
		range = spell.getRange();
		speed = spell.getSpeed();
		stepSize = spell.getStepSize();

		if (spell.getGravity()/400 != 0) { // 400 because gravity is acceleration/second^2, so scaling per ticks is 20^2
			gravity = new Vector(0, -1, 0);
			gravity = WbsMath.scaleVector(gravity, spell.getGravity()/400);
		}
		
		return this;
	}
	
	
	// Getters and setters
	public ProjectileObject setSpeed(double speed) {
		this.speed = speed;
		return this;
	}
	public double getSpeed() {
		return speed;
	}
	
	/**
	 * Set the gravity of this projectile (default 0)
	 * @param gravity The gravity in blocks/s^2
	 */
	public ProjectileObject setGravity(double gravity) {
		this.gravity = WbsMath.scaleVector(this.gravity, gravity/400);
		return this;
	}
	public double getGravity() {
		return gravity.length() * 400;
	}
	
	public ProjectileObject setStepSize(double stepSize) {
		this.stepSize = stepSize;
		return this;
	}
	public double getStepSize() {
		return stepSize;
	}
	
	public ProjectileObject setHitbox(double hitbox) {
		this.hitbox = hitbox;
		return this;
	}
	public double getHitbox() {
		return hitbox;
	}

	public ProjectileObject setRange(double range) {
		this.range = range;
		return this;
	}
	public double getRange() {
		return range;
	}
	
	public ProjectileObject setFireDirection(Vector fireDirection) {
		this.fireDirection = fireDirection;
		return this;
	}
	public Vector getFireDirection() {
		return fireDirection;
	}
	
	public ProjectileObject setHitSound(WbsSoundGroup hitSound) {
		this.hitSound = hitSound;
		return this;
	}
}
