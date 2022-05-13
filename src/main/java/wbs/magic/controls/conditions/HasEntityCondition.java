package wbs.magic.controls.conditions;

import wbs.magic.controls.CastTrigger;
import wbs.magic.controls.EventDetails;

import java.util.List;

public class HasEntityCondition extends BooleanCastCondition {
    public HasEntityCondition(List<String> args, String directory) {
        super(args, directory);
    }

    @Override
    protected boolean getBool(EventDetails details) {
        return details.getOtherEntity() != null;
    }

    @Override
    public String formatTriggerString(CastTrigger trigger, String triggerString) {
        return triggerString + " Entity";
    }
}
