package wbs.magic.spellinstances.ranged.targeted;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.spells.SpellConfig;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.LineOfSightTargeter;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.WbsEntities;

import java.util.Set;

@Spell(name = "Control Undead",
        description = "Force all undead in a radius to attack the targeted mob.")
@SpellOption(optionName = "radius", type = SpellOptionType.DOUBLE, defaultDouble = 15)
public class ControlUndead extends TargetedSpell {
    public ControlUndead(SpellConfig config, String directory) {
        super(config, directory);

        radius = config.getDouble("radius");
    }

    private double radius;

    @Override
    protected <T extends LivingEntity> boolean preCast(SpellCaster caster, Set<T> targets) {

        Set<LivingEntity> undeadMobs = WbsEntities.getNearby(caster.getPlayer(), radius, false, LivingEntity.class);



        return true;
    }

    @Override
    protected <T extends LivingEntity> void castOn(SpellCaster caster, T target) {

    }

}
