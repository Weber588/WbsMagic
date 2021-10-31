package wbs.magic.controls.conditions;

import wbs.magic.SpellCaster;
import wbs.magic.controls.CastTrigger;
import wbs.magic.controls.EventDetails;

import java.util.List;

public class ConcentratingCondition extends BooleanCastCondition {
    public ConcentratingCondition(List<String> args, String directory) {
        super(args, directory);
    }

    @Override
    public boolean getBool(EventDetails details) {
        return SpellCaster.getCaster(details.player).isConcentrating();
    }
}
