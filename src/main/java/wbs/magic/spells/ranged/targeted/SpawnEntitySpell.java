package wbs.magic.spells.ranged.targeted;

import org.bukkit.entity.LivingEntity;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.SpellCaster;
import wbs.magic.spells.framework.CastingContext;

public class SpawnEntitySpell extends TargetedSpell {
    public SpawnEntitySpell(SpellConfig config, String directory) {
        super(config, directory);
    }

    @Override
    public void castOn(CastingContext context, LivingEntity target) {

    }
}
