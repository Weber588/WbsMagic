package wbs.magic.spellmanagement.configuration.options;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

/**
 * An option that holds the information required to create an object of type
 * {@link T} from a set of parameters, the values of which are defined in an
 * annotation & by the user at runtime.<br/>
 * If the user should be able to configure options of that type at runtime (such
 * as from cast or tempwand commands), an {@link OptionParameter} should be made
 * available. These optionParameters do not need to reflect the optionName of that
 * value, but often will for primitives or other objects that can be configured from
 * a single spell.
 * @param <T> The type of object produced when configured
 * @param <K> The annotation type that configures the defaults for this option
 */
public abstract class ConfiguredSpellOption<T, K extends Annotation> {

    @NotNull
    private final List<OptionParameter> parameters = new LinkedList<>();

    @NotNull
    protected final String optionName;
    @NotNull
    protected final String[] aliases;

    private final boolean saveToDefaults;

    public ConfiguredSpellOption(K annotation) {
        optionName = getOptionName(annotation);
        aliases = getAliases(annotation);
        saveToDefaults = getSaveToDefaults(annotation);
    }

    public ConfiguredSpellOption(@NotNull String optionName, @NotNull String[] aliases, boolean saveToDefaults) {
        this.optionName = optionName;
        this.aliases = aliases;
        this.saveToDefaults = saveToDefaults;
    }

    public List<OptionParameter> getOptionParameters() {
        return new LinkedList<>(parameters);
    }

    protected final void addParameter(OptionParameter parameter) {
        parameters.add(parameter);
    }

    /**
     * Configure the values from a given spell configuration
     * @param config The configuration section to read from
     */
    public abstract void configure(ConfigurationSection config, String directory);
    /**
     * Configure the values from a given spell configuration
     * @param key The key, typically from a command, to determine if this method call should affect this option
     * @param value The value as a string, to be parsed as {@link T}
     * @return Any errors encountered
     */
    public abstract String configure(String key, String value);

    public abstract void setValue(T value);

    @NotNull
    public abstract Class<T> getValueClass();

    @Nullable
    public abstract T get();

    @NotNull
    public abstract T getDefault();

    public boolean saveToDefaults() {
        return saveToDefaults;
    }

    protected abstract String getOptionName(K annotation);
    protected abstract String[] getAliases(K annotation);

    protected abstract boolean getSaveToDefaults(K annotation);

    public abstract void writeToConfig(ConfigurationSection config);
}
