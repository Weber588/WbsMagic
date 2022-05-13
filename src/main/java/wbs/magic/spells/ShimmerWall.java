package wbs.magic.spells;

import org.bukkit.Location;
import wbs.magic.SpellCaster;
import wbs.magic.objects.PersistenceLevel;
import wbs.magic.objects.ShimmerWallObject;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spells.framework.CastingContext;

@Spell(name = "Shimmer Wall",
        description = "Create a wall that allows entities to pass through, but not magic effects",
        cooldown = 45)
@SpellOption(optionName = "distance", type = SpellOptionType.DOUBLE, defaultDouble = 2)
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 30)
@SpellOption(optionName = "height", type = SpellOptionType.DOUBLE, defaultDouble = 3)
@SpellOption(optionName = "width", type = SpellOptionType.DOUBLE, defaultDouble = 5)
@SpellOption(optionName = "hits", type = SpellOptionType.INT, defaultInt = 5)
@SpellOption(optionName = "reflect", type = SpellOptionType.BOOLEAN, defaultBool = false)
@SpellOption(optionName = "dispel-level", type = SpellOptionType.STRING, defaultString = "NORMAL", enumType = PersistenceLevel.class)
@SpellOption(optionName = "allow-caster-spells", type = SpellOptionType.BOOLEAN, defaultBool = false)
@SpellOption(optionName = "one-way", type = SpellOptionType.BOOLEAN, defaultBool = false)
public class ShimmerWall extends SpellInstance {
    public ShimmerWall(SpellConfig config, String directory) {
        super(config, directory);

        distance = config.getDouble("distance");
        duration = ((int) config.getDouble("duration") * 20);
        height = config.getDouble("height");
        width = config.getDouble("width");
        reflect = config.getBoolean("reflect");
        allowCasterSpells = config.getBoolean("allow-caster-spells");
        oneWay = config.getBoolean("one-way");

        hits = config.getInt("hits");
        if (hits <= 0) {
            hits = Integer.MAX_VALUE;
        }

        level = getEnumLogErrors(PersistenceLevel.class, config, directory, "dispel-level", PersistenceLevel.NORMAL);
    }

    private final double distance;
    private final int duration;
    private final double height;
    private final double width;
    private int hits;
    private final boolean reflect;
    private final PersistenceLevel level;
    private final boolean allowCasterSpells;
    private final boolean oneWay;

    @Override
    public boolean cast(CastingContext context) {
        SpellCaster caster = context.caster;
        Location spawnLoc = caster.getLocation().add(caster.getFacingVector().setY(0).normalize().multiply(distance));

        ShimmerWallObject wallObject = new ShimmerWallObject(spawnLoc, caster, this);

        wallObject.setFacing(caster.getFacingVector().setY(0));
        wallObject.setSize(width, height);

        wallObject.setLevel(level);
        wallObject.setHits(hits);
        wallObject.setReflect(reflect);

        wallObject.setMaxAge(duration);
        wallObject.setAllowCasterSpells(allowCasterSpells);
        wallObject.setOneWay(oneWay);

        wallObject.run();
        return true;
    }

    @Override
    public String toString() {
        String asString = super.toString();

        asString += "\n&rDuration: &7" + (duration / 20) + " seconds";
        asString += "\n&rWidth: &7" + width;
        asString += "\n&rHeight: &7" + height;
        if (hits == Integer.MAX_VALUE) {
            asString += "\n&rMax hits: &7Infinite";
        } else {
            asString += "\n&rMax hits: &7" + hits;
        }
        asString += "\n&rReflect projectiles? &7" + reflect;
        asString += "\n&rAllow caster spells? &7" + allowCasterSpells;

        return asString;
    }
}
