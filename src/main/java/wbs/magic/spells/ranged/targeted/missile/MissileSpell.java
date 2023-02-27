package wbs.magic.spells.ranged.targeted.missile;

import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;
import wbs.magic.spells.ranged.targeted.TargetedSpell;
import wbs.magic.targeters.LineOfSightTargeter;
import wbs.magic.targeters.RadiusTargeter;

@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 0.5)
@SpellOption(optionName = "agility", type = SpellOptionType.DOUBLE, defaultDouble = 100)
// Overrides
@TargeterOption(optionName = "targeter", defaultRange = 60)
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
