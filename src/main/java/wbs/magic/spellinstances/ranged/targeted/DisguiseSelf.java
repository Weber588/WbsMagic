package wbs.magic.spellinstances.ranged.targeted;

import org.bukkit.entity.LivingEntity;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.spells.SpellConfig;
import wbs.magic.wrappers.SpellCaster;

import java.util.Set;

@Spell(name = "Disguise SElf",
        cost = 15,
        cooldown = 15,
        description = "Disguise yourself as the target mob or player. If you disguise as a mob, that type of mob will not attack you."
)
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultInt = 1, aliases = {"time", "length"})
public class DisguiseSelf extends TargetedSpell {
    public DisguiseSelf(SpellConfig config, String directory) {
        super(config, directory);
    }

    @Override
    protected <T extends LivingEntity> boolean preCast(SpellCaster caster, Set<T> targets) {



        return true; // Don't iterate over multiple
    }

    @Override
    protected <T extends LivingEntity> void castOn(SpellCaster caster, T target) {

    }
}
