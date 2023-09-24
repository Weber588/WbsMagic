package wbs.magic.targeters;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import wbs.magic.SpellCaster;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spells.SpellInstance;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public abstract class GenericTargeter extends Targeter {

	protected double range = 5;

	protected EntityType entityType = null;
	protected Class<? extends LivingEntity> defaultTargetClass = LivingEntity.class;

	protected boolean ignoreCaster = true;

	/**
	   * Get all targets for the given caster in a way specific to each implementation
	   * @param caster The spellcaster to get targets for
	   * @return A set of living entities that meet the targeting criteria for the targeter type. May return an empty set, will never be null
	   */
	public Collection<LivingEntity> getTargets(SpellCaster caster) {
		return new HashSet<>(getTargets(caster, defaultTargetClass));
	}

	/**
	   * Get all player targets for the given caster
	   * @param caster The spellcaster to get targets for
	   * @return A set of living entities that meet the targeting criteria for the targeter type. May return an empty set, will never be null
	   */
	public Collection<Player> getPlayerTargets(SpellCaster caster) {
		return getTargets(caster, Player.class);
	}

	/**
	   * Get all targets of a specific Entity subclass
	   * @param caster The spellcaster to get targets for
	   * @param clazz The class type to return valid entities for
	   * @return A set of clazz entities that meet the targeting criteria. May return an empty set, will never be null
	   */
	public abstract <T extends Entity> Collection<T> getTargets(SpellCaster caster, Class<T> clazz);

	/**
	 * Set whether or not this targeter should ignore the player casting
	 * @param ignoreCaster If true, the caster will never be included
	 * @return The same targeter (for chaining)
	 */
	@SuppressWarnings("unchecked")
	public <T extends GenericTargeter> T setIgnoreCaster(boolean ignoreCaster) {
		this.ignoreCaster = ignoreCaster;
		return (T) this;
	}

	/**
	 * Get a predicate that filters for a certain class of living entity for a specific caster
	 * @param caster The caster to get the predicate for
	 * @param clazz The subclass of LivingEntity to filter for
	 * @return A predicate with a test() method to see if a living entity is a valid target
	 * for the caster, and if the target is of the provided class.
	 */
	public final <T extends Entity> Predicate<Entity> getPredicate(SpellCaster caster, Class<T> clazz) {
		Player player = caster.getPlayer();
		return entity -> {
			if (entityType != null && entity.getType() != entityType) return false;

			if (!clazz.isInstance(entity)) return false;

			boolean returnVal = SpellInstance.VALID_TARGETS_PREDICATE.test(entity);
			if (ignoreCaster) {
				returnVal &= !entity.equals(player);
			}

			return returnVal;
		};
	}

	public void setEntityType(EntityType entityType) {
		this.entityType = entityType;
	}

    public abstract String getNoTargetsMessage();

	public EntityType getEntityType() {
		return entityType;
	}

	public Class<? extends LivingEntity> getDefaultTargetClass() {
		return defaultTargetClass;
	}

	public void setDefaultTargetClass(Class<? extends LivingEntity> defaultTargetClass) {
		this.defaultTargetClass = defaultTargetClass;
	}

	@Override
	public abstract String toString();
}
