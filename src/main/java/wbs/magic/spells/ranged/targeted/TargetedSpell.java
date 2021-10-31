package wbs.magic.spells.ranged.targeted;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.LivingEntity;

import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.exceptions.InvalidTargetException;
import wbs.magic.exceptions.NoTargetsFoundException;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.spells.framework.EntityTargetedSpell;
import wbs.magic.spells.framework.LivingEntitySpell;
import wbs.magic.spells.ranged.RangedSpell;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.LineOfSightTargeter;
import wbs.magic.targeters.NearestTargeter;
import wbs.magic.targeters.RadiusTargeter;
import wbs.magic.targeters.RandomTargeter;
import wbs.magic.targeters.SelfTargeter;
import wbs.magic.SpellCaster;

public abstract class TargetedSpell extends RangedSpell implements LivingEntitySpell {
	
	protected final static String INVALID_TARGET_ERROR = "Invalid target!";

	public TargetedSpell(SpellConfig config, String directory) {
		super(config, directory);
		configureTargeter(config, directory);
	}

	protected GenericTargeter targeter;

	protected Class<? extends LivingEntity> targetClass = LivingEntity.class; 

	/**
	 * Cast the spell with a given caster, with specific targets. Used in
	 * entity-interaction WandControls, since using other targeters would
	 * double up on finding the entity the caster interacted with.
	 * @param caster The caster to make cast the spell
	 * @param interactionTarget The creature to target. If null, the spells
	 * configured targeter will be used.
	 * @return true if the spell was successful, false if the spell failed
	 */
	public final boolean cast(SpellCaster caster, LivingEntity interactionTarget) {
		return false;
	}

	@Override
	public String toString() {
		String asString = super.toString();
		
		asString += "\n&rTarget: &7" + targeter.toString();
		
		return asString;
	}

	@Override
	public GenericTargeter getTargeter() {
		return targeter;
	}

	@Override
	public void setTargeter(GenericTargeter targeter) {
		this.targeter = targeter;
	}
}
