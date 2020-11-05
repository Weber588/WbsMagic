package wbs.magic.targeters;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.LivingEntity;

import wbs.magic.wrappers.SpellCaster;

public class RadiusTargeter extends GenericTargeter {

	public static double DEFAULT_RANGE = 5;
	private double range = DEFAULT_RANGE;
	
	public RadiusTargeter() {
		
	}
	
	public RadiusTargeter(double range) {
		if (range >= 0) {
			this.range = range;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends LivingEntity> Set<T> getTargets(SpellCaster caster, Class<T> clazz) {
		Set<LivingEntity> nearbyLiving = caster.getNearbyLiving(range, false);
		Set<T> targets = new HashSet<>();
		for (LivingEntity entity : nearbyLiving) {
			if (clazz.isInstance(entity)) {
				targets.add((T) entity);
			}
		}
		
		return targets;
	}

	@Override
	public double getRange() {
		return range;
	}

	@Override
	public void sendFailMessage(SpellCaster caster) {
		caster.sendActionBar(TargeterType.RADIUS.getFailMessage());
	}
	
	@Override
	public String toString() {
		return"Radius (" + range + ")"; 
	}
}
