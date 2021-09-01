package wbs.magic.objects.colliders;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.events.objects.MagicObjectMoveEvent;
import wbs.magic.objects.generics.MagicObject;

public class TriCollider extends PlaneCollider {

    private final Location point1, point2, point3;

    public TriCollider(MagicObject parent,
                       @NotNull Location point1,
                       @NotNull Location point2,
                       @NotNull Location point3) {
        super(parent,
                point1.toVector().subtract(point2.toVector())
                        .crossProduct(point1.toVector().subtract(point3.toVector()))
        );
        this.point1 = point1;
        this.point2 = point2;
        this.point3 = point3;
    }

    @Override
    protected @Nullable Collision getCollision(MagicObjectMoveEvent event) {
        Location start = event.getMagicObject().getLocation();
        Location end = event.getNewLocation();

        boolean isAboveBefore = isAbove(start);
        boolean isAboveAfter = isAbove(end);

        if (isAboveBefore == isAboveAfter) return null;

        if ((collideOnEnter && isAboveBefore) || (collideOnLeave && isAboveAfter)) {

            Location hitPoint = getHitPosition(start, end);

            Vector to1 = hitPoint.toVector().subtract(point1.toVector());
            Vector to2 = hitPoint.toVector().subtract(point2.toVector());
            Vector to3 = hitPoint.toVector().subtract(point3.toVector());

            double totalAngle = to1.angle(to2);
            totalAngle += to2.angle(to3);
            totalAngle += to3.angle(to1);

            if (totalAngle >= Math.PI * 2) {
                return new Collision(this, hitPoint, localNormal(start));
            }
        }

        return null;
    }
}
