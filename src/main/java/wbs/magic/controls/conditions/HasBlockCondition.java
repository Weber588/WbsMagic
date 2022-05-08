package wbs.magic.controls.conditions;

import wbs.magic.controls.CastTrigger;
import wbs.magic.controls.EventDetails;

import java.util.List;

public class HasBlockCondition extends BooleanCastCondition {
    public HasBlockCondition(List<String> args, String directory) {
        super(args, directory);
    }

    @Override
    protected boolean getBool(EventDetails details) {
        return details.getBlock() != null;
    }

    @Override
    public String formatTriggerString(CastTrigger trigger, String triggerString) {
        return triggerString + " Block";
    }
}
