package wbs.magic.spells.ranged;

import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spells.SpellInstance;

@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 60)
public abstract class RangedSpell extends SpellInstance {

	protected double range; // in blocks
	
	public RangedSpell(SpellConfig config, String directory) {
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
