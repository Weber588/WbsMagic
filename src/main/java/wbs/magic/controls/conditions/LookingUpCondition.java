package wbs.magic.controls.conditions;

import wbs.magic.controls.CastTrigger;
import wbs.magic.controls.EventDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class LookingUpCondition extends BooleanCastCondition {
    public LookingUpCondition(List<String> args, String directory) {
        super(args, directory);
    }

    @Override
    protected boolean getBool(EventDetails details)  {
        return details.player.getLocation().getPitch() <= -85;
    }

    @Override
    public String formatTriggerString(CastTrigger trigger, String triggerString) {
        return triggerString + " Up";
    }

    @Override
    public Collection<Class<? extends CastCondition>> getConflicts() {
        return Arrays.asList(LookingDownCondition.class, PitchCondition.class);
    }
}
