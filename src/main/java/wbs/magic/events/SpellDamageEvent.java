package wbs.magic.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import wbs.magic.DamageSource;
import wbs.magic.SpellCaster;

public class SpellDamageEvent extends SpellEvent implements Cancellable {

	public SpellDamageEvent(SpellCaster caster, DamageSource source, double baseDamage, double finalDamage) {
		super(caster, source.getSpell());
		this.baseDamage = baseDamage;
		this.finalDamage = finalDamage;
	}
	
	private final double baseDamage;
	private double finalDamage;

	public double getBaseDamage() {
		return baseDamage;
	}
	public double getFinalDamage() {
		return finalDamage;
	}

	public void setFinalDamage(double finalDamage) {
		this.finalDamage = finalDamage;
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
