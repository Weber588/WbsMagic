package wbs.magic.spellmanagement.configuration.options;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.MagicSettings;
import wbs.magic.spellmanagement.configuration.options.LocationTargeterOptions.LocationTargeterOption;
import wbs.magic.targeters.TargeterManager;
import wbs.magic.targeters.location.LocationTargeter;
import wbs.magic.targeters.location.LocationTargeterManager;

import java.util.Objects;

public class ConfiguredLocationTargeter extends ConfiguredSpellOption<LocationTargeter, LocationTargeterOption> {
    private final static String TYPE_KEY = "type";
    private final static String RANGE_KEY = "range";
    private final static String COUNT_KEY = "count";

    @NotNull
    private final Class<? extends LocationTargeter> defaultTargeter;
    private final double defaultRange;
    private final int defaultCount;

    @NotNull
    private Class<? extends LocationTargeter> type;
    private double range;
    private int count;

    public ConfiguredLocationTargeter(LocationTargeterOption annotation) {
        super(annotation);

        defaultTargeter = annotation.defaultType();
        type = annotation.defaultType();

        defaultRange = annotation.defaultRange();
        range = annotation.defaultRange();

        defaultCount = annotation.defaultCount();
        count = annotation.defaultCount();

        addParameter(new OptionParameter(optionName, TargeterManager.getDefaultIds()));
        addParameter(new OptionParameter(optionName + "-" + RANGE_KEY, 5, 25, 100));
        addParameter(new OptionParameter(optionName + "-" + COUNT_KEY, 1, 2, 3, 4, 5));
    }

    @Override
    public void configure(ConfigurationSection config, String directory) {

        String error;

        if (config.isConfigurationSection(optionName)) { // Section format
            ConfigurationSection targeterSection = config.getConfigurationSection(optionName);
            Objects.requireNonNull(targeterSection);

            error = configureTargeter(targeterSection.getString(TYPE_KEY));
            if (error != null) {
                MagicSettings.getInstance().logError(error, directory + "/" + TYPE_KEY);
            }

            error = configureRange(targeterSection.getString(RANGE_KEY));
            if (error != null) {
                MagicSettings.getInstance().logError(error, directory + "/" + RANGE_KEY);
            }

            error = configureCount(targeterSection.getString(COUNT_KEY));
            if (error != null) {
                MagicSettings.getInstance().logError(error, directory + "/" + COUNT_KEY);
            }
        } else { // Legacy/flat format
            error = configureTargeter(config.getString("targeter"));
            if (error != null) {
                MagicSettings.getInstance().logError(error, directory + "/" + optionName);
            }

            String rangeFieldName = null;
            if (config.isString(optionName + "-" + RANGE_KEY)) {
                rangeFieldName = optionName + "-" + RANGE_KEY;
            } else if (config.isString(RANGE_KEY)) {
                rangeFieldName = RANGE_KEY;
            }

            if (rangeFieldName != null) {
                error = configureRange(config.getString(rangeFieldName));

                if (error != null) {
                    MagicSettings.getInstance().logError(error, directory + "/" + rangeFieldName);
                }
            }
        }
    }

    private String configureTargeter(String value) {
        if (value == null) {
            return null;
        }

        Class<? extends LocationTargeter> checkType = LocationTargeterManager.getTargeterType(value);

        if (checkType == null) {
            type = defaultTargeter;
            return "Invalid targeter: " + value + ".";
        }

        type = checkType;

        return null;
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

    private String configureCount(String value) {
        if (value == null) {
            return null;
        }

        try {
            count = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            count = defaultCount;
            return "Invalid count: " + value + ". Use an integer.";
        }
        return null;
    }

    @Override
    public String configure(String key, String value) {
        return null;
    }

    @Override
    public void setValue(LocationTargeter value) {
        type = value.getClass();
        range = value.getRange();
        count = value.getLocationCount();
    }

    @Override
    public @NotNull Class<LocationTargeter> getValueClass() {
        return LocationTargeter.class;
    }

    @Override
    public @Nullable LocationTargeter get() {
        LocationTargeter targeter = LocationTargeterManager.getTargeter(type);

        targeter.setRange(range);
        targeter.setLocationCount(count);

        return targeter;
    }

    @Override
    public @NotNull LocationTargeter getDefault() {
        LocationTargeter targeter = LocationTargeterManager.getTargeter(defaultTargeter);

        targeter.setRange(defaultRange);
        targeter.setLocationCount(defaultCount);

        return targeter;
    }

    @Override
    protected String getOptionName(LocationTargeterOption annotation) {
        return annotation.optionName();
    }

    @Override
    protected String[] getAliases(LocationTargeterOption annotation) {
        return annotation.aliases();
    }

    @Override
    protected boolean getSaveToDefaults(LocationTargeterOption annotation) {
        return annotation.saveToDefaults();
    }

    @Override
    public void writeToConfig(ConfigurationSection config) {
        config.set(optionName + "." + TYPE_KEY, LocationTargeterManager.getDefaultId(type));
        config.set(optionName + "." + RANGE_KEY, range);
        config.set(optionName + "." + COUNT_KEY, count);
    }
}
