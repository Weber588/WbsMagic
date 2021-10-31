package wbs.magic.controls.conditions;

import wbs.magic.controls.CastTrigger;
import wbs.magic.controls.EventDetails;
import wbs.magic.exceptions.EventNotSupportedException;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.util.List;

public abstract class BooleanCastCondition extends CastCondition {

    private final boolean compare;
    public BooleanCastCondition(List<String> args, String directory) {
        super(args, directory);

        if (args.size() > 0) {
            String boolArg = args.get(0);

            if (!(boolArg.equalsIgnoreCase("yes") ||
                    boolArg.equalsIgnoreCase("true") ||
                    boolArg.equalsIgnoreCase("no") ||
                    boolArg.equalsIgnoreCase("false")))
            {
                throw new InvalidConfigurationException("Boolean conditions require either true or false." + getUsage());
            }

            String compareString = args.get(0);
            compare = compareString.equalsIgnoreCase("true") ||
                            compareString.equalsIgnoreCase("yes");
        } else {
            compare = true;
        }
    }

    @Override
    public final boolean checkInternal(EventDetails details) throws EventNotSupportedException {
        return getBool(details) == compare;
    }

    @Override
    public String getUsage() {
        return "[true|false]";
    }

    protected abstract boolean getBool(EventDetails details) throws EventNotSupportedException;

    public boolean getComparison() {
        return compare;
    }
}
