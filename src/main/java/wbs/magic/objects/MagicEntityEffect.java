package wbs.magic.objects;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import wbs.magic.events.objects.MagicObjectMoveEvent;
import wbs.magic.objects.colliders.Collision;
import wbs.magic.objects.generics.KinematicMagicObject;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;
import wbs.utils.util.WbsMath;

/**
 * Follows the given entity to allow entity-based magic effects to be dispelled
 * via callbacks
 */
public class MagicEntityEffect extends KinematicMagicObject {

    public MagicEntityEffect(Entity entity, SpellCaster caster, SpellInstance castingSpell) {
        super(entity.getLocation(), caster, castingSpell);
        this.entity = entity;
    }

    private final Entity entity;

    private boolean expireOnDeath = true;
    private boolean removeOnExpire = false;
    private boolean moveEntityWithObject = false;

    private boolean collidedThisTick = false;

    @Override
    protected boolean tick() {
        if (collidedThisTick) {
            setLocation(entity.getLocation());
            collidedThisTick = false;
        } else {
            move(entity.getLocation());
        }

        if (expireOnDeath) {
            return entity.isDead() || !entity.isValid();
        }

        return false;
    }

    @Override
    public Location move(Location location) {
        Location toReturn = super.move(location);

        if (moveEntityWithObject && !location.equals(toReturn)) {
            entity.teleport(toReturn);
        }

        return toReturn;
    }

    @Override
    protected void onRemove() {
        if (removeOnExpire && entity.isValid()) {
            if (entity instanceof Player) {
                ((Player) entity).setHealth(0);
            } else {
                entity.remove();
            }
        }
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean moveEntityWithObject() {
        return moveEntityWithObject;
    }

    public void setMoveEntityWithObject(boolean moveEntityWithObject) {
        this.moveEntityWithObject = moveEntityWithObject;
    }

    public boolean isExpireOnDeath() {
        return expireOnDeath;
    }

    @Override
    public void onCollide(MagicObjectMoveEvent moveEvent, Collision collision) {
        collidedThisTick = true;
        if (collision.getCollider().isBouncy()) {

            Vector velocity = entity.getVelocity();
            if (velocity.length() > 0.1) {
                Vector newVel = WbsMath.scaleVector(WbsMath.reflectVector(velocity, collision.getNormal()), velocity.length());

                entity.setVelocity(newVel);

                if (entity instanceof Fireball) {
                    Fireball fireball = (Fireball) entity;
                    fireball.setDirection(newVel);
                }
            }
        }
    }

    public void setExpireOnDeath(boolean expireOnDeath) {
        this.expireOnDeath = expireOnDeath;
    }

    public void setRemoveOnExpire(boolean removeOnExpire) {
        this.removeOnExpire = removeOnExpire;
    }
}
