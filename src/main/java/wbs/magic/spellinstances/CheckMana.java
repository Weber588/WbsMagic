package wbs.magic.spellinstances;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.enums.SpellType;
import wbs.magic.wrappers.SpellCaster;

@Spell(name = "Check Mana",
		cost = 0,
		cooldown = 0,
		description = "A spell that simply displays the casters mana."
)
@SpellOption(optionName = "cost", type = SpellOptionType.INT, defaultInt = 0)
public class CheckMana extends SpellInstance {

	public CheckMana(SpellConfig config, String directory) {
		super(config, directory);
	}

	@Override
	public boolean cast(SpellCaster caster) {
		caster.checkMana();
		return false;
	}

	@Override
	public SpellType getType() {
		return SpellType.CHECK_MANA;
	}
}
