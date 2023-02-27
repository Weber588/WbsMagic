package wbs.magic.targeters;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import wbs.magic.SpellCaster;
import wbs.magic.spells.SpellInstance;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.entities.selector.RadiusSelector;

public class RadiusTargeter extends GenericTargeter {

	public static double DEFAULT_RANGE = 5;
	private double range = DEFAULT_RANGE;

	private final RadiusSelector<LivingEntity> selector =
			new RadiusSelector<>(LivingEntity.class).setPredicateRaw(SpellInstance.VALID_TARGETS_PREDICATE);
	
	public RadiusTargeter() {
		selector.setRange(range);
	}
	
	public RadiusTargeter(double range) {
		if (range >= 0) {
			this.range = range;
			selector.setRange(range);
		}
	}

	@Override
	public <T extends Entity> Set<T> getTargets(SpellCaster caster, Class<T> clazz) {
		return getTargets(caster, caster.getLocation(), clazz);
	}

	public Set<LivingEntity> getTargets(SpellCaster caster, Location loc) {
		return getTargets(caster, loc, LivingEntity.class);
	}

	public <T extends Entity> Set<T> getTargets(SpellCaster caster, Location loc, Class<T> clazz) {



		Set<T> nearbyLiving = WbsEntities.getNearby(loc, range, clazz);
		Set<T> targets = new HashSet<>();

		Predicate<Entity> predicate = getPredicate(caster, clazz);

		for (T entity : nearbyLiving) {
			if (!predicate.test(entity)) continue;

			targets.add(entity);
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
	public String getNoTargetsMessage() {
		return "No valid targets in range!";
	}

	@Override
	public String toString() {
		return"Radius (" + range + ")"; 
	}
}
