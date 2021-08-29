package wbs.magic.spells.ranged.targeted;

import org.bukkit.entity.LivingEntity;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.SpellCaster;

public class SpawnEntitySpell extends TargetedSpell {
    public SpawnEntitySpell(SpellConfig config, String directory) {
        super(config, directory);
    }

    @Override
    protected <T extends LivingEntity> void castOn(SpellCaster caster, T target) {

    }
}
