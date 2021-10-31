package wbs.magic.spells.ranged.targeted;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import org.bukkit.entity.LivingEntity;
import wbs.magic.spellmanagement.configuration.RequiresPlugin;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.SpellCaster;
import wbs.magic.spells.framework.CastingContext;

import java.util.Collection;
import java.util.Set;

@Spell(name = "Disguise SElf",
        cost = 15,
        cooldown = 15,
        description = "Disguise yourself as the target mob or player. If you disguise as a mob, that type of mob will not attack you."
)
@RequiresPlugin("LibsDisguises")
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultInt = 1, aliases = {"time", "length"})
@SpellOption(optionName = "disguise-type", type = SpellOptionType.STRING, defaultString = "SHEEP", enumType = DisguiseType.class)
public class DisguiseSelf extends TargetedSpell {
    public DisguiseSelf(SpellConfig config, String directory) {
        super(config, directory);
    }

    @Override
    public boolean preCastEntity(CastingContext context, Collection<LivingEntity> targets) {
        SpellCaster caster = context.caster;



        return true; // Don't iterate over multiple
    }

    @Override
    public void castOn(CastingContext context, LivingEntity target) {

    }
}
