package wbs.magic.events.objects;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import wbs.magic.objects.generics.KinematicMagicObject;

/**
 * This event is fired when a magic object is attempting to move, but before
 * the move is completed.
 */
public class MagicObjectMoveEvent extends MagicObjectEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private Location newLocation;
    private final KinematicMagicObject magicObject;

    public MagicObjectMoveEvent(KinematicMagicObject magicObject, Location newLocation) {
        super(magicObject);
        this.newLocation = newLocation;
        this.magicObject = magicObject;
    }

    @Override
    public KinematicMagicObject getMagicObject() {
        return magicObject;
    }

    /**
     * The location the object is attempting to move to.
     * @return A clone of the location being moved to. To
     * modify this value, use {@link }
     */
    public Location getNewLocation() {
        return newLocation.clone();
    }

    public void setNewLocation(Location newLocation) {
        this.newLocation = newLocation;
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
