package wbs.magic.spells;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import wbs.magic.SpellCaster;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellSettings;
import wbs.magic.spells.framework.BlockSpell;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.spells.framework.LivingEntitySpell;
import wbs.magic.targeters.GenericTargeter;

@Spell(name = "Fire Spray", description = "Spray fire in the direction you're facing!")
@SpellSettings(isContinuousCast = true)
public class FireSpray extends SpellInstance implements BlockSpell, LivingEntitySpell {
    public FireSpray(SpellConfig config, String directory) {
        super(config, directory);
    }

    @Override
    public boolean cast(CastingContext context) {
        return false;
    }

    @Override
    public void castOn(CastingContext context, Block target) {

    }

    @Override
    public void castOn(CastingContext context, LivingEntity target) {

    }

    @Override
    public GenericTargeter getTargeter() {
        return null;
    }
}
