package wbs.magic.controls.conditions;

import wbs.magic.controls.EventDetails;

import java.util.List;

public class PitchCondition extends NumCompareCastCondition {
    public PitchCondition(List<String> args, String directory) {
        super(args, directory);
    }

    @Override
    public double getValue(EventDetails details) {
        return details.player.getLocation().getPitch();
    }
}
