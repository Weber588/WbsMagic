package wbs.magic.events;

import org.bukkit.event.Event;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;

public abstract class SpellEvent extends Event {

	protected SpellEvent(SpellCaster caster, SpellInstance spell) {
		this.caster = caster;
		this.spell = spell;
	}
	
	private final SpellCaster caster;
	private final SpellInstance spell;
	
	public SpellInstance getSpell() {
		return spell;
	}

	public SpellCaster getCaster() {
		return caster;
	}

}
