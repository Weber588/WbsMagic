package wbs.magic.events.objects;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import wbs.magic.objects.generics.DynamicMagicObject;
import wbs.magic.objects.generics.MagicObject;

public class DynamicObjectBounceEvent extends MagicObjectEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final DynamicMagicObject magicObject;
    public DynamicObjectBounceEvent(DynamicMagicObject magicObject, Location hitLocation, BlockFace hitFace) {
        super(magicObject);
        this.magicObject = magicObject;
        this.hitLocation = hitLocation;
        this.hitFace = hitFace;
    }

    private final Location hitLocation;
    private final BlockFace hitFace;

    public Location getHitLocation() {
        return hitLocation;
    }

    public BlockFace getHitFace() {
        return hitFace;
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
