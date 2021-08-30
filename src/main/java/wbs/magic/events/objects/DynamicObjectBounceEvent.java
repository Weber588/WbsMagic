package wbs.magic.events.objects;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.magic.objects.generics.DynamicMagicObject;

public class DynamicObjectBounceEvent extends MagicObjectEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final DynamicMagicObject magicObject;
    public DynamicObjectBounceEvent(DynamicMagicObject magicObject, Location hitLocation, Vector normal) {
        super(magicObject);
        this.magicObject = magicObject;
        this.hitLocation = hitLocation;
        this.normal = normal;
    }

    private final Location hitLocation;
    private final Vector normal;

    public Location getHitLocation() {
        return hitLocation;
    }

    public Vector getNormal() {
        return normal.clone();
    }

    @Override
    public DynamicMagicObject getMagicObject() {
        return magicObject;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
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
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }
}
