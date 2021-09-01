package wbs.magic.objects.generics;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import org.bukkit.util.Vector;
import wbs.magic.events.objects.MagicObjectMoveEvent;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;

public abstract class KinematicMagicObject extends MagicObject {
	
	public Location location; // The location the object is currently at

	public KinematicMagicObject(Location location, SpellCaster caster, SpellInstance castingSpell) {
		super(location, caster, castingSpell);
		this.location = location;
	}
	
	@Override
	public Location getLocation() {
		return location.clone();
	}

	/**
	 * Change the location of this object without
	 * triggering a move event
	 * @param location The new location
	 */
	public void setLocation(Location location) {
		this.location = location;

		if (collider == null || collider.getWorld() != world) return;
		Vector offset = collider.getLocation().subtract(getLocation()).toVector();

		collider.setLocation(location.clone().add(offset));
	}

	/**
	 * Attempts to move this object from it's current location
	 * to the provided one, and returns the actual location
	 * it was moved to after an event is fired.
	 * @param location The location to move to
	 * @return The new location
	 */
	public Location move(Location location) {
		MagicObjectMoveEvent moveEvent = new MagicObjectMoveEvent(this, location);

		Bukkit.getPluginManager().callEvent(moveEvent);

		if (moveEvent.isCancelled()) return getLocation();

		Location newLocation = moveEvent.getNewLocation();

		setLocation(newLocation);

		return newLocation;
	}
}
