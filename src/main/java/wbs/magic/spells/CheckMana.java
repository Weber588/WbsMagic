package wbs.magic.spells;

import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.SpellCaster;
import wbs.magic.spells.framework.CastingContext;

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
	public boolean cast(CastingContext context) {
		SpellCaster caster = context.caster;
		caster.checkMana();
		return false;
	}
}
