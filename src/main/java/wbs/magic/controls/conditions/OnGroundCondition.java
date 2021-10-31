package wbs.magic.controls.conditions;

import wbs.magic.controls.EventDetails;

import java.util.List;

public class OnGroundCondition extends BooleanCastCondition {
    public OnGroundCondition(List<String> args, String directory) {
        super(args, directory);
    }

    @Override
    public boolean getBool(EventDetails details) {
        return details.player.isOnGround();
    }
}
