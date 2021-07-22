package wbs.magic.objects.generics;

import java.util.*;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import wbs.magic.WbsMagic;
import wbs.magic.exceptions.MagicObjectExistsException;
import wbs.magic.spellinstances.SpellInstance;
import wbs.magic.wrappers.SpellCaster;

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
	
	
	
	
	
	
	
	
	
	

	public Location spawnLocation; // The spawn location; should never change. To move, use DynamicMagicObject
	public SpellCaster caster;
	public SpellInstance castingSpell;
	
	public MagicObject(Location location, SpellCaster caster, SpellInstance castingSpell) {
		this.spawnLocation = location;
		this.caster = caster;
		this.castingSpell = castingSpell;
		world = Objects.requireNonNull(location.getWorld());
		
		activeObjects.put(caster.getUUID(), this);
	}

	public World world;
	protected boolean active = true;
	protected boolean isPersistent = false; // Persistent objects are immune to some removal effects (such as Negate Magic)
	
	protected int timerID = -1; // The ID of the runnable
	
	protected WbsParticleGroup effects = null; // The vast majority of magicobjects will use particles
	protected WbsParticleGroup fizzleEffects = null;
	
	public void run() {
		if (timerID != -1) {
			throw new MagicObjectExistsException();
		}

		onRun();

		timerID = new BukkitRunnable() {
			boolean cancel = false;
			@Override
	        public void run() {
				cancel = tick();
				
				if (cancel || !active) {
					remove();
				}
	        }
	    }.runTaskTimer(plugin, 0L, 1L).getTaskId();
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
	 */
	public final void remove(boolean force) {
		if (isPersistent && !force) return;
		if (!active) return;
		
	//	plugin.broadcast("Fizzling ID " + timerID);
		
		active = false;
		activeObjects.remove(caster.getUUID(), this);
		if (timerID != -1) {
			Bukkit.getScheduler().cancelTask(timerID);
		}
		
		if (fizzleEffects != null) {
			fizzleEffects.play(getLocation());
		}

		onRemove();
	}

	protected void onRemove() {

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
	
	public MagicObject setFizzleEffect(WbsParticleGroup fizzleEffects) {
		this.fizzleEffects = fizzleEffects.clone();
		return this;
	}

	public final Location getSpawnLocation() {
		return spawnLocation.clone();
	}
	
	// This method can be overridden by extending classes
	public Location getLocation() {
		return spawnLocation.clone();
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

	public SpellInstance getSpell() {
		return castingSpell;
	}

	public SpellCaster getCaster() {
		return caster;
	}
}
