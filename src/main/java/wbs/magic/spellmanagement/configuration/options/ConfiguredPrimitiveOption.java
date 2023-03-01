package wbs.magic.spellmanagement.configuration.options;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.MagicSettings;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class ConfiguredPrimitiveOption<T, K extends Annotation> extends ConfiguredSpellOption<T, K> {

    public static final String VALUE_SPLIT_REGEX = "\\|";

    private T value;
    protected final T defaultValue;
    protected final List<T> valueList = new LinkedList<>();
    protected final List<T> defaultList = new LinkedList<>();

    public ConfiguredPrimitiveOption(K annotation) {
        super(annotation);

        value = getValue(annotation);
        defaultValue = getValue(annotation);

        List<T> asList = Arrays.asList(getListDefaults(annotation));
        valueList.addAll(asList);
        defaultList.addAll(asList);
    }

    public ConfiguredPrimitiveOption(T value, T defaultValue, @NotNull String optionName, @NotNull String[] aliases, boolean saveToDefaults) {
        super(optionName, aliases, saveToDefaults);
        this.value = value;
        this.defaultValue = defaultValue;
    }

    public ConfiguredPrimitiveOption(T value, T defaultValue, Collection<T> defaultList, @NotNull String optionName, @NotNull String[] aliases, boolean saveToDefaults) {
        super(optionName, aliases, saveToDefaults);
        this.value = value;
        this.defaultValue = defaultValue;
        this.defaultList.addAll(defaultList);
    }

    protected abstract T getValue(K annotation);
    protected abstract T[] getListDefaults(K annotation);
    protected abstract T getValue(ConfigurationSection config, String key) throws InvalidConfigurationException;
    protected abstract T parseString(String asString) throws InvalidConfigurationException;

    @Override
    public @Nullable T get() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public @NotNull T getDefault() {
        return defaultValue;
    }

    public @NotNull List<T> getList() {
        List<T> valueList = new LinkedList<>();

        // Add standard value so this still works when a single value
        // was provided.
        valueList.add(value);
        valueList.addAll(this.valueList);

        return valueList;
    }

    public @NotNull List<T> getDefaultList() {
        return new LinkedList<>(defaultList);
    }

    @Override
    public void configure(ConfigurationSection config, String directory) {
        if (config.contains(optionName)) {
            if (!config.isList(optionName)) {
                try {
                    value = getValue(config, optionName);
                } catch (InvalidConfigurationException e) {
                    MagicSettings.getInstance().logError(e.getMessage(), directory + "/" + optionName);
                }
            } else {
                List<String> stringList = config.getStringList(optionName);

                List<T> checkValueList = new LinkedList<>();

                for (String asString : stringList) {
                    try {
                        T checkValue = parseString(asString);
                        checkValueList.add(checkValue);
                    } catch (InvalidConfigurationException e) {
                        MagicSettings.getInstance().logError(e.getMessage(), directory + "/" + optionName);
                    }
                }

                if (checkValueList.size() > 0) {
                    value = checkValueList.get(0);
                    // Remove first entry as it's added in the list getter
                    checkValueList.remove(0);
                    valueList.clear();
                    valueList.addAll(checkValueList);
                }
            }

            if (value == null) {
                value = defaultValue;
            }
        }
    }

    @Override
    public String configure(String key, String value) {
        if (key.trim().equalsIgnoreCase(optionName)) {
            String[] split = value.split(VALUE_SPLIT_REGEX);
            List<T> checkValueList = new LinkedList<>();

            for (String entry : split) {
                try {
                    T check = parseString(entry);
                    checkValueList.add(check);
                } catch (InvalidConfigurationException e) {
                    return e.getMessage();
                }
            }

            if (checkValueList.size() > 0) {
                this.value = checkValueList.get(0);
                // Remove first entry as it's added in the list getter
                checkValueList.remove(0);
                valueList.clear();
                valueList.addAll(checkValueList);
            }

            if (this.value == null) {
                this.value = defaultValue;
            }
        }
        return null;
    }

    @Override
    public void writeToConfig(ConfigurationSection config) {
        config.set(optionName, value);
    }

    @Override
    public String toString() {
        return "ConfiguredPrimitiveOption{" +
                "value=" + value +
                ", defaultValue=" + defaultValue +
                '}';
    }
}
