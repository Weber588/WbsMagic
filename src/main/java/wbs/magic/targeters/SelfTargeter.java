package wbs.magic.targeters;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import wbs.magic.SpellCaster;

public class SelfTargeter extends GenericTargeter {

	public static double DEFAULT_RANGE = 0;
	protected double range = DEFAULT_RANGE;

	/*
	 *  These two remain for SelfTargeter because it can only return player type;
	 *  No point checking type unless a class was provided
	 */
	
	@Override
	public Set<LivingEntity> getTargets(SpellCaster caster) {
		Set<LivingEntity> targets = new HashSet<>(); 
		targets.add(caster.getPlayer());
		return targets;
	}
	
	@Override
	public Set<Player> getPlayerTargets(SpellCaster caster) {
		Set<Player> targets = new HashSet<>(); 
		targets.add(caster.getPlayer());
		return targets;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Entity> Set<T> getTargets(SpellCaster caster, Class<T> clazz) {
		Set<T> targets = new HashSet<>();
		Player player = caster.getPlayer();

		// Don't use the predicate, as it filters out the caster, and we want to return it.
		if (clazz.isInstance(player)) {
			targets.add((T) player);
		}
		return targets;
	}

	@Override
	public double getRange() {
		return range;
	}

	@Override
	public void sendFailMessage(SpellCaster caster) {
		caster.sendActionBar(TargeterType.SELF.getFailMessage());
	}
	
	@Override
	public String toString() {
		return "Self"; 
	}

}
