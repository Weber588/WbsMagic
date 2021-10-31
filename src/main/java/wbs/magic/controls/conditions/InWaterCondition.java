package wbs.magic.controls.conditions;

import wbs.magic.controls.EventDetails;
import wbs.utils.util.entities.WbsEntityUtil;

import java.util.List;

public class InWaterCondition extends BooleanCastCondition {
    public InWaterCondition(List<String> args, String directory) {
        super(args, directory);
    }

    @Override
    public boolean getBool(EventDetails details) {
        return WbsEntityUtil.isInWater(details.player);
    }
}
