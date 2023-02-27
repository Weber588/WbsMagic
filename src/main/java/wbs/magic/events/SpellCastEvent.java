package wbs.magic.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;
import wbs.magic.wand.MagicWand;

public class SpellCastEvent extends SpellEvent implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	@Nullable
	private final MagicWand wand;

	public SpellCastEvent(SpellCaster caster, SpellInstance spell) {
		this(caster, spell, null);
	}

	public SpellCastEvent(SpellCaster caster, SpellInstance spell, @Nullable MagicWand wand) {
		super(caster, spell);
		this.wand = wand;
	}

	@Nullable
	public MagicWand getWand() {
		return wand;
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
