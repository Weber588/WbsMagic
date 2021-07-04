package wbs.magic.spellinstances.ranged.targeted.missile;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.spellinstances.ranged.targeted.TargetedSpell;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.LineOfSightTargeter;

@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 0.5)
@SpellOption(optionName = "agility", type = SpellOptionType.DOUBLE, defaultDouble = 100)
// Overrides
@SpellOption(optionName = "targeter", type = SpellOptionType.STRING, defaultString = "LINE_OF_SIGHT")
public abstract class MissileSpell extends TargetedSpell {
	protected double speed;
	protected double agility;

	public MissileSpell(SpellConfig config, String directory) {
		super(config, directory);

		speed = config.getDouble("speed");
		agility = config.getDouble("agility");
	}
	
	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rSpeed: &7" + speed;
		asString += "\n&rAgility: &7" + agility;

		return asString;
	}
}
