package wbs.magic.objects.generics;

import java.util.function.Predicate;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import wbs.magic.WbsMagic;
import wbs.magic.exceptions.MagicObjectExistsException;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;

public abstract class MissileObject extends KinematicMagicObject {

	public MissileObject(Location location, SpellCaster caster, SpellInstance castingSpell) {
		super(location, caster, castingSpell);

		player = caster.getPlayer();
		predicate = SpellCaster.getPredicate(player);
	}

	private final Player player;

	private LivingEntity target = null;
	private double speed;

	// Ray tracing stuff
	private FluidCollisionMode fluidMode = FluidCollisionMode.NEVER;
	private final Predicate<Entity> predicate;
	
	private double agility; // how many radians the missile may turn per tick
	private Vector trajectory, trueAim; // current trajectory, and ideal trajectory
	private Location targetPos;
	private int age = 0;
	private double xToTarget, yToTarget, zToTarget;

	private final int maxAge = 1200;
	
	public LivingEntity getTarget() {
		return target;
	}
	public void setTarget(LivingEntity newTarget) {
		target = newTarget;
	}

	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	public double getAgility() {
		return agility;
	}
	public void setAgility(double agility) {
		this.agility = agility;
	}
	
	public FluidCollisionMode getFluidMode() {
		return fluidMode;
	}
	public void setFluidMode(FluidCollisionMode mode) {
		fluidMode = mode;
	}
	
	public Vector getTrajectory() {
		return trajectory;
	}
	public void setTrajectory(Vector trajectory) {
		this.trajectory = trajectory;
	}
	
	@Override
	public void run() {
		if (timerID != -1) {
			throw new MagicObjectExistsException();
		}
		
		Player player = caster.getPlayer();
		
		timerID = new BukkitRunnable() {
			boolean cancel = false;
			
			@Override
	        public void run() {
				if (!player.isOnline()) {
					remove(true);
					return;
				}
				
				age++;
				if (age > maxAge) {
					timedOut();
					remove(true);
				}
				
				if (target.isDead()) {
					targetDead();
				}
				
				targetPos = target.getEyeLocation();
				
				reAim();
				move();
				cancel = tick();

				if (cancel || !active) {
					remove(true);
					return;
				}
				
				if (!target.isDead() && location.distance(targetPos) < 1) {
					hit();
					
					remove(true);
				}
	        }
	    }.runTaskTimer(plugin, 0L, 1L).getTaskId();
	}
	
	/**
	 * Called when the missile hit a block before it hit its target
	 */
	public void hitBlock() {
		remove(true);
	}

	/**
	 * Called every tick by the missile
	 */
	public void hit() {
		
	}
	
	/**
	 * Called when the missile target is dead
	 */
	public void targetDead() {
		
	}

	/**
	 * Called when the missile reached its max age
	 */
	public void timedOut() {
		
	}
	
	
	private void reAim() {
		if (target.isDead()) {
			return;
		}
		
		double instanceAgility = agility;
		/*
		 *  Magic missiles can be stuck going in a circle around targets when they're standing still.
		 *  By having a small chance to move more sharply, the circle is
		 *  less likely to last for a long time.
		 */
		if (chance(1)) {
			instanceAgility *= 2;
		}

		xToTarget = targetPos.getX() - location.getX();
		yToTarget = targetPos.getY() - location.getY();
		zToTarget = targetPos.getZ() - location.getZ();
		trueAim = new Vector(xToTarget, yToTarget, zToTarget);
		
		Vector scaledAim = scaleVector(trueAim, speed);
		double angle = scaledAim.angle(trajectory);
		if (angle < instanceAgility || Double.isNaN(angle)) {
			trajectory = scaleVector(trueAim, speed);
		} else {
			double farAngle = (Math.PI - angle)/2; // Angle between trajectory and (trueAim - trajectory), after scaling trueAim to speed.
			double newAngle = Math.PI - instanceAgility - farAngle; // Pi radians in a triangle so calculate difference
			double addLength = Math.sin(instanceAgility) * speed / Math.min(Math.sin(newAngle), 0.5);
			Vector addVec = scaleVector(scaledAim.subtract(trajectory), addLength);
			trajectory = trajectory.add(addVec);
			trajectory = scaleVector(trajectory, speed);
		}
	}

	private void move() {
		Vector defaultTrajectory = trajectory.clone();
		
		if (target.isDead()) {
			RayTraceResult traceResult = world.rayTrace(location, defaultTrajectory, defaultTrajectory.length(), fluidMode, false, 0, predicate);

			if (traceResult != null) {
				if (traceResult.getHitBlock() != null) {
					hitBlock();
				}
			}

			location.add(defaultTrajectory);
			return;
		}
		
		int attempts = 10;
		int foresight = 3;
		Vector escapeVector = null;
		while (escapeVector == null && foresight > 0) {
			escapeVector = getEscapeVector(foresight, attempts);
			foresight -= 1;
		}
		if (escapeVector == null) { // No escape was found; will collide this turn
			location.add(scaleVector(defaultTrajectory, speed));
			hitBlock();
		} else { // An escape was found; use the escape vector
			location.add(escapeVector); // Go the found way
			trajectory = escapeVector;
		}
	}
	
	private Vector getEscapeVector(int foresight, int attempts) {
		Vector defaultTrajectory = trajectory.clone();
		Vector escapeVector = defaultTrajectory;
		World world = location.getWorld();

		// World was unloaded; destroy this object
		if (world == null) {
			WbsMagic.getInstance().getLogger().info("A world was unloaded unexepectedly; a missile was deleted.");
			remove(true);
			return null;
		}

		RayTraceResult traceResult;
		boolean hit = false;
		int i = 0;
		
		do {
			i++;
			// This will be null if the location is valid
			traceResult = world.rayTrace(location, escapeVector, speed*foresight, fluidMode, false, 0, predicate);
			
			if (traceResult != null) {
				
				if (traceResult.getHitBlock() == null) { // Entity was hit
					hit = true;
				} else {
					// Get a random vector with bias towards forwards
					escapeVector = scaleVector(defaultTrajectory.clone().add(randomVector(speed)), speed);
				}
			}
			
			
		} while (traceResult != null && i < attempts && !hit); // While the raycast would hit a block
		
		if (traceResult == null) {
			return escapeVector;
		}
		
		if (i == attempts) {
			return null;
		}
		
		return escapeVector;
	}
}
