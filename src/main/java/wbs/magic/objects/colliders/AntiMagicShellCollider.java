package wbs.magic.objects.colliders;

import wbs.magic.events.objects.MagicObjectMoveEvent;
import wbs.magic.objects.AntiMagicShellObject;
import wbs.magic.objects.generics.DynamicMagicObject;
import wbs.magic.objects.generics.DynamicProjectileObject;
import wbs.magic.spells.SpellInstance;
import wbs.utils.util.WbsMath;

import java.util.Objects;

public class AntiMagicShellCollider extends SphereCollider {

    private final AntiMagicShellObject parent;

    public AntiMagicShellCollider(AntiMagicShellObject parent, double radius) {
        super(parent, radius);
        this.parent = parent;

        setCollideOnLeave(true);
    }

    @Override
    protected void beforeBounce(MagicObjectMoveEvent moveEvent, DynamicMagicObject dynamicObject) {
        Collision collision = Objects.requireNonNull(moveEvent.getCollision());

        collision.setNormal(collision.getNormal().add(WbsMath.randomVector(0.05)));
    }

    @Override
    protected void onBounce(MagicObjectMoveEvent moveEvent, DynamicMagicObject dynamicObject) {
        if (dynamicObject instanceof DynamicProjectileObject) {
            // Allow reflected projectiles to hit the sender
            dynamicObject.setEntityPredicate(SpellInstance.VALID_TARGETS_PREDICATE);
        }

        parent.setHits(parent.getHits());

        moveEvent.setCancelled(true);
    }
}
