package wbs.magic.objects;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import wbs.magic.events.objects.MagicObjectMoveEvent;
import wbs.magic.objects.colliders.Collision;
import wbs.magic.objects.generics.KinematicMagicObject;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;
import wbs.utils.util.WbsMath;

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Follows the given entity to allow entity-based magic effects to be dispelled
 * via callbacks
 */
public class MagicEntityEffect extends KinematicMagicObject {

    private static final Multimap<UUID, MagicEntityEffect> entityEffects = HashMultimap.create();

    public static Collection<MagicEntityEffect> getEffects(Entity entity) {
        return getEffects(entity.getUniqueId());
    }

    public static Collection<MagicEntityEffect> getEffects(UUID uuid) {
        return new LinkedList<>(entityEffects.get(uuid));
    }

    public static Collection<MagicEntityEffect> getEffects(Entity entity, Class<? extends SpellInstance> clazz) {
        return getEffects(entity.getUniqueId(), clazz);
    }

    public static Collection<MagicEntityEffect> getEffects(UUID uuid, Class<? extends SpellInstance> clazz) {
        return entityEffects.get(uuid).stream()
                .filter(effect ->
                        clazz.isAssignableFrom(effect.getSpell().getClass()))
                .collect(Collectors.toList());
    }

    @Nullable
    public static MagicEntityEffect getEffectBySpell(Entity entity, Class<? extends SpellInstance> clazz) {
        return getEffectBySpell(entity.getUniqueId(), clazz);
    }

    @Nullable
    public static MagicEntityEffect getEffectBySpell(UUID uuid, Class<? extends SpellInstance> clazz) {
        return entityEffects.get(uuid).stream()
                .filter(check ->
                        clazz.isAssignableFrom(check.getSpell().getClass()))
                .findAny()
                .orElse(null);
    }

    @Nullable
    public static <T extends SpellInstance> T getAffectingSpell(Entity entity, Class<T> clazz) {
        return getAffectingSpell(entity.getUniqueId(), clazz);
    }

    @Nullable
    public static <T extends SpellInstance> T getAffectingSpell(UUID uuid, Class<T> clazz) {
        MagicEntityEffect effect = getEffectBySpell(uuid, clazz);

        if (effect != null) {
            return clazz.cast(effect.getSpell());
        }

        return null;
    }

    public MagicEntityEffect(Entity entity, SpellCaster caster, SpellInstance castingSpell) {
        super(entity.getLocation(), caster, castingSpell);
        this.entity = entity;
        entityEffects.put(entity.getUniqueId(), this);
    }

    private final Entity entity;

    private boolean expireOnDeath = true;
    private boolean removeOnExpire = false;
    private boolean moveEntityWithObject = false;

    private boolean collidedThisTick = false;
    @Nullable
    private String expireMessage = null;

    @Override
    protected final boolean tick() {
        if (collidedThisTick) {
            setLocation(entity.getLocation());
            collidedThisTick = false;
        } else {
            move(entity.getLocation());
        }

        boolean cancel = false;
        if (expireOnDeath) {
            cancel = entity.isDead() || !entity.isValid();
        }

        return onTick(entity) || cancel;
    }

    protected boolean onTick(Entity entity) {
        return false;
    }

    @Override
    public Location move(Location location) {
        Location toReturn = super.move(location);

        if (moveEntityWithObject && !location.equals(toReturn)) {
            // TODO: Does this need to check for caster to use magic teleport?
            entity.teleport(toReturn);
        }

        return toReturn;
    }

    @Override
    protected void onRemove() {
        super.onRemove();

        entityEffects.remove(entity.getUniqueId(), this);

        if (removeOnExpire && entity.isValid()) {
            if (entity instanceof Player) {
                ((Player) entity).setHealth(0);
            } else {
                entity.remove();
            }
        }

        if (expireMessage != null && entity instanceof Player) {
            plugin.sendActionBar(expireMessage, (Player) entity);
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

    @Nullable
    public String getExpireMessage() {
        return expireMessage;
    }

    /**
     * Set a message to send as an actionbar to the entity if they're a player, when this effect expires
     * @param expireMessage The message to send on remove. Null for no message.
     */
    public void setExpireMessage(@Nullable String expireMessage) {
        this.expireMessage = expireMessage;
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
