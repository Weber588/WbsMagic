package wbs.magic.spells.ranged.targeted;

import org.bukkit.entity.LivingEntity;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.SpellCaster;
import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.WbsEntities;

import java.util.Collection;
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
    public boolean preCastEntity(CastingContext context, Collection<LivingEntity> targets) {
        SpellCaster caster = context.caster;

        Set<LivingEntity> undeadMobs = WbsEntities.getNearby(caster.getPlayer(), radius, false, LivingEntity.class);



        return true;
    }

    @Override
    public void castOn(CastingContext context, LivingEntity target) {

    }

    @Override
    public String toString() {
        String asString = super.toString();

        asString += "\n&rRadius: &7" + radius;

        return asString;
    }
}
