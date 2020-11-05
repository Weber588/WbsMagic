package wbs.magic.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import wbs.magic.spellinstances.SpellInstance;
import wbs.magic.wrappers.SpellCaster;

public class SpellPrepareEvent extends SpellEvent implements Cancellable {
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	public SpellPrepareEvent(SpellCaster caster, SpellInstance spell) {
		super(caster, spell);
	}
	
	
	
	
	
	
	
	
	

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
    	return HANDLERS;
	}

	private boolean isCancelled = false;
	
	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		isCancelled = cancel;
	}
}
