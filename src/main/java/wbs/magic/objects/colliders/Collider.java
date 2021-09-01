package wbs.magic.objects.colliders;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.events.objects.MagicObjectCollisionEvent;
import wbs.magic.events.objects.MagicObjectMoveEvent;
import wbs.magic.objects.generics.DynamicMagicObject;
import wbs.magic.objects.generics.KinematicMagicObject;
import wbs.magic.objects.generics.MagicObject;

import java.util.*;
import java.util.function.Predicate;

public abstract class Collider {

    private static final Map<MagicObject, Collider> objectsWithColliders = new HashMap<>();

    public static Set<Collider> getColliders() {
        return new HashSet<>(objectsWithColliders.values());
    }

    public static Map<MagicObject, Collider> getColliderMap() {
        return new HashMap<>(objectsWithColliders);
    }

    public static Set<MagicObject> getObjectsWithColliders() {
        return new HashSet<>(objectsWithColliders.keySet());
    }



    private final MagicObject parent;
    @NotNull
    private Location location;
    @NotNull
    private World world;

    protected boolean cancelOnCollision = false;
    protected boolean bouncy = false;

    protected boolean collideOnEnter = true;
    protected boolean collideOnLeave = false;

    protected Predicate<KinematicMagicObject> predicate = obj -> true;

    public Collider(MagicObject parent) {
        this.parent = parent;
        location = parent.getLocation();
        world = Objects.requireNonNull(location.getWorld());

        objectsWithColliders.put(parent, this);
    }

    @Nullable
    protected abstract Collision getCollision(MagicObjectMoveEvent event);

    @Nullable
    public final Collision tryColliding(MagicObjectMoveEvent moveEvent) {
        if (!collideOnEnter && !collideOnLeave) return null;
        if (moveEvent.getNewLocation().getWorld() != location.getWorld()) return null;
        if (!predicate.test(moveEvent.getMagicObject())) return null;

        Collision collision = getCollision(moveEvent);

        if (collision == null) return null;

        MagicObjectCollisionEvent collideEvent = new MagicObjectCollisionEvent(parent, collision);

        if (collideEvent.isCancelled()) return null;

        if (cancelOnCollision) moveEvent.setCancelled(true);

        moveEvent.setCollision(collision);

        if (bouncy) {
            if (moveEvent.getMagicObject() instanceof DynamicMagicObject) {
                DynamicMagicObject dynObj = (DynamicMagicObject) moveEvent.getMagicObject();

                beforeBounce(moveEvent, dynObj);

                if (dynObj.bounce(collision.getNormal())) {
                    onBounce(moveEvent, dynObj);
                }
            }
        }

        onCollide(moveEvent, collision);

        return collision;
    }

    protected void beforeBounce(MagicObjectMoveEvent moveEvent, DynamicMagicObject dynamicObject) {

    }

    protected void onBounce(MagicObjectMoveEvent moveEvent, DynamicMagicObject dynamicObject) {

    }

    protected void onCollide(MagicObjectMoveEvent moveEvent, Collision collision) {

    }

    public void remove() {
        objectsWithColliders.remove(parent);
    }

    public MagicObject getParent() {
        return parent;
    }

    @NotNull
    public Location getLocation() {
        return location;
    }

    public void setLocation(@NotNull Location location) {
        this.location = location;
    }

    @NotNull
    public World getWorld() {
        return world;
    }

    public void setWorld(@NotNull World world) {
        this.world = world;
    }

    public boolean cancelOnCollision() {
        return cancelOnCollision;
    }

    public void setCancelOnCollision(boolean cancelOnCollision) {
        this.cancelOnCollision = cancelOnCollision;
    }

    public boolean isBouncy() {
        return bouncy;
    }

    public void setBouncy(boolean bouncy) {
        this.bouncy = bouncy;
    }

    public boolean collideOnEnter() {
        return collideOnEnter;
    }

    public void setCollideOnEnter(boolean collideOnEnter) {
        this.collideOnEnter = collideOnEnter;
    }

    public boolean collideOnLeave() {
        return collideOnLeave;
    }

    public void setCollideOnLeave(boolean collideOnLeave) {
        this.collideOnLeave = collideOnLeave;
    }
}
