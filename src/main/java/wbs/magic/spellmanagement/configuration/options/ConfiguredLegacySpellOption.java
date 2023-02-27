package wbs.magic.spellmanagement.configuration.options;

import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.MagicSettings;
import wbs.magic.WbsMagic;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.utils.util.WbsEnums;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ConfiguredLegacySpellOption extends ConfiguredSpellOption<Object, SpellOption> {
    private final SpellOptionType type;

    private Object value;
    private final Object defaultValue;

    public ConfiguredLegacySpellOption(SpellOption annotation) {
        super(annotation);

        type = annotation.type();
        switch (type) {
            case INT:
                value = annotation.defaultInt();
                break;
            case BOOLEAN:
                value = annotation.defaultBool();
                break;
            case STRING:
                value = annotation.defaultString();
                break;
            case DOUBLE:
                value = annotation.defaultDouble();
                break;
            case STRING_LIST:
                value = annotation.defaultStrings();
                break;
            case PARTICLE:
                value = annotation.defaultParticle();
                break;
        }

        defaultValue = value;


        switch (type) {
            case INT:
                addParameter(new OptionParameter(optionName, 1, 5, 10, 25));
                break;
            case BOOLEAN:
                addParameter(new OptionParameter(optionName, true, false));
                break;
            case STRING:
                if (annotation.enumType() != Enum.class) {
                    //noinspection unchecked
                    List<String> enumConstants = WbsEnums.toStringList((Class<? extends Enum<?>>) annotation.enumType());
                    OptionParameter enumParam = new OptionParameter(optionName, enumConstants);
                    addParameter(enumParam);
                }
                break;
            case DOUBLE:
                addParameter(new OptionParameter(optionName, 0.5, 1.0, 5.0, 10.0));
                break;
            case STRING_LIST:
                addParameter(new OptionParameter(optionName));
                break;
            case PARTICLE:
                addParameter(new OptionParameter(optionName, WbsEnums.toStringList(Particle.class)));
                break;
        }
    }

    @Override
    public void configure(ConfigurationSection config, String directory) {
        switch (type) {
            case INT:
                value = config.getInt(optionName, (Integer) defaultValue);

                if (value == defaultValue) {
                    for (String alias : aliases) {
                        value = config.getInt(alias, (Integer) defaultValue);
                        if (value != defaultValue) break;
                    }
                }
                break;
            case BOOLEAN:
                value = config.getBoolean(optionName, (Boolean) defaultValue);

                if (value == defaultValue) {
                    for (String alias : aliases) {
                        value = config.getBoolean(alias, (Boolean) defaultValue);
                        if (value != defaultValue) break;
                    }
                }
                break;
            case STRING:
                value = config.getString(optionName, (String) defaultValue);

                if (Objects.equals(value, defaultValue)) {
                    for (String alias : aliases) {
                        value = config.getString(alias, (String) defaultValue);
                        if (value != defaultValue) break;
                    }
                }
                break;
            case DOUBLE:
                value = config.getDouble(optionName, (Double) defaultValue);

                if (value == defaultValue) {
                    for (String alias : aliases) {
                        value = config.getDouble(alias, (Double) defaultValue);
                        if (value != defaultValue) break;
                    }
                }
                break;
            case STRING_LIST:
                List<String> stringList = config.getStringList(optionName);

                if (stringList.isEmpty()) {
                    stringList = Arrays.asList((String[]) defaultValue);
                }

                value = stringList.toArray();

                if (Arrays.equals(((String[]) value), (String[]) defaultValue)) {
                    for (String alias : aliases) {
                        stringList = config.getStringList(alias);

                        if (stringList.isEmpty()) {
                            stringList = Arrays.asList((String[]) defaultValue);
                        }

                        value = stringList.toArray();

                        if (!(Arrays.equals(((String[]) value), (String[]) defaultValue))) break;
                    }
                }

                break;
            case PARTICLE:
                // TODO: Add alias support? Might not need to since it's legacy and not used with aliases

                value = config.getString(optionName, (String) defaultValue);
                Objects.requireNonNull(value);

                Particle check = WbsEnums.getEnumFromString(Particle.class, (String) value);
                if (check == null) {
                    MagicSettings.getInstance().logError("Invalid particle: " + value, directory + "/" + optionName);
                    value = defaultValue;
                }
                break;
        }

        if (value == null) {
            value = defaultValue;
        }
    }

    @Override
    public String configure(String key, String value) {
        if (key.trim().equalsIgnoreCase(optionName)) {
            try {
                switch (type) {
                    case INT:
                        this.value = Integer.parseInt(value);
                        break;
                    case BOOLEAN:
                        this.value = Boolean.valueOf(value);
                        break;
                    case STRING:
                        this.value = value;
                        break;
                    case DOUBLE:
                        this.value = Double.parseDouble(value);
                        break;
                    case STRING_LIST:
                        this.value = Arrays.asList(value.split("\\s"));
                        break;
                    case PARTICLE:
                        Particle check = WbsEnums.getEnumFromString(Particle.class, value);
                        if (check == null) {
                            this.value = defaultValue;
                            return "Invalid particle: " + value;
                        } else {
                            this.value = value;
                        }
                        break;
                }
            } catch (NumberFormatException e) {
                return e.getMessage();
            }

            if (this.value == null) {
                this.value = defaultValue;
            }
        }
        return null;
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Integer && type == SpellOptionType.INT) {
            this.value = value;
        }
        if (value instanceof Double && type == SpellOptionType.DOUBLE) {
            this.value = value;
        }
        if (value instanceof Boolean && type == SpellOptionType.BOOLEAN) {
            this.value = value;
        }
        if (value instanceof String && (type == SpellOptionType.STRING || type == SpellOptionType.PARTICLE)) {
            this.value = value;
        }
        if (value instanceof List && type == SpellOptionType.STRING_LIST) {
            this.value = value;
        }
    }

    @Override
    public @NotNull Class<Object> getValueClass() {
        return Object.class;
    }

    @Override
    public @Nullable Object get() {
        return value;
    }

    @Override
    public @NotNull Object getDefault() {
        return defaultValue;
    }

    @Override
    protected String getOptionName(SpellOption annotation) {
        return annotation.optionName();
    }

    @Override
    protected String[] getAliases(SpellOption annotation) {
        return annotation.aliases();
    }

    @Override
    protected boolean getSaveToDefaults(SpellOption annotation) {
        return annotation.saveToDefaults();
    }

    public SpellOptionType getType() {
        return type;
    }

    public double getDouble() {
        Double value = (Double) get();
        if (value != null) {
            return value;
        } else {
            return (double) getDefault();
        }
    }

    public int getInt() {
        Integer value = (Integer) get();
        if (value != null) {
            return value;
        } else {
            return (int) getDefault();
        }
    }

    public boolean getBool() {
        Boolean value = (Boolean) get();
        if (value != null) {
            return value;
        } else {
            return (boolean) getDefault();
        }
    }

    public String getString() {
        String value = (String) get();
        if (value != null) {
            return value;
        } else {
            return (String) getDefault();
        }
    }

    @Override
    public void writeToConfig(ConfigurationSection config) {
        switch (type) {
            case INT:
                config.set(optionName, getInt());
                break;
            case BOOLEAN:
                config.set(optionName, getBool());
                break;
            case STRING:
                config.set(optionName, getString());
                break;
            case DOUBLE:
                config.set(optionName, getDouble());
                break;
            case STRING_LIST:
                config.set(optionName, get());
                break;
            case PARTICLE:
                config.set("particle." + optionName, getString());
                break;
            default:
                WbsMagic.getInstance().getLogger().severe(
                        "An option type was not configured while writing to config. Please report this error."
                );
        }
    }
}
