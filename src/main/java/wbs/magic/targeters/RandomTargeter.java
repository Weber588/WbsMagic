package wbs.magic.targeters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.LivingEntity;

import wbs.magic.wrappers.SpellCaster;

public class RandomTargeter extends GenericTargeter {

	public static double DEFAULT_RANGE = 5;
	private double range = DEFAULT_RANGE;
	
	public RandomTargeter(double range) {
		if (range >= 0) {
			this.range = range;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends LivingEntity> Set<T> getTargets(SpellCaster caster, Class<T> clazz) {
		Set<T> targets = new HashSet<>();
		Set<LivingEntity> entities = caster.getNearbyLiving(range, false);
		if (entities.isEmpty()) {
			return targets;
		}

		ArrayList<T> validTargets = new ArrayList<>();
		for (LivingEntity entity : entities) {
			if (!clazz.isInstance(entity)) {
				validTargets.add((T) entity);
			}
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
	public void sendFailMessage(SpellCaster caster) {
		caster.sendActionBar(TargeterType.RANDOM.getFailMessage());
	}
	
	@Override
	public String toString() {
		return "Random (in radius " + range + ")"; 
	}
}
