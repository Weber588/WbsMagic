package wbs.magic.targeters;

import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import wbs.magic.SpellCaster;

import java.util.Set;
import java.util.function.Predicate;

/*
 *  This class should never be created or used; it simply provides the default 
 *  methods for getTargets and getPlayerTargets since they're trivial and identical
 *  in all subclasses
 */
public abstract class GenericTargeter {

	protected double range = 5;

	/**
	   * Get all targets for the given caster in a way specific to each implementation
	   * @param caster The spellcaster to get targets for
	   * @return A set of living entities that meet the targeting criteria for the targeter type. May return an empty set, will never be null
	   */
	public Set<LivingEntity> getTargets(SpellCaster caster) {
		return getTargets(caster, LivingEntity.class);
	}

	/**
	   * Get all player targets for the given caster
	   * @param caster The spellcaster to get targets for
	   * @return A set of living entities that meet the targeting criteria for the targeter type. May return an empty set, will never be null
	   */
	public Set<Player> getPlayerTargets(SpellCaster caster) {
		return getTargets(caster, Player.class);
	}

	/**
	   * Get all targets of a specific Entity subclass
	   * @param caster The spellcaster to get targets for
	   * @param clazz The class type to return valid entities for
	   * @return A set of clazz entities that meet the targeting criteria. May return an empty set, will never be null
	   */
	public abstract <T extends LivingEntity> Set<T> getTargets(SpellCaster caster, Class<T> clazz);

	/**
	   * Gets the range for the targeter.
	   * @return The range
	   */
	public double getRange() {
		return range;
	}
	
	/**
	   * Sends the targets failMessage to the casting player.
	   * @param caster The spellcaster to send the fail message to
	   */
	public abstract void sendFailMessage(SpellCaster caster);
	
	/**
	 * Get a predicate that filters for a certain class of living entity for a specific caster
	 * @param caster The caster to get the predicate for
	 * @param clazz The subclass of LivingEntity to filter for
	 * @return A predicate with a test() method to see if a living entity is a valid target
	 * for the caster, and if the target is of the provided class.
	 */
	protected final <T extends LivingEntity> Predicate<Entity> getPredicate(SpellCaster caster, Class<T> clazz) {
		Player player = caster.getPlayer();
		return entity -> {
			boolean returnVal = false;
			if (clazz.isInstance(entity)) {
				returnVal = true;
				if (entity instanceof Player) {
					if (entity.equals(player)) {
						returnVal = false;
					}
					if (((Player) entity).getGameMode() == GameMode.SPECTATOR) {
						returnVal = false;
					}
				} else if (entity instanceof ArmorStand) {
					returnVal = false;
				}

				if (entity.isDead()) {
					returnVal = false;
				}
			}
			return returnVal;
		};
	}
}
