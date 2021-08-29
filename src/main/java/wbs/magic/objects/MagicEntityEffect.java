package wbs.magic.objects;

import org.bukkit.entity.Entity;
import wbs.magic.objects.generics.KinematicMagicObject;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;

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

    @Override
    protected boolean tick() {
        setLocation(entity.getLocation());

        if (expireOnDeath) {
            return entity.isDead() || !entity.isValid();
        }

        return false;
    }

    public boolean isExpireOnDeath() {
        return expireOnDeath;
    }

    public void setExpireOnDeath(boolean expireOnDeath) {
        this.expireOnDeath = expireOnDeath;
    }
}
