package wbs.magic.objects.colliders;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.events.objects.MagicObjectMoveEvent;
import wbs.magic.objects.generics.MagicObject;

public class CircleCollider extends PlaneCollider {

    private double radius = 1;

    public CircleCollider(MagicObject parent, @NotNull Vector normal) {
        super(parent, normal);
    }

    @Override
    protected @Nullable Collision getCollision(MagicObjectMoveEvent event) {
        Location start = event.getMagicObject().getLocation();
        Location end = event.getNewLocation();

        boolean isAboveBefore = isAbove(start);
        boolean isAboveAfter = isAbove(end);

        if (isAboveBefore == isAboveAfter) return null;

        if ((collideOnEnter && isAboveBefore) || (collideOnLeave && isAboveAfter)) {
            Location hitPos = getHitPosition(start, end);

            if (distanceOnPlane(hitPos) < radius) {
                return new Collision(this, hitPos, localNormal(start));
            }
        }

        return null;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
