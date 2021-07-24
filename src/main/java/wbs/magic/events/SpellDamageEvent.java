package wbs.magic.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;

public class SpellDamageEvent extends SpellEvent implements Cancellable {

	public SpellDamageEvent(SpellCaster caster, SpellInstance spell, double damage) {
		super(caster, spell);
		this.damage = damage;
	}
	
	double damage;
	
	public double getDamage() {
		return damage;
	}
	

	private static final HandlerList HANDLERS = new HandlerList();
	
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
