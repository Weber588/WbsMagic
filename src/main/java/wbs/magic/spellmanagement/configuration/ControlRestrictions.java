package wbs.magic.spellmanagement.configuration;

import org.jetbrains.annotations.Nullable;
import wbs.magic.controls.WandControl;
import wbs.magic.spellmanagement.configuration.RestrictWandControls;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ControlRestrictions {
    private boolean requireShift;
    private boolean dontRestrictLineOfSight;
    private final List<WandControl> validControls = new LinkedList<>();

    public ControlRestrictions(@Nullable RestrictWandControls annotationVersion) {
        if (annotationVersion != null) {
            requireShift = annotationVersion.requireShift();
            dontRestrictLineOfSight = annotationVersion.dontRestrictLineOfSight();
            validControls.addAll(Arrays.asList(annotationVersion.limitedControls()));
        }
    }

    public boolean requiresShift() {
        return requireShift;
    }
    public boolean dontRestrictLineOfSight() {
        return dontRestrictLineOfSight;
    }

    public List<WandControl> validControls() {
        return new LinkedList<>(validControls);
    }
}
