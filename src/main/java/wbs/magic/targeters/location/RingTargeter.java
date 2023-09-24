package wbs.magic.targeters.location;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import wbs.magic.SpellCaster;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class RingTargeter extends LocationTargeter {
    public static final double DEFAULT_RANGE = 5;

    @Override
    public Collection<Location> getLocations(SpellCaster caster) {
        Vector toRotate = new Vector(range, 0, 0);

        List<Location> locations = new LinkedList<>();

        for (int i = 0; i < locationCount; i++) {
            locations.add(caster.getLocation().add(toRotate));
            toRotate.rotateAroundY(2 * Math.PI / locationCount);
        }

        return locations;
    }

    @Override
    public String getNoTargetsMessage() {
        return "No valid spots found!";
    }
}
