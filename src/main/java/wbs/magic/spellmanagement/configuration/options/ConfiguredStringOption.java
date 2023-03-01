package wbs.magic.spellmanagement.configuration.options;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.magic.spellmanagement.configuration.options.StringOptions.StringOption;
import wbs.utils.exceptions.InvalidConfigurationException;

public class ConfiguredStringOption extends ConfiguredPrimitiveOption<String, StringOption> {
    public ConfiguredStringOption(StringOption annotation) {
        super(annotation);

        OptionParameter param = new OptionParameter(optionName, (Object[]) annotation.suggestions());
        param.addSuggestion(getDefault());
        addParameter(param);
    }

    public ConfiguredStringOption(String value, String defaultValue, @NotNull String optionName, @NotNull String[] aliases, boolean saveToDefaults) {
        super(value, defaultValue, optionName, aliases, saveToDefaults);

        OptionParameter param = new OptionParameter(optionName);
        if (!defaultValue.isEmpty()) {
            param.addSuggestion(defaultValue);
        }

        addParameter(param);
    }

    @Override
    protected String getValue(StringOption annotation) {
        return annotation.defaultValue();
    }

    @Override
    protected String[] getListDefaults(StringOption annotation) {
        return annotation.listDefaults();
    }

    @Override
    protected String getValue(ConfigurationSection config, String key) throws InvalidConfigurationException {
        return config.getString(key);
    }

    @Override
    protected String parseString(String asString) throws InvalidConfigurationException {
        return asString;
    }

    @Override
    public @NotNull Class<String> getValueClass() {
        return String.class;
    }

    @Override
    protected String getOptionName(StringOption annotation) {
        return annotation.optionName();
    }

    @Override
    protected String[] getAliases(StringOption annotation) {
        return annotation.aliases();
    }

    @Override
    protected boolean getSaveToDefaults(StringOption annotation) {
        return annotation.saveToDefaults();
    }
}
