package wbs.magic.spellinstances.ranged;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.spellinstances.SpellInstance;

@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 60)
public abstract class RangedSpell extends SpellInstance {

	protected double range; // in blocks
	
	protected RangedSpell(SpellConfig config, String directory) {
		super(config, directory);
		range = config.getDouble("range");
	}
	
	@Override
	public String toString() {
		String asString = super.toString();
		
		asString += "\n&rRange: &7" + range;
		
		return asString;
	}

	public double getRange() {
		return range;
	}
}
