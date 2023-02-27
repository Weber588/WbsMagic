package wbs.magic.spellmanagement.configuration.options;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.MagicSettings;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.TargeterManager;

import java.util.Objects;

public class ConfiguredTargeterOption extends ConfiguredSpellOption<GenericTargeter, TargeterOption> {

    @NotNull
    private final Class<? extends GenericTargeter> defaultTargeter;
    private final double defaultRange;

    @NotNull
    private Class<? extends GenericTargeter> type;
    private double range;

    public ConfiguredTargeterOption(TargeterOption annotation) {
        super(annotation);

        defaultTargeter = annotation.defaultType();
        type = annotation.defaultType();
        defaultRange = annotation.defaultRange();
        range = annotation.defaultRange();

        addParameter(new OptionParameter(optionName, TargeterManager.getDefaultIds()));
        addParameter(new OptionParameter(optionName + "-range", 5, 25, 100));
    }

    @Override
    protected String getOptionName(TargeterOption annotation) {
        return annotation.optionName();
    }

    @Override
    protected String[] getAliases(TargeterOption annotation) {
        return annotation.aliases();
    }

    @Override
    public void configure(ConfigurationSection config, String directory) {
        String error;

        if (config.isConfigurationSection(optionName)) { // Section format
            ConfigurationSection targeterSection = config.getConfigurationSection(optionName);
            Objects.requireNonNull(targeterSection);

            error = configureTargeter(targeterSection.getString("type"));
            if (error != null) {
                MagicSettings.getInstance().logError(error, directory + "/type");
            }

            error = configureRange(targeterSection.getString("range"));
            if (error != null) {
                MagicSettings.getInstance().logError(error, directory + "/range");
            }
        } else { // Legacy/flat format
            error = configureTargeter(config.getString("targeter"));
            if (error != null) {
                MagicSettings.getInstance().logError(error, directory + "/" + optionName);
            }

            String rangeFieldName = null;
            if (config.isString(optionName + "-range")) {
                rangeFieldName = optionName + "-range";
            } else if (config.isString("range")) {
                rangeFieldName = "range";
            }

            if (rangeFieldName != null) {
                error = configureRange(config.getString(rangeFieldName));

                if (error != null) {
                    MagicSettings.getInstance().logError(error, directory + "/" + rangeFieldName);
                }
            }
        }
    }

    @Override
    public String configure(String key, String value) {
        if (key.trim().equalsIgnoreCase(optionName)) {
            return configureTargeter(value);
        } else if (key.trim().equalsIgnoreCase(optionName + "-range")) {
            return configureRange(value);
        }

        return null;
    }

    @Override
    public void setValue(GenericTargeter value) {
        type = value.getClass();
        range = value.getRange();
    }

    private String configureRange(String value) {
        if (value == null) {
            return null;
        }

        try {
            range = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            range = defaultRange;
            return "Invalid range: " + value + ". Use a number.";
        }
        return null;
    }

    private String configureTargeter(String value) {
        if (value == null) {
            return null;
        }

        Class<? extends GenericTargeter> checkType = TargeterManager.getTargeterType(value);

        if (checkType == null) {
            type = defaultTargeter;
            return "Invalid targeter: " + value + ".";
        }

        type = checkType;

        return null;
    }

    @Override
    public @NotNull Class<GenericTargeter> getValueClass() {
        return GenericTargeter.class;
    }

    @Nullable
    @Override
    public GenericTargeter get() {
        if (range <= 0) {
            range = TargeterManager.getDefaultRange(type);
        }

        return TargeterManager.getTargeter(type);
    }

    @NotNull
    @Override
    public GenericTargeter getDefault() {
        double range = defaultRange;
        if (range <= 0) {
            range = TargeterManager.getDefaultRange(type);
        }

        GenericTargeter targeter = TargeterManager.getTargeter(type);

        targeter.setRange(range);

        return targeter;
    }

    @Override
    protected boolean getSaveToDefaults(TargeterOption annotation) {
        return annotation.saveToDefaults();
    }

    @Override
    public void writeToConfig(ConfigurationSection config) {
        config.set(optionName + ".type", TargeterManager.getDefaultId(type));
        config.set(optionName + ".range", range);
    }


}
