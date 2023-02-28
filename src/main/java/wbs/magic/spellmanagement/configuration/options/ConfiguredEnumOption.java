package wbs.magic.spellmanagement.configuration.options;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.MagicSettings;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.utils.util.WbsEnums;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ConfiguredEnumOption extends ConfiguredSpellOption<Enum, EnumOption> {

    private final Class<? extends Enum> enumType;
    private final String defaultValue;
    private String value;

    public ConfiguredEnumOption(EnumOption annotation) {
        super(annotation);

        enumType = annotation.enumType();
        defaultValue = annotation.defaultValue();
        value = annotation.defaultValue();

        List<String> enumSuggestions = Arrays.stream(enumType.getEnumConstants())
                .map(Enum::name)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        addParameter(new OptionParameter(optionName, enumSuggestions));

    }

    private Enum toEnum(String value) {
        return WbsEnums.getEnumFromString(enumType, value);
    }

    @Override
    public void configure(ConfigurationSection config, String directory) {
        String checkValue = config.getString(optionName, defaultValue);

        Enum check = toEnum(checkValue);

        if (check == null) {
            MagicSettings.getInstance().logError("Invalid value \"" + checkValue + "\" for option type " + enumType.getSimpleName(), directory + "/" + optionName);
        } else {
            value = checkValue;
        }
    }

    @Override
    public String configure(String key, String value) {
        if (key.trim().equalsIgnoreCase(optionName)) {
            Enum check = toEnum(value);

            if (check == null) {
                return "Invalid value \"" + value + "\" for type " + enumType.getSimpleName();
            } else {
                this.value = value;
            }
        }
        return null;
    }

    @Override
    public void setValue(Enum value) {
        this.value = value.name();
    }

    @Override
    public @NotNull Class<Enum> getValueClass() {
        return Enum.class;
    }

    @Override
    public @Nullable Enum get() {
        return toEnum(value);
    }

    @Override
    public @NotNull Enum getDefault() {
        return toEnum(defaultValue);
    }

    @Override
    protected String getOptionName(EnumOption annotation) {
        return annotation.optionName();
    }

    @Override
    protected String[] getAliases(EnumOption annotation) {
        return annotation.aliases();
    }

    @Override
    protected boolean getSaveToDefaults(EnumOption annotation) {
        return annotation.saveToDefaults();
    }

    @Override
    public void writeToConfig(ConfigurationSection config) {
        config.set(optionName, value);
    }
}
