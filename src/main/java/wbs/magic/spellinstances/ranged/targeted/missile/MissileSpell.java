package wbs.magic.spellinstances.ranged.targeted.missile;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.spellinstances.ranged.targeted.TargetedSpell;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.LineOfSightTargeter;

@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 0.5)
@SpellOption(optionName = "agility", type = SpellOptionType.DOUBLE, defaultDouble = 100)
public abstract class MissileSpell extends TargetedSpell {

	private final static double DEFAULT_RANGE = 200;
	private final static GenericTargeter DEFAULT_TARGETER = new LineOfSightTargeter();
	protected double speed;
	protected double agility;

	protected MissileSpell(SpellConfig config, String directory, double defaultRange, GenericTargeter targeter, double defaultSpeed, double defaultAgility) {
		super(config, directory, defaultRange, targeter);

		speed = config.getDouble("speed", defaultSpeed);
		agility = config.getDouble("agility", defaultAgility);
	}
	
	protected MissileSpell(SpellConfig config, String directory, double defaultSpeed, double defaultAgility) {
		super(config, directory, DEFAULT_RANGE, DEFAULT_TARGETER);

		speed = config.getDouble("speed", defaultSpeed);
		agility = config.getDouble("agility", defaultAgility);
	}
	
	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rSpeed: &7" + speed;
		
		return asString;
	}
}
