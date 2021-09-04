package wbs.magic.objects.colliders;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import wbs.magic.events.objects.MagicObjectMoveEvent;
import wbs.magic.objects.generics.KinematicMagicObject;
import wbs.magic.objects.generics.MagicObject;

public class SphereCollider extends Collider {

    private double radius;

    public SphereCollider(MagicObject parent, double radius) {
        super(parent);
        this.radius = radius;
    }

    @Override
    protected @Nullable Collision getCollision(MagicObjectMoveEvent moveEvent) {
        KinematicMagicObject obj = moveEvent.getMagicObject();

        double newDist = moveEvent.getNewLocation().distance(getLocation());
        double currentDist =
                moveEvent.getMagicObject()
                        .getLocation()
                        .distance(getLocation());

        if ((collideOnEnter && newDist < radius && currentDist > radius) ||
                (collideOnLeave && newDist > radius && currentDist < radius))
        {
            boolean entering = newDist < radius;

            Vector normal = obj.getLocation().subtract(getLocation()).toVector();

            Location hitPos = normal.normalize()
                    .multiply(radius)
                    .toLocation(getWorld())
                    .add(getLocation());

            Location offset = normal.normalize()
                    .multiply(radius * (1 + (entering ? -0.5 : 0.5)))
                    .toLocation(getWorld())
                    .add(getLocation());

            normal = obj.getLocation().subtract(offset).toVector();

            Collision collision = new Collision(this, hitPos, normal);

            onCollide(moveEvent, collision, entering);

            return collision;
        }

        return null;
    }

    protected void onCollide(MagicObjectMoveEvent moveEvent, Collision collision, boolean entering) {

    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
