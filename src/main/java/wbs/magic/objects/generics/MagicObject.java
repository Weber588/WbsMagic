package wbs.magic.objects.generics;

import java.util.*;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.WbsMagic;
import wbs.magic.events.objects.MagicObjectSpawnEvent;
import wbs.magic.exceptions.MagicObjectExistsException;
import wbs.magic.objects.AlignmentType;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.objects.PersistenceLevel;
import wbs.magic.objects.colliders.Collider;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;

import wbs.utils.util.WbsMath;
import wbs.utils.util.particles.WbsParticleGroup;

public abstract class MagicObject {
	/**
	   * Configure a spell from a config section
	   * @param error The error message to be displayed
	   * @param directory The directory in the errored config
	   */
	protected static void logError(String error, String directory) {
		// errors.add("&c" + error + " &7(" + directory + ")");
		logger.warning(error + "(" + directory + ")");
	}

	protected static Logger logger;
	protected static WbsMagic plugin;
	public static void setPlugin(WbsMagic plugin) {
		MagicObject.plugin = plugin;
		logger = plugin.getLogger();
	}
	
	private static final Multimap<UUID, MagicObject> activeObjects = HashMultimap.create();
	public static Collection<MagicObject> getAllActive() {
		return activeObjects.values();
	}
	
	public static Collection<MagicObject> getAllActive(SpellCaster caster) {
		return activeObjects.get(caster.getUUID());
	}
	
	public static List<MagicObject> getNearbyActive(Location location, double distance) {
		List<MagicObject> nearby = new LinkedList<>();
		double distanceSquared = distance * distance;
		for (MagicObject object : activeObjects.values()) {
			Location objLocation = object.getLocation();
			if (!object.world.equals(location.getWorld())) continue;
			if (objLocation.distanceSquared(location) <= distanceSquared) {
				nearby.add(object);
			}
		}
		return nearby;
	}
	
	public static List<MagicObject> getNearbyActive(Location location, double distance, SpellCaster caster) {
		List<MagicObject> nearby = new LinkedList<>();
		for (MagicObject object : activeObjects.get(caster.getUUID())) {
			if (object.getLocation().distance(location) <= distance) {
				nearby.add(object);
			}
		}
		return nearby;
	}

	public static List<MagicEntityEffect> getActiveEffects(Entity entity) {
		List<MagicEntityEffect> effects = new LinkedList<>();

		for (MagicObject obj : activeObjects.values()) {
			if (obj instanceof MagicEntityEffect) {
				MagicEntityEffect effect = (MagicEntityEffect) obj;
				if (effect.getEntity().equals(entity)) {
					effects.add(effect);
				}
			}
		}

		return effects;
	}
	
	
	
	
	
	
	
	

	public Location spawnLocation; // The spawn location; should never change. To move, use DynamicMagicObject
	public SpellCaster caster;
	@NotNull
	public SpellInstance castingSpell;
	private AlignmentType alignmentType;
	
	public MagicObject(Location location, SpellCaster caster, @NotNull SpellInstance castingSpell) {
		this.spawnLocation = location;
		this.caster = caster;
		this.castingSpell = castingSpell;
		world = Objects.requireNonNull(location.getWorld());

		alignmentType = castingSpell.getAlignmentType();
		
		activeObjects.put(caster.getUUID(), this);
	}

	public World world;
	protected boolean active = true;
	protected boolean isPersistent = false; // Persistent objects are immune to some removal effects (such as Negate Magic)

	protected PersistenceLevel persistenceLevel = PersistenceLevel.WEAK;

	private int age = 0;

	private int maxAge = Integer.MAX_VALUE;

	protected int timerID = -1; // The ID of the runnable

	protected Collider collider;

	@Nullable
	protected WbsParticleGroup effects = null; // The vast majority of magicobjects will use particles
	@Nullable
	protected WbsParticleGroup endEffects = null;
	@Nullable
	protected WbsParticleGroup dispelEffects = null;

