package wbs.magic.spellmanagement.configuration.options;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.MagicSettings;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.lang.annotation.Annotation;

public abstract class ConfiguredPrimitiveOption<T, K extends Annotation> extends ConfiguredSpellOption<T, K> {

    private T value;
    protected final T defaultValue;

    public ConfiguredPrimitiveOption(K annotation) {
        super(annotation);

        value = getValue(annotation);
        defaultValue = getValue(annotation);
    }

    public ConfiguredPrimitiveOption(T value, T defaultValue, @NotNull String optionName, @NotNull String[] aliases, boolean saveToDefaults) {
        super(optionName, aliases, saveToDefaults);
        this.value = value;
        this.defaultValue = defaultValue;
    }

    protected abstract T getValue(K annotation);
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

    @Override
    public void configure(ConfigurationSection config, String directory) {
        if (config.contains(optionName)) {
            try {
                value = getValue(config, optionName);
            } catch (InvalidConfigurationException e) {
                MagicSettings.getInstance().logError(e.getMessage(), directory + "/" + optionName);
            }
            if (value == null) {
                value = defaultValue;
            }
        }
    }

    @Override
    public String configure(String key, String value) {
        if (key.trim().equalsIgnoreCase(optionName)) {
            try {
                this.value = parseString(value);
            } catch (InvalidConfigurationException e) {
                return e.getMessage();
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
