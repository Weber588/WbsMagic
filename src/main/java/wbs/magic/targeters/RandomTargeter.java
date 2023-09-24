package wbs.magic.targeters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import wbs.magic.SpellCaster;

public class RandomTargeter extends GenericTargeter {

	public static double DEFAULT_RANGE = 5;
	private double range = DEFAULT_RANGE;
	
	public RandomTargeter() {}

	public RandomTargeter(double range) {
		if (range >= 0) {
			this.range = range;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Entity> Set<T> getTargets(SpellCaster caster, Class<T> clazz) {
		Set<T> targets = new HashSet<>();
		Set<LivingEntity> entities = caster.getNearbyLiving(range, false);
		if (entities.isEmpty()) {
			return targets;
		}

		Predicate<Entity> predicate = getPredicate(caster, clazz);

		ArrayList<T> validTargets = new ArrayList<>();
		for (LivingEntity entity : entities) {
			if (!predicate.test(entity)) continue;

			validTargets.add((T) entity);
		}

		if (validTargets.isEmpty()) {
			return targets;
		}
		
		int entityCount = validTargets.size();
		int index = (int) (Math.floor(Math.random()*entityCount));
		targets.add(validTargets.get(index));
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
		return "Random (in radius " + range + ")"; 
	}
}
