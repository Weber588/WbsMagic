package wbs.magic.objects.colliders;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class Collision {

    @NotNull
    private final Collider collider;
    @NotNull
    private final Location hitLocation;
    @NotNull
    private Vector normal;

    public Collision(@NotNull Collider collider, @NotNull Location hitLocation, @NotNull Vector normal) {
        this.collider = collider;

        this.hitLocation = hitLocation;
        this.normal = normal.normalize();
    }

    @NotNull
    public Collider getCollider() {
        return collider;
    }

    @NotNull
    public Location getHitLocation() {
        return hitLocation.clone();
    }

    @NotNull
    public Vector getNormal() {
        return normal.clone();
    }

    public void setNormal(@NotNull Vector normal) {
        this.normal = normal;
    }
}
