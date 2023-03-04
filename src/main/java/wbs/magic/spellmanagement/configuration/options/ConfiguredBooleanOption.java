package wbs.magic.spellmanagement.configuration.options;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.magic.spellmanagement.configuration.options.BoolOptions.BoolOption;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.util.Arrays;
import java.util.List;

public class ConfiguredBooleanOption extends ConfiguredPrimitiveOption<Boolean, BoolOption> {
    public static final List<String> TRUE_STRINGS = Arrays.asList("true", "yes", "1", "on");
    public static final List<String> FALSE_STRINGS = Arrays.asList("false", "no", "0", "off");

    public static boolean boolFromString(String asString) throws InvalidConfigurationException {
        asString = asString.trim().toLowerCase();
        if (TRUE_STRINGS.contains(asString)) {
            return true;
        } else if (FALSE_STRINGS.contains(asString)) {
            return false;
        }

        throw new InvalidConfigurationException("Invalid boolean: " + asString);
    }

    public ConfiguredBooleanOption(BoolOption annotation) {
        super(annotation);

        addParameter(new OptionParameter(optionName, true, false));
    }

    @Override
    protected Boolean getValue(BoolOption annotation) {
        return annotation.defaultValue();
    }

    @Override
    protected Boolean[] getListDefaults(BoolOption annotation) {
        boolean[] defaultList = annotation.listDefaults();
        Boolean[] returnList = new Boolean[defaultList.length];
        for (int i = 0; i < defaultList.length; i++) {
            returnList[i] = defaultList[i];
        }
        return returnList;
    }

    @Override
    protected Boolean getValue(ConfigurationSection config, String key) throws InvalidConfigurationException {
        if (config.isBoolean(key)) {
            return config.getBoolean(key);
        } else {
            throw new InvalidConfigurationException("Invalid double: " + config.getString(key));
        }
    }

    @Override
    protected Boolean parseString(String asString) throws InvalidConfigurationException {
        return boolFromString(asString);
    }

    @Override
    public @NotNull Class<Boolean> getValueClass() {
        return Boolean.class;
    }

    @Override
    protected String getOptionName(BoolOption annotation) {
        return annotation.optionName();
    }

    @Override
    protected String[] getAliases(BoolOption annotation) {
        return annotation.aliases();
    }

    @Override
    protected boolean getSaveToDefaults(BoolOption annotation) {
        return annotation.saveToDefaults();
    }
}
