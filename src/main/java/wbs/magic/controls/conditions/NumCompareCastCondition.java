package wbs.magic.controls.conditions;

import org.jetbrains.annotations.NotNull;
import wbs.magic.controls.CastTrigger;
import wbs.magic.controls.EventDetails;
import wbs.magic.exceptions.EventNotSupportedException;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class NumCompareCastCondition extends CastCondition {

    private enum Operator {
        EQUALS("="),
        NOT_EQUALS("!="),

        LESS_THAN("<"),
        LESS_THAN_OR_EQUAL_TO("<="),

        GREATER_THAN(">"),
        GREATER_THAN_OR_EQUAL_TO("<=");

        public final String label;
        Operator(String label) {
            this.label = label;
        }

        public boolean compare(double val1, double val2) {
            switch (this) {
                case EQUALS:
                    return val1 == val2;
                case NOT_EQUALS:
                    return val1 != val2;
                case LESS_THAN:
                    return val1 < val2;
                case LESS_THAN_OR_EQUAL_TO:
                    return val1 <= val2;
                case GREATER_THAN:
                    return val1 > val2;
                case GREATER_THAN_OR_EQUAL_TO:
                    return val1 >= val2;
            }
            return false;
        }

        public static Operator fromLabel(String label) {
            for (Operator operator : values()) {
                if (operator.label.equalsIgnoreCase(label)) {
                    return operator;
                }
            }
            return null;
        }
    }

    @NotNull
    protected final Operator operator;
    protected final double compareValue;

    public NumCompareCastCondition(List<String> args, String directory) {
        super(args, directory);
        double tempValue = Double.NaN;

        if (args.size() == 0) {
            throw new InvalidConfigurationException();
        }
        String arg1 = args.get(0);
        try {
            tempValue = Double.parseDouble(arg1);
        } catch (NumberFormatException ignored) {}

        // Dumb stuff with final initializers interacting with try/catch blocks
        if (!Double.isNaN(tempValue)) {
            compareValue = tempValue;
            operator = Operator.EQUALS;
            return;
        }

        Operator checkOperator = Operator.fromLabel(arg1);
        if (checkOperator == null) {
            throw new InvalidConfigurationException("Invalid operator/number: " + arg1 + ". " + getUsage());
        } else {
            operator = checkOperator;
        }

        String arg2 = args.get(1);
        try {
            tempValue = Double.parseDouble(arg2);
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationException("Invalid number: " + arg2  + ". " + getUsage());
        }

        compareValue = tempValue;
    }

    @Override
    public final boolean checkInternal(EventDetails details) throws EventNotSupportedException {
        return operator.compare(getValue(details), compareValue);
    }

    public String getUsage() {
        return "Usage: [operator] <number>. Valid operators: " +
                Arrays.stream(Operator.values()).map(operator -> operator.label).collect(Collectors.joining(", "));
    }

    public abstract double getValue(EventDetails details) throws EventNotSupportedException;

    public abstract String getValueString();

    @Override
    public final String formatTriggerString(CastTrigger trigger, String triggerString) {
        return triggerString + " (" + getValueString() + " " + operator.label + " " + compareValue + ")";
    }
}
