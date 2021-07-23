package wbs.magic.spellinstances.ranged;

import org.bukkit.Location;
import wbs.magic.annotations.FailableSpell;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.annotations.SpellSettings;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.objects.GravityWellObject;
import wbs.magic.spells.SpellConfig;
import wbs.magic.wrappers.SpellCaster;

@Spell(name = "Gravity Well",
        cost = 50,
        cooldown = 30,
        description = "Summon a miniature black hole that pulls creatures and projectiles towards it."
)
@FailableSpell("The spell will fail if the caster is not looking at a block, or the block is too far away.")
@SpellSettings(canBeConcentration = true)
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 15)
@SpellOption(optionName = "radius", type = SpellOptionType.DOUBLE, defaultDouble = 6)
@SpellOption(optionName = "force", type = SpellOptionType.DOUBLE, defaultDouble = 0.8)
@SpellOption(optionName = "ignore-caster", type = SpellOptionType.BOOLEAN,defaultBool = false)
@SpellOption(optionName = "target-entities", type = SpellOptionType.BOOLEAN,defaultBool = true)
@SpellOption(optionName = "target-projectiles", type = SpellOptionType.BOOLEAN,defaultBool = true)
// Overrides
@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 30)
public class GravityWellSpell extends RangedSpell {
    public GravityWellSpell(SpellConfig config, String directory) {
        super(config, directory);

        duration = (int) (config.getDouble("duration") * 20);
        radius = config.getDouble("radius");
        force = config.getDouble("force");
        ignoreCaster = config.getBoolean("ignore-caster");

        targetEntities = config.getBoolean("target-entities");
        targetProjectiles = config.getBoolean("target-projectiles");
    }

    private final int duration;
    private final double radius;
    private final double force;
    private final boolean ignoreCaster;
    private final boolean targetEntities;
    private final boolean targetProjectiles;

    @Override
    public boolean cast(SpellCaster caster) {
        Location targetPos = caster.getTargetPos(range);
        if (targetPos == null) {
            caster.sendActionBar("You need line of sight to a block!");
            return false;
        }

        if (isConcentration) {
            caster.setConcentration(this);
        }

        GravityWellObject well = new GravityWellObject(targetPos.add(0, 1, 0), caster, this);

        well.setIgnoreCaster(ignoreCaster);
        well.setDistance(radius);
        well.setForce(force);
        well.setDuration(duration);
        well.setTargetEntities(targetEntities);
        well.setTargetProjectiles(targetProjectiles);

        well.run();

        return true;
    }

    @Override
    public String toString() {
        String asString = super.toString();

        asString += "\n&rDuration: &7" + (duration / 20) + " seconds";
        asString += "\n&rRadius: &7" + radius;
        asString += "\n&rForce: &7" + force;
        asString += "\n&rIgnore caster: &7" + ignoreCaster;
        asString += "\n&rTarget projectiles? &7" + targetProjectiles;
        asString += "\n&rTarget entities? &7" + targetEntities;

        return asString;
    }
}
