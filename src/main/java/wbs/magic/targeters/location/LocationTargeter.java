package wbs.magic.targeters.location;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import wbs.magic.SpellCaster;
import wbs.magic.targeters.Targeter;

import java.util.Collection;

public abstract class LocationTargeter extends Targeter {

    protected int locationCount = 1;

    /**
     * Get all targets of a specific Entity subclass
     * @param caster The spellcaster to get targets for
     * @return A set of clazz entities that meet the targeting criteria. May return an empty set, will never be null
     */
    public abstract Collection<Location> getLocations(SpellCaster caster);

    @Nullable
    public Location getAny(SpellCaster caster) {
        return getLocations(caster).stream().findAny().orElse(null);
    }

    public abstract String getNoTargetsMessage();

    public int getLocationCount() {
        return locationCount;
    }

    public void setLocationCount(int locationCount) {
        this.locationCount = locationCount;
    }
}
