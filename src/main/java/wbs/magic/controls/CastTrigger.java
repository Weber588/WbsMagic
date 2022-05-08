package wbs.magic.controls;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import wbs.magic.MagicSettings;
import wbs.magic.controls.conditions.CastCondition;
import wbs.magic.controls.conditions.SneakCondition;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.configuration.WbsConfigReader;

import java.util.*;
import java.util.stream.Collectors;

public class CastTrigger {

    private final WandControl control;
    private final List<CastCondition> conditions = new LinkedList<>();
    private int priority = Integer.MAX_VALUE;

    public CastTrigger(WandControl control) {
        this.control = control;
    }

    public CastTrigger(ConfigurationSection section, String directory) {
        MagicSettings settings = MagicSettings.getInstance();
        WbsConfigReader.requireNotNull(section, "event", settings, directory);

        String eventString = section.getString("event");
        assert eventString != null;

        control = WbsEnums.getEnumFromString(WandControl.class, eventString);
        if (control == null) {
            throw new InvalidConfigurationException("Invalid event: " + eventString + ". " +
                    "Please choose from the following: " +
                    String.join(", ", WbsEnums.toStringList(WandControl.class)));
        }

        priority = section.getInt("priority", Integer.MAX_VALUE);

        Set<Class<? extends CastCondition>> conflicts = new HashSet<>();
        Set<CastCondition> conditionsToRemove = new HashSet<>();

        List<String> conditionStrings = section.getStringList("conditions");

        Map<CastCondition, String> conditionStringsMap = new HashMap<>();
        for (String conditionString : conditionStrings) {
            CastCondition condition = CastCondition.getCondition(conditionString, directory + "/conditions");

            conditionStringsMap.put(condition, conditionString);

            if (condition != null) {
                if (conflicts.contains(condition.getClass())) {
                    conditionsToRemove.add(condition);
                }

                boolean conflicted = false;
                for (Class<? extends CastCondition> conflict : condition.getConflicts()) {
                    for (CastCondition existingCondition : conditions) {
                        if (conflict.isAssignableFrom(existingCondition.getClass())) {
                            conflicts.add(conflict);
                            conflicts.add(condition.getClass());

                            conditionsToRemove.add(condition);
                            conditionsToRemove.add(existingCondition);

                            String conditionName = conditionString.split(" ")[0];
                            String existingConditionName = conditionStringsMap.get(existingCondition).split(" ")[0];

                            settings.logError(
                                    "Condition \"" +  conditionName
                                            + "\" conflicts with \""
                                            + existingConditionName + " \"", directory);

                            conflicted = true;
                            break;
                        }
                    }
                    if (conflicted) break;
                }

                if (!conflicted) {
                    conditions.add(condition);
                }
            }
        }

        int conflicted = 0;
        for (CastCondition toRemove : conditionsToRemove) {
            if (conditions.remove(toRemove)) {
                conflicted++;
            }
        }

        if (conflicted > 0) {
            settings.logError(conflicted + " conflicting conditions.", directory);
        }
    }

    public boolean check(EventDetails eventDetails) {
        if (this.control.getEvents().contains(eventDetails.event.getClass())) {
            return checkConditions(eventDetails);
        }
        return false;
    }


    public void addCondition(CastCondition condition) {
        conditions.add(condition);
    }

    @SuppressWarnings("unchecked")
    public <T extends CastCondition> List<T> getConditions(Class<T> clazz) {
        return conditions.stream()
                .filter(condition -> condition.getClass().equals(clazz))
                .map(condition -> (T) condition)
                .collect(Collectors.toList());
    }

    public boolean checkConditions(EventDetails event) {
        boolean success;

        for (CastCondition condition : conditions) {
            success = condition.check(event);
            if (!success) return false;
        }

        return true;
    }

    @Override
    public String toString() {
        String asString = WbsEnums.toPrettyString(control);

        for (CastCondition condition : conditions) {
            asString = condition.formatTriggerString(this, asString);
        }

        return asString;
    }

    public int getPriority() {
        return priority;
    }

    public WandControl getControl() {
        return control;
    }

    public boolean hasCondition(Class<? extends CastCondition> clazz) {
        for (CastCondition condition : conditions) {
            if (condition.getClass().equals(clazz)) return true;
        }
        return false;
    }

    public boolean runFor(Event event) {
        for (Class<? extends Event> eventType : control.getEvents()) {
            if (event.getClass().equals(eventType)) {
                return true;
            }
        }
        return false;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
