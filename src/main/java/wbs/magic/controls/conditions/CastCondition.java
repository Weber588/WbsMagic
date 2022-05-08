package wbs.magic.controls.conditions;

import org.jetbrains.annotations.Nullable;
import wbs.magic.MagicSettings;
import wbs.magic.controls.CastTrigger;
import wbs.magic.controls.EventDetails;
import wbs.magic.exceptions.EventNotSupportedException;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class CastCondition {

    @Nullable
    public static CastCondition getCondition(String conditionString, String directory) {
        String[] args = conditionString.split(" ");

        String name = args[0];

        directory += ":" + name;

        Class<? extends CastCondition> conditionClass = registeredConditions.get(name.toUpperCase());

        if (conditionClass != null) {
            return buildCondition(conditionClass, args, directory);
        }

        for (String regex : registeredConditions.keySet()) {
            if (name.matches(regex)) {
                return buildCondition(registeredConditions.get(regex), args, directory);
            }
        }

        MagicSettings.getInstance().logError("Invalid condition: " + name, directory);
        return null;
    }

    @Nullable
    public static CastCondition buildCondition(Class<? extends CastCondition> conditionClass, String[] args, String directory) {
        List<String> argList = new LinkedList<>(Arrays.asList(args).subList(1, args.length));

        try {
            Constructor<? extends CastCondition> constructor = conditionClass.getConstructor(List.class, String.class);

            return constructor.newInstance(argList, directory);
        } catch (NoSuchMethodException e) {
            MagicSettings.getInstance().logError("Internal error. CastConditions require a (List<String>, String) constructor.", directory);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof InvalidConfigurationException) {
                InvalidConfigurationException configException = (InvalidConfigurationException) e.getTargetException();

                MagicSettings.getInstance().logError(configException.getMessage(), directory);
            } else {
                e.printStackTrace();
                MagicSettings.getInstance().logError("An unknown error occurred. Check console for details.", directory);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            MagicSettings.getInstance().logError("An unknown error occurred. Check console for details.", directory);
        }

        return null;
    }

    public static void loadConditions() {
        registeredConditions.clear();

        registerCondition("MATERIAL|BLOCK", BlockIsMaterialCondition.class);
        registerCondition("(IS_)?CONCENTRATING", ConcentratingCondition.class);
        registerCondition("(HAS_)?BLOCK", HasBlockCondition.class);
        registerCondition("(HAS_)?ENTITY", HasEntityCondition.class);
        registerCondition("HEALTH", HealthCondition.class);
        registerCondition("(IN_)?WATER", InWaterCondition.class);
        registerCondition("LIGHT(_?LEVEL)?", LightLevelCondition.class);
        registerCondition("(LOOK(ING)?|FACING)_DOWN", LookingDownCondition.class);
        registerCondition("(LOOK(ING)?|FACING)_UP", LookingUpCondition.class);
        registerCondition("ON_GROUND", OnGroundCondition.class);
        registerCondition("PITCH", PitchCondition.class);
        registerCondition("SNEAK(ING)?", SneakCondition.class);
    }

    private static final HashMap<String, Class<? extends CastCondition>> registeredConditions = new HashMap<>();
    public static void registerCondition(String regex, Class<? extends CastCondition> conditionClass) {
        registeredConditions.put(regex, conditionClass);
    }

    protected final List<String> args;

    public CastCondition(List<String> args, String directory) throws InvalidConfigurationException {
        this.args = args;
    }

    protected abstract boolean checkInternal(EventDetails details) throws EventNotSupportedException;


    /**
     * Run this condition if the event is supported,
     * or return true otherwise.
     * @param details The details to check.
     * @return True if the event doesn't match, or the result
     * of {@link #checkInternal(EventDetails)} if it does.
     */
    public boolean check(EventDetails details) {
        try {
            return checkInternal(details);
        } catch (EventNotSupportedException e) {
            return true;
        }
    }

    public abstract String getUsage();

    /**
     * Format the display for a given trigger, assuming this condition is enforced.
     * @param trigger The trigger the string is being formatted for.
     * @param triggerString The string so far, possibly with other conditions already added.
     * @return The new formatted string, or the same string if no change is needed.
     */
    public String formatTriggerString(CastTrigger trigger, String triggerString) {
        return triggerString;
    }

    public Collection<Class<? extends CastCondition>> getConflicts() {
        return new LinkedList<>();
    }
}