	public void run() {
		if (timerID != -1) {
			throw new MagicObjectExistsException();
		}

		MagicObjectSpawnEvent spawnEvent = new MagicObjectSpawnEvent(this);

		Bukkit.getPluginManager().callEvent(spawnEvent);

		if (spawnEvent.isCancelled()) {
			return;
		}

		onRun();

		timerID = new BukkitRunnable() {
			boolean cancel = false;
			@Override
	        public void run() {
				cancel = tick();

				age++;

				if (cancel || !active || age >= maxAge) {
					if (age >= maxAge) {
						onMaxAgeHit();
					}
					remove();
				}
	        }
	    }.runTaskTimer(plugin, 0L, 1L).getTaskId();
	}

	protected void onMaxAgeHit() {

	}

	/**
	 * Called when the object starts running, before the timer is scheduled
	 */
	protected void onRun() {

	}

	/**
	 * Called every tick by the magic object
	 * @return Whether or not to cancel. True to make the object expire.
	 */
	protected abstract boolean tick();

	public final void remove() {
		remove(false);
	}

	/**
	 * Remove this magic object.
	 * @param force If the object is persistent, this must be true to remove it.
	 * @return Whether or not the object was removed.
	 */
	public final boolean remove(boolean force) {
		if (isPersistent && !force) return false;
		if (!active) return false;
		
	//	plugin.broadcast("Fizzling ID " + timerID);
		
		active = false;
		activeObjects.remove(caster.getUUID(), this);
		if (timerID != -1) {
			Bukkit.getScheduler().cancelTask(timerID);
		}
		
		if (endEffects != null) {
			endEffects.play(getLocation());
		}

		if (collider != null) {
			collider.remove();
		}

		onRemove();

		return true;
	}

	/**
	 * Called after this entity is removed and everything else is resolved (including deregistration)
	 */
	protected void onRemove() {

	}

	public boolean dispel(PersistenceLevel persistenceLevel) {
		boolean remove = this.persistenceLevel.ordinal() <= persistenceLevel.ordinal();

		boolean removed = false;
		if (remove) {
			removed = remove(false);
		}

		return removed;
	}

	/**
	 * Returns the distance between this object and another magic object.
	 * Returns {@link Double#POSITIVE_INFINITY} if it's in another world.
	 * @param other The object to measure distance to.
	 * @return The distance between the objects, or {@link Double#POSITIVE_INFINITY}
	 * if they're in different worlds.
	 */
	public double distance(MagicObject other) {
		Location otherLoc = other.getLocation();
		if (otherLoc.getWorld() != world) return Double.POSITIVE_INFINITY;

		return otherLoc.distance(getLocation());
	}

	public boolean isExpired() {
		return !active;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isPersistent() {
		return isPersistent;
	}
	public void setPersistent(boolean isPersistent) {
		this.isPersistent = isPersistent;
	}

	public MagicObject setParticle(WbsParticleGroup effects) {
		this.effects = effects.clone();
		return this;
	}
	
	public MagicObject setEndEffects(WbsParticleGroup endEffects) {
		this.endEffects = endEffects.clone();
		return this;
	}

	public final Location getSpawnLocation() {
		return spawnLocation.clone();
	}
	
	// This method can be overridden by extending classes
	public Location getLocation() {
		return spawnLocation.clone();
	}

	public AlignmentType getAlignmentType() {
		return alignmentType;
	}

	public void setAlignmentType(AlignmentType alignmentType) {
		this.alignmentType = alignmentType;
	}

	//************
	// Math methods
	protected Vector scaleVector(Vector original, double magnitude) {
		return WbsMath.scaleVector(original, magnitude);
	}

	protected boolean chance(double percent) {
		return WbsMath.chance(percent);
	}
	
	protected Vector randomVector(double magnitude) {
		return WbsMath.randomVector(magnitude);
	}

	@NotNull
	public SpellInstance getSpell() {
		return castingSpell;
	}

	public SpellCaster getCaster() {
		return caster;
	}

	public int getAge() {
		return age;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}

	public PersistenceLevel getPersistenceLevel() {
		return persistenceLevel;
	}

	public void setPersistenceLevel(PersistenceLevel persistenceLevel) {
		this.persistenceLevel = persistenceLevel;
	}

	@Nullable
	public Collider getCollider() {
		return collider;
	}

	public void setCollider(@Nullable Collider collider) {
		this.collider = collider;
	}
}
