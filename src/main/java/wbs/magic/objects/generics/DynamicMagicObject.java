package wbs.magic.objects.generics;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import wbs.magic.exceptions.MagicObjectExistsException;
import wbs.magic.spellinstances.SpellInstance;
import wbs.magic.wrappers.SpellCaster;

public abstract class DynamicMagicObject extends MagicObject {
	
	public Location location; // The location the object is currently at

	public DynamicMagicObject(Location location, SpellCaster caster, SpellInstance castingSpell) {
		super(location, caster, castingSpell);
		this.location = location;
	}
	
	@Override
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
	
	@Override
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}

}
