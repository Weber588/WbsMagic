package wbs.magic.spellmanagement.configuration.options;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.MagicSettings;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.TargeterManager;
import wbs.utils.util.WbsEnums;

import java.util.Objects;

public class ConfiguredTargeterOption extends ConfiguredSpellOption<GenericTargeter, TargeterOption> {

    private final static String TYPE_KEY = "type";
    private final static String RANGE_KEY = "range";
    private final static String ENTITY_TYPE_KEY = "entity-type";

    @NotNull
    private final Class<? extends GenericTargeter> defaultTargeter;
    private final double defaultRange;

    @NotNull
    private Class<? extends GenericTargeter> type;
    private double range;
    @NotNull
    private String entityType;

    public ConfiguredTargeterOption(TargeterOption annotation) {
        super(annotation);

        defaultTargeter = annotation.defaultType();
        type = annotation.defaultType();
        defaultRange = annotation.defaultRange();
        range = annotation.defaultRange();

        entityType = annotation.entityType();

        addParameter(new OptionParameter(optionName, TargeterManager.getDefaultIds()));
        addParameter(new OptionParameter(optionName + "-" + RANGE_KEY, 5, 25, 100));
        addParameter(new OptionParameter(optionName + "-" + ENTITY_TYPE_KEY, WbsEnums.toStringList(EntityType.class)));
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

            error = configureTargeter(targeterSection.getString(TYPE_KEY));
            if (error != null) {
                MagicSettings.getInstance().logError(error, directory + "/" + TYPE_KEY);
            }

            error = configureRange(targeterSection.getString(RANGE_KEY));
            if (error != null) {
                MagicSettings.getInstance().logError(error, directory + "/" + RANGE_KEY);
            }

            error = configureEntityType(targeterSection.getString(ENTITY_TYPE_KEY));
            if (error != null) {
                MagicSettings.getInstance().logError(error, directory + "/" + ENTITY_TYPE_KEY);
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

    @Override
    public String configure(String key, String value) {
        if (key.trim().equalsIgnoreCase(optionName)) {
            return configureTargeter(value);
        } else if (key.trim().equalsIgnoreCase(optionName + "-" + RANGE_KEY)) {
            return configureRange(value);
        } else if (key.trim().equalsIgnoreCase(optionName + "-" + ENTITY_TYPE_KEY)) {
            return configureEntityType(value);
        }

        return null;
    }

    @Override
    public void setValue(GenericTargeter value) {
        type = value.getClass();
        range = value.getRange();
        entityType = value.getEntityType().name();
    }

    private String configureEntityType(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        EntityType checkType = WbsEnums.getEnumFromString(EntityType.class, value);
        if (checkType == null) {
            return "Invalid entity type \"" + value + "\".";
        }

        entityType = value;

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

        GenericTargeter targeter = TargeterManager.getTargeter(type);
        targeter.setRange(range);
        if (!entityType.isEmpty()) {
            targeter.setEntityType(WbsEnums.getEnumFromString(EntityType.class, entityType));
        }
        return targeter;
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
