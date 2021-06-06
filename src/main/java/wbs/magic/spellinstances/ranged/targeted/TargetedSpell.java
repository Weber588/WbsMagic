package wbs.magic.spellinstances.ranged.targeted;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.LivingEntity;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.exceptions.InvalidTargetException;
import wbs.magic.exceptions.NoTargetsFoundException;
import wbs.magic.spellinstances.ranged.RangedSpell;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.LineOfSightTargeter;
import wbs.magic.targeters.NearestTargeter;
import wbs.magic.targeters.RadiusTargeter;
import wbs.magic.targeters.RandomTargeter;
import wbs.magic.targeters.SelfTargeter;
import wbs.magic.wrappers.SpellCaster;

@SpellOption(optionName = "targeter", type = SpellOptionType.STRING, aliases = {"targetter", "target"})
public abstract class TargetedSpell extends RangedSpell {
	
	protected final static String INVALID_TARGET_ERROR = "Invalid target!";
	protected final static double DEFAULT_RANGE = -1;
	
	public TargetedSpell(SpellConfig config, String directory) {
		super(config, directory, DEFAULT_RANGE);
		configureTargeter(config, directory, null);
	}

	public TargetedSpell(SpellConfig config, String directory, double defaultRange) {
		super(config, directory, defaultRange);
		configureTargeter(config, directory, null);
	}

	public TargetedSpell(SpellConfig config, String directory, GenericTargeter targeter) {
		super(config, directory, DEFAULT_RANGE);
		configureTargeter(config, directory, targeter);
	}
	
	public TargetedSpell(SpellConfig config, String directory, double defaultRange, GenericTargeter targeter) {
		super(config, directory, defaultRange);
		configureTargeter(config, directory, targeter);
	}
	
	protected void sendConfirmationMessage(SpellCaster caster, Set<? extends LivingEntity> targets) {
		if (targets.size() == 1) {
			LivingEntity displayTarget = null;
			for (LivingEntity target : targets) {
				displayTarget = target;
			}
			caster.sendActionBar("Cast &h" + getName() + "&r on &h" + displayTarget.getName() + "&r!");
		} else {
			caster.sendActionBar("Cast &h" + getName() + "&r on &h" + targets.size() + "&r creatures!");
		}
	}
	
	// Forcing the implementing spell to use castOn instead
	@Override
	public final boolean cast(SpellCaster caster) {
		return cast(caster, null);
	}
	
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
	public final <T extends LivingEntity> boolean cast(SpellCaster caster, LivingEntity interactionTarget) {
		return cast(caster, interactionTarget, targetClass);
	}
	
	/**
	 * Defaulted 
	 * @param caster
	 * @param interactionTarget
	 * @param clazz
	 * @return
	 */
	protected final <T extends LivingEntity> boolean cast(SpellCaster caster, LivingEntity interactionTarget, Class<T> clazz) {
		Set<T> targets;
		try {
			targets = populateTargets(caster, interactionTarget, clazz);
		} catch (InvalidTargetException e) {
			caster.sendActionBar(INVALID_TARGET_ERROR);
			return false;
		} catch (NoTargetsFoundException e) {
			targeter.sendFailMessage(caster);
			return false;
		}
		
		if (preCast(caster, targets)) {
			return false;
		}
		
		for (LivingEntity target : targets) {
			castOn(caster, target);
		}
		
		sendConfirmationMessage(caster, targets);
		return true;
	}

	/**
	 * Run the spells effect on a given target. Potentially run multiple times per
	 * casting, as it happens for each target.
	 * @param caster The caster
	 * @param target The target to cast on
	 */
	protected abstract <T extends LivingEntity> void castOn(SpellCaster caster, T target);

	/**
	 * Run before each target has #castOn called on it. Mainly for setting concentration.
	 * Also returns a boolean for canceling the spell before it is run on each target;
	 * true to cancel, false to run as normal.
	 * @param caster The caster
	 * @return true to cancel
	 */
	protected <T extends LivingEntity> boolean preCast(SpellCaster caster, Set<T> targets) {
		return false;
	}

	
	/**
	 * Gets a list of targets to use, given a caster. interactionTarget overrides
	 * the spells targeter, and forces that entity to be targeted, unless it is an
	 * invalid type.
	 * @param caster The caster using the spell.
	 * @param interactionTarget The target to force. May be null to use spell's targeter.
	 * @param clazz The required class for a target to be.
	 * @return A set of targets.
	 * <p>If interactionTarget is non-null and of type T, it will be the
	 * only element in the Set.
	 * <p>If interactionTarget is null, the spells configured targeter will be used.
	 * @throws InvalidTargetException if interactionTarget is non-null, but is not of type T.
	 * @throws NoTargetsFoundException if the spells configured targeter was used, but found
	 * no targets.
	 */
	protected <T extends LivingEntity> Set<T> populateTargets(SpellCaster caster, LivingEntity interactionTarget, Class<T> clazz)
	throws InvalidTargetException, NoTargetsFoundException {
		Set<T> targets;
		if (interactionTarget == null) {
			targets = targeter.getTargets(caster, clazz);
			
			if (targets.isEmpty()) {
				throw new NoTargetsFoundException();
			}
		} else {
			if (clazz.isInstance(interactionTarget)) {
				targets = new HashSet<>();
				targets.add(clazz.cast(interactionTarget));
			} else {
				throw new InvalidTargetException();
			}
		}
		
		return targets;
	}
	
	@Override
	public String toString() {
		String asString = super.toString();
		
		asString += "\n&rTarget: &7" + targeter.toString();
		
		return asString;
	}

	protected GenericTargeter targeter;
	
	public void configureTargeter(SpellConfig config, String directory, GenericTargeter defaultTargeter) {
		String targeterString = "";
		targeterString = config.getString("targetter", targeterString);
		targeterString = config.getString("targeter", targeterString);
		targeterString = config.getString("target", targeterString);
		
		switch (targeterString.toUpperCase().replace(" ", "").replace("_", "")) {
		case "LINEOFSIGHT":
		case "LOOKING":
		case "SIGHT":
			if (range == -1) {
				range = LineOfSightTargeter.DEFAULT_RANGE;
			}
			targeter = new LineOfSightTargeter(range);
			break;
		case "NEAREST":
		case "CLOSEST":
			if (range == -1) {
				range = NearestTargeter.DEFAULT_RANGE;
			}
			targeter = new NearestTargeter(range);
			break;
		case "SELF":
		case "USER":
		case "PLAYER":
		case "CASTER":
			if (range == -1) {
				range = SelfTargeter.DEFAULT_RANGE;
			}
			targeter = new SelfTargeter();
			break;
		case "RADIUS":
		case "NEAR":
		case "CLOSE":
			if (range == -1) {
				range = RadiusTargeter.DEFAULT_RANGE;
			}
			targeter = new RadiusTargeter(range);
			break;
		case "RANDOM":
			if (range == -1) {
				range = RandomTargeter.DEFAULT_RANGE;
			}
			targeter = new RandomTargeter(range);
			break;
		default:
			targeter = defaultTargeter;
		}
	}
}
