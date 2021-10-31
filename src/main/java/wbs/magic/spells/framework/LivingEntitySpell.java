package wbs.magic.spells.framework;

import org.bukkit.entity.LivingEntity;

/**
 * Specific implementation of {@link EntityTargetedSpell} for LivingEntity,
 * as it's commonly used.
 */
public interface LivingEntitySpell extends EntityTargetedSpell<LivingEntity> {
    @Override
    default Class<LivingEntity> getEntityClass() {
        return LivingEntity.class;
    }
}
