package wbs.magic.objects;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import wbs.magic.objects.generics.DynamicMagicObject;
import wbs.magic.spellinstances.SpellInstance;
import wbs.magic.wrappers.SpellCaster;

/**
 * Follows the given entity to allow entity-based magic effects to be dispelled
 * via callbacks
 */
public class MagicEntityEffect extends DynamicMagicObject {

    public MagicEntityEffect(Entity entity, SpellCaster caster, SpellInstance castingSpell) {
        super(entity.getLocation(), caster, castingSpell);
        this.entity = entity;
    }

    private final Entity entity;

    private boolean expireOnDeath = false;

    @Override
    protected boolean tick() {
        setLocation(entity.getLocation());

        if (expireOnDeath) {
            return entity.isDead();
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
