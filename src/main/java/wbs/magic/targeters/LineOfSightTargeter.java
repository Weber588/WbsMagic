package wbs.magic.targeters;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;

import wbs.magic.SpellCaster;

public class LineOfSightTargeter extends GenericTargeter {

	public static double DEFAULT_RANGE = 100;
	private double range = DEFAULT_RANGE;
	
	public LineOfSightTargeter() {
		
	}
	
	public LineOfSightTargeter(double range) {
		if (range >= 0) {
			this.range = range;
		}
	}

	@Override
	public Set<LivingEntity> getTargets(SpellCaster caster) {
		return getTargets(caster, LivingEntity.class);
	}
	
	@Override
	public <T extends Entity> Set<T> getTargets(SpellCaster caster, Class<T> clazz) {
		Set<T> targets = new HashSet<>();

		Location location = caster.getEyeLocation();
		World world = location.getWorld();

		Predicate<Entity> predicate = getPredicate(caster, clazz);
		
		final FluidCollisionMode fluidMode = FluidCollisionMode.NEVER;
		assert world != null;
		RayTraceResult result = world.rayTrace(location, caster.getFacingVector(), range, fluidMode, false, 1, predicate);
		
		if (result == null) {
			return targets;
		} else {
			if (result.getHitEntity() == null) {
				return targets;
			}
		}

		@SuppressWarnings("unchecked")
		T target = (T) result.getHitEntity();
		targets.add(target);
		
		return targets;
	}
	
	public Set<Entity> getTargets(SpellCaster caster, Predicate<Entity> predicate) {
		Set<Entity> targets = new HashSet<>();

		Location location = caster.getEyeLocation();
		World world = location.getWorld();

		final FluidCollisionMode fluidMode = FluidCollisionMode.NEVER;
		assert world != null;
		RayTraceResult result = world.rayTrace(location, caster.getFacingVector(), range, fluidMode, false, 1, predicate);
		
		if (result == null) {
			return targets;
		} else {
			if (result.getHitEntity() == null) {
				return targets;
			}
		}

		LivingEntity target = (LivingEntity) result.getHitEntity();
		targets.add(target);
		
		return targets;
	}

	@Override
	public double getRange() {
		return range;
	}

	@Override
	public void sendFailMessage(SpellCaster caster) {
		caster.sendActionBar(getNoTargetsMessage());
	}

	@Override
	public String getNoTargetsMessage() {
		return "You need line of sight with an entity!";
	}

	@Override
	public String toString() {
		return "Line of Sight (" + range + ")"; 
	}
}
