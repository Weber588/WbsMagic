package wbs.magic.spellmanagement.configuration;

import org.jetbrains.annotations.Nullable;
import wbs.magic.spellmanagement.configuration.RestrictWandControls;

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
