package wbs.magic.controls.conditions;

import wbs.magic.controls.CastTrigger;
import wbs.magic.controls.EventDetails;

import java.util.List;

public class SneakCondition extends BooleanCastCondition {
    public SneakCondition(List<String> args, String directory) {
        super(args, directory);
    }

    @Override
    public boolean getBool(EventDetails details) {
        return details.player.isSneaking();
    }

    @Override
    public String formatTriggerString(CastTrigger trigger, String triggerString) {
        return "Sneak " + triggerString;
    }
}
