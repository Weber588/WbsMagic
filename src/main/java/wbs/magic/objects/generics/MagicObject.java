package wbs.magic.objects.generics;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
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
		// TODO Change WbsMagic to not be static bc gross
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
		for (MagicObject object : activeObjects.values()) {
			if (object.getLocation().distance(location) <= distance) {
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
	public SpellInstance castingSpell = null;
	
	public MagicObject(Location location, SpellCaster caster, SpellInstance castingSpell) {
		this.spawnLocation = location;
		this.caster = caster;
		this.castingSpell = castingSpell;
		world = location.getWorld();
		
		activeObjects.put(caster.getUUID(), this);
	}

	public World world;
	protected boolean isExpired = false;
	protected boolean isPersistent = false; // Persistent objects are immune to some removal effects (such as Negate Magic)
	
	protected int timerID = -1; // The ID of the runnable
	
	protected WbsParticleGroup effects = null; // The vast majority of magicobjects will use particles
	protected WbsParticleGroup fizzleEffects = null;
	
	public void run() {
		if (timerID != -1) {
			throw new MagicObjectExistsException();
		}
		
		timerID = new BukkitRunnable() {
			boolean cancel = false;
			@Override
	        public void run() {
				cancel = tick();
				
				if (cancel || isExpired) {
					fizzle();
				}
	        }
	    }.runTaskTimer(plugin, 0L, 1L).getTaskId();
	}
	
	/**
	 * Called every tick by the magic object
	 * @return Whether or not to cancel. True to make the object expire.
	 */
	protected abstract boolean tick();
	
	/**
	 * A method that gets called when the object needs to be removed
	 */
	public final void fizzle() {
		
	//	plugin.broadcast("Fizzling ID " + timerID);
		
		isExpired = true;
		activeObjects.remove(caster.getUUID(), this);
		if (timerID != -1) {
			Bukkit.getScheduler().cancelTask(timerID);
		}
		
		if (fizzleEffects != null) {
			fizzleEffects.play(getLocation());
		}
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
		return spawnLocation;
	}
	
	// This method can be overridden by extending classes
	public Location getLocation() {
		return spawnLocation;
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
}
