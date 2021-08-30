package wbs.magic.events.objects;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import wbs.magic.objects.colliders.Collision;
import wbs.magic.objects.generics.MagicObject;

public class MagicObjectCollisionEvent extends MagicObjectEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Collision collision;

    public MagicObjectCollisionEvent(MagicObject magicObject, Collision collision) {
        super(magicObject);
        this.collision = collision;
    }

    public Collision getCollision() {
        return collision;
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
