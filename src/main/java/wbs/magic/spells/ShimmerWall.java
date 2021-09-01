package wbs.magic.spells;

import org.bukkit.Location;
import wbs.magic.SpellCaster;
import wbs.magic.objects.PersistenceLevel;
import wbs.magic.objects.ShimmerWallObject;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;

@Spell(name = "Shimmer Wall", description = "Create a wall that allows entities to pass through, but not magic effects")
@SpellOption(optionName = "distance", type = SpellOptionType.DOUBLE, defaultDouble = 2)
@SpellOption(optionName = "height", type = SpellOptionType.DOUBLE, defaultDouble = 3)
@SpellOption(optionName = "width", type = SpellOptionType.DOUBLE, defaultDouble = 5)
@SpellOption(optionName = "hits", type = SpellOptionType.INT, defaultInt = Integer.MAX_VALUE)
@SpellOption(optionName = "dispel-level", type = SpellOptionType.STRING, defaultString = "NORMAL", enumType = PersistenceLevel.class)
public class ShimmerWall extends SpellInstance {
    public ShimmerWall(SpellConfig config, String directory) {
        super(config, directory);

        distance = config.getDouble("distance");
        height = config.getDouble("height");
        width = config.getDouble("width");
        hits = config.getInt("hits");

        level = getEnumLogErrors(PersistenceLevel.class, config, directory, "dispel-level", PersistenceLevel.NORMAL);
    }

    private final double distance;
    private final double height;
    private final double width;
    private final int hits;
    private final PersistenceLevel level;

    @Override
    public boolean cast(SpellCaster caster) {
        Location spawnLoc = caster.getLocation().add(caster.getFacingVector().setY(0).normalize().multiply(distance));

        ShimmerWallObject wallObject = new ShimmerWallObject(spawnLoc, caster, this);

        wallObject.setFacing(caster.getFacingVector().setY(0));
        wallObject.setSize(width, height);

        wallObject.setLevel(level);
        wallObject.setHits(hits);

        wallObject.run();
        return true;
    }
}
