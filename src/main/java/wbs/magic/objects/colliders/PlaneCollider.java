package wbs.magic.objects.colliders;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.magic.objects.generics.MagicObject;

public abstract class PlaneCollider extends Collider {

    @NotNull
    protected Vector normal;

    public PlaneCollider(MagicObject parent, @NotNull Vector normal) {
        super(parent);
        this.normal = normal;
    }

    /**
     * Check if the given location is on the side
     * of the plane that the normal extends into
     * @param location The location to check
     * @return True if the location is on the normal-facing
     * side of the plane
     */
    protected boolean isAbove(Location location) {
        return location.toVector().subtract(getLocation().toVector()).angle(normal) < Math.PI / 2;
    }

    /**
     * Gets the distance of the given location
     * from the normal extruding infinitely from
     * the colliders location
     * @param location The location to check against
     * @return The distance to the location along the plane
     */
    protected double distanceOnPlane(Location location) {
        Vector toLoc = location.clone().subtract(getLocation()).toVector();

        double angle = normal.angle(toLoc);

        return Math.sin(angle) * toLoc.length();
    }

    protected Location getHitPosition(Location start, Location end) {
        Vector between = start.clone().subtract(end).toVector();

        Vector diff = start.clone().subtract(getLocation()).toVector();

        double component1 = diff.dot(normal);
        double component2 = between.dot(normal);
        double component3 = component1 / component2;

        return start.clone().subtract(between.multiply(component3));
    }

    protected Vector localNormal(Location startPoint) {
        return normal.clone().multiply(isAbove(startPoint) ? 1 : -1);
    }
}
