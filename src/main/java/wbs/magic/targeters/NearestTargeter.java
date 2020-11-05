package wbs.magic.targeters;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import wbs.magic.wrappers.SpellCaster;

public class NearestTargeter extends GenericTargeter {

	public static double DEFAULT_RANGE = 5;
	private double range = DEFAULT_RANGE;
	
	public NearestTargeter() {
		
	}
	
	public NearestTargeter(double range) {
		if (range >= 0) {
			this.range = range;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends LivingEntity> Set<T> getTargets(SpellCaster caster, Class<T> clazz) {
		Set<T> targets = new HashSet<>();
		Location location = caster.getLocation();

		Set<LivingEntity> entities;
		entities = caster.getNearbyLiving(range, false);
		
		T target = null;
		
		double closest = (range+1);
		for (LivingEntity entity : entities) {
			if (clazz.isInstance(entity)) {
				double distance = entity.getLocation().distance(location);
				if (target == null || distance < closest) {
					closest = distance;
					target = (T) entity;
				}
			}
		}
		
		if (target != null) {
			targets.add(target);
		}
		
		return targets;
	}

	@Override
	public double getRange() {
		return range;
	}
	
	@Override
	public void sendFailMessage(SpellCaster caster) {
		caster.sendActionBar(TargeterType.NEAREST.getFailMessage());
	}
	
	@Override
	public String toString() {
		return "Closest (in radius " + range + ")"; 
	}
}

