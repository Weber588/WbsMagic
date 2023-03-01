package wbs.magic.spellmanagement.configuration.options;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.magic.spellmanagement.configuration.options.DoubleOptions.DoubleOption;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.lang.reflect.Method;

public class ConfiguredDoubleOption extends ConfiguredPrimitiveOption<Double, DoubleOption> {

    static {
        Class<?> clazz = DoubleOption.class;
        double[] val = null;
        try {
            Method method = clazz.getDeclaredMethod("suggestions");
            val = (double[]) method.getDefaultValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        if (val != null && val.length > 0) {
            DEFAULT_SUGGESTIONS = val;
        } else {
            DEFAULT_SUGGESTIONS = new double[] {};
        }
    }

    public static final double[] DEFAULT_SUGGESTIONS;

    public ConfiguredDoubleOption(DoubleOption annotation) {
        super(annotation);

        OptionParameter param = new OptionParameter(optionName);
        for (double suggestion : annotation.suggestions()) {
            param.addSuggestion(Double.toString(suggestion));
        }
        param.addSuggestion(String.valueOf(defaultValue));

        addParameter(param);
    }

    public ConfiguredDoubleOption(Double value, Double defaultValue, @NotNull String optionName, @NotNull String[] aliases, boolean saveToDefaults) {
        super(value, defaultValue, optionName, aliases, saveToDefaults);

        OptionParameter param = new OptionParameter(optionName);
        for (double suggestion : DEFAULT_SUGGESTIONS) {
            param.addSuggestion(Double.toString(suggestion));
        }
        param.addSuggestion(String.valueOf(defaultValue));

        addParameter(param);
    }

    @Override
    public @NotNull Class<Double> getValueClass() {
        return Double.class;
    }

    @Override
    protected String getOptionName(DoubleOption annotation) {
        return annotation.optionName();
    }

    @Override
    protected Double getValue(DoubleOption annotation) {
        return annotation.defaultValue();
    }

    @Override
    protected Double[] getListDefaults(DoubleOption annotation) {
        double[] defaultList = annotation.listDefaults();
        Double[] returnList = new Double[defaultList.length];
        for (int i = 0; i < defaultList.length; i++) {
            returnList[i] = defaultList[i];
        }
        return returnList;
    }

    @Override
    protected Double getValue(ConfigurationSection config, String key) throws InvalidConfigurationException {
        if (config.isDouble(key) || config.isInt(key)) { // Can interpret either value as a double
            return config.getDouble(key);
        } else {
            throw new InvalidConfigurationException("Invalid double: " + config.getString(key));
        }
    }

    @Override
    protected Double parseString(String asString) throws InvalidConfigurationException {
        try {
            return Double.parseDouble(asString);
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationException(e.getMessage());
        }
    }

    @Override
    protected String[] getAliases(DoubleOption annotation) {
        return annotation.aliases();
    }

    @Override
    protected boolean getSaveToDefaults(DoubleOption annotation) {
        return annotation.saveToDefaults();
    }
}
