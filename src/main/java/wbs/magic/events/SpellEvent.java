package wbs.magic.events;

import org.bukkit.event.Event;
import wbs.magic.spellinstances.SpellInstance;
import wbs.magic.wrappers.SpellCaster;

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
