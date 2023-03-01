package wbs.magic.spellmanagement.configuration.options;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.magic.spellmanagement.configuration.options.BoolOptions.BoolOption;
import wbs.utils.exceptions.InvalidConfigurationException;

public class ConfiguredBooleanOption extends ConfiguredPrimitiveOption<Boolean, BoolOption> {
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
        switch (asString.trim().toLowerCase()) {
            case "true":
            case "yes":
            case "1":
                return true;
            case "false":
            case "no":
            case "0":
                return false;
            default:
                throw new InvalidConfigurationException("Invalid boolean: " + asString);
        }
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
