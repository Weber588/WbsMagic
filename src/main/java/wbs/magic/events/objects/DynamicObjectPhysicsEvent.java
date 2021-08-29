package wbs.magic.events.objects;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.magic.objects.generics.DynamicMagicObject;

/**
 * This even is fired when a {@link DynamicMagicObject}
 * performs a physics step. May happen multiple times per tick.
 */
public class DynamicObjectPhysicsEvent extends MagicObjectEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final DynamicMagicObject magicObject;
    public DynamicObjectPhysicsEvent(DynamicMagicObject magicObject, Vector accelerationThisStep) {
        super(magicObject);
        this.magicObject = magicObject;

        this.accelerationThisStep = accelerationThisStep;
    }

    private Vector accelerationThisStep;

    public Vector getAccelerationThisStep() {
        return accelerationThisStep;
    }

    public void setAccelerationThisStep(Vector accelerationThisStep) {
        this.accelerationThisStep = accelerationThisStep;
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
