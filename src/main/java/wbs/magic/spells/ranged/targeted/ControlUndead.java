package wbs.magic.spells.ranged.targeted;

import org.bukkit.entity.LivingEntity;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.SpellCaster;
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

    @Override
    public String toString() {
        String asString = super.toString();

        asString += "\n&rRadius: &7" + radius;

        return asString;
    }
}
