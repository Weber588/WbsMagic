package wbs.magic.spellinstances;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.wrappers.SpellCaster;

@Spell(name = "Check Mana",
		cost = 0,
		cooldown = 0,
		description = "A spell that simply displays the casters mana."
)
public class CheckMana extends SpellInstance {

	public CheckMana(SpellConfig config, String directory) {
		super(config, directory);
	}

	@Override
	public boolean cast(SpellCaster caster) {
		caster.checkMana();
		return false;
	}
}
