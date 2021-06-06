package wbs.magic.spells;

import org.jetbrains.annotations.Nullable;
import wbs.magic.annotations.RestrictWandControls;

public class ControlRestrictions {
    private boolean requireShift;
    private boolean dontRestrictLineOfSight;

    public ControlRestrictions(@Nullable RestrictWandControls annotationVersion) {
        if (annotationVersion != null) {
            requireShift = annotationVersion.requireShift();
            dontRestrictLineOfSight = annotationVersion.dontRestrictLineOfSight();
        }
    }

    public boolean requiresShift() {
        return requireShift;
    }
    public boolean dontRestrictLineOfSight() {
        return dontRestrictLineOfSight;
    }
}
