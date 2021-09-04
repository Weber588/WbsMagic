package wbs.magic.objects.colliders;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.events.objects.MagicObjectMoveEvent;
import wbs.magic.objects.generics.MagicObject;

public class QuadCollider extends PlaneCollider {
    // "point4" is the point opposite 2 in a parallelogram
    protected final Location point1, point2, point3, point4;

    public QuadCollider(MagicObject parent,
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

        point4 = point2.clone()
                        .add(
                                point3.toVector().subtract(point2.toVector())
                        ).add(
                        point1.toVector().subtract(point2.toVector())
                );
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

            double angle1to3 = to3.angle(to1);

            double totalAngle = to1.angle(to2);
            totalAngle += to2.angle(to3);
            totalAngle += angle1to3;

            if (totalAngle >= Math.PI * 2 - 0.05) {
                return new Collision(this, hitPoint, localNormal(start));
            } else {
                Vector to4 = hitPoint.toVector().subtract(point4.toVector());

                totalAngle = to4.angle(to1);
                totalAngle += to4.angle(to3);
                totalAngle += angle1to3;

                if (totalAngle >= Math.PI * 2 - 0.05) {
                    return new Collision(this, hitPoint, localNormal(start));
                }
            }
        }

        return null;
    }
}
