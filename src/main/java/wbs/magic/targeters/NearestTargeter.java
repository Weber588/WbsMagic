package wbs.magic.targeters;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import wbs.magic.SpellCaster;

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
	public <T extends Entity> Set<T> getTargets(SpellCaster caster, Class<T> clazz) {
		Set<T> targets = new HashSet<>();
		Location location = caster.getLocation();

		Set<LivingEntity> entities;
		entities = caster.getNearbyLiving(range, false);
		
		T target = null;

		Predicate<Entity> predicate = getPredicate(caster, clazz);

		double closest = (range+1);
		for (LivingEntity entity : entities) {
			if (!predicate.test(entity)) continue;

			double distance = entity.getLocation().distance(location);
			if (target == null || distance < closest) {
				closest = distance;
				target = (T) entity;
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
	public String getNoTargetsMessage() {
		return "No valid targets in range!";
	}

	@Override
	public String toString() {
		return "Closest (in radius " + range + ")"; 
	}
}

