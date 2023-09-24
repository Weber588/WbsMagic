package wbs.magic.targeters.location;

import org.bukkit.Location;
import wbs.magic.SpellCaster;

import java.util.Collection;
import java.util.Collections;

public class SightLocationTargeter extends LocationTargeter {
    public static final double DEFAULT_RANGE = 50;

    @Override
    public Collection<Location> getLocations(SpellCaster caster) {
        return Collections.singleton(caster.getTargetPos(range));
    }

    @Override
    public String getNoTargetsMessage() {
        return "You need line of sight to a block!";
    }
}
