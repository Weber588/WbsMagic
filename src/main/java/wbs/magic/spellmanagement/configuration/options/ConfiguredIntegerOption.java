package wbs.magic.spellmanagement.configuration.options;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.magic.spellmanagement.configuration.options.IntOptions.IntOption;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.lang.reflect.Method;

public class ConfiguredIntegerOption extends ConfiguredPrimitiveOption<Integer, IntOption> {

    static {
        Class<?> clazz = IntOption.class;
        int[] val = null;
        try {
            Method method = clazz.getDeclaredMethod("suggestions");
            val = (int[]) method.getDefaultValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        if (val != null && val.length > 0) {
            DEFAULT_SUGGESTIONS = val;
        } else {
            DEFAULT_SUGGESTIONS = new int[] {};
        }
    }

    public static final int[] DEFAULT_SUGGESTIONS;

    public ConfiguredIntegerOption(IntOption annotation) {
        super(annotation);

        OptionParameter param = new OptionParameter(optionName);
        for (int suggestion : annotation.suggestions()) {
            param.addSuggestion(Integer.toString(suggestion));
        }
        addParameter(param);
    }

    public ConfiguredIntegerOption(Integer value, Integer defaultValue, @NotNull String optionName, @NotNull String[] aliases, boolean saveToDefaults) {
        super(value, defaultValue, optionName, aliases, saveToDefaults);

        OptionParameter param = new OptionParameter(optionName);
        for (int suggestion : DEFAULT_SUGGESTIONS) {
            param.addSuggestion(Integer.toString(suggestion));
        }
        param.addSuggestion(String.valueOf(defaultValue));

        addParameter(param);
    }

    @Override
    protected String getOptionName(IntOption annotation) {
        return annotation.optionName();
    }

    @Override
    protected Integer getValue(IntOption annotation) {
        return annotation.defaultValue();
    }

    @Override
    protected Integer getValue(ConfigurationSection config, String key) throws InvalidConfigurationException {
        if (config.isInt(key)) {
            return config.getInt(key);
        } else {
            throw new InvalidConfigurationException("Invalid integer: " + config.getString(key));
        }
    }

    @Override
    protected Integer parseString(String asString) throws InvalidConfigurationException {
        try {
            return Integer.parseInt(asString);
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationException(e.getMessage());
        }
    }

    @Override
    protected String[] getAliases(IntOption annotation) {
        return annotation.aliases();
    }

    @Override
    public @NotNull Class<Integer> getValueClass() {
        return Integer.class;
    }

    @Override
    protected boolean getSaveToDefaults(IntOption annotation) {
        return annotation.saveToDefaults();
    }
}
