package wbs.magic.events.objects;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import wbs.magic.objects.generics.MagicObject;

public class MagicObjectSpawnEvent extends MagicObjectEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    public MagicObjectSpawnEvent(MagicObject magicObject) {
        super(magicObject);
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
