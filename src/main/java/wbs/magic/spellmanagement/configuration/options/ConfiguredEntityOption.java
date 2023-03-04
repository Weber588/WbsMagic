package wbs.magic.spellmanagement.configuration.options;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.MagicSettings;
import wbs.magic.generators.EntityGenerator;
import wbs.magic.spellmanagement.configuration.options.EntityOptions.EntityOption;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsColours;
import wbs.utils.util.WbsEnums;

import java.util.Objects;
import java.util.function.Function;

public class ConfiguredEntityOption extends ConfiguredSpellOption<EntityGenerator, EntityOption> {
    public static final String BABY_KEY = "baby";
    public static final String CHARGED_KEY = "charged";
    public static final String COLOUR_KEY = "colour";
    public static final String DISPLAY_NAME_KEY = "display-name";
    public static final String DO_DROPS_KEY = "do-drops";
    public static final String FIRE_DURATION_KEY = "fire-duration";
    public static final String FUSE_DURATION_KEY = "fuse-duration";
    public static final String HOLDING_ITEM_KEY = "held-item";
    public static final String LINGERING_KEY = "lingering";
    public static final String MATERIAL_KEY = "material";
    public static final String POTION_AMPLIFIER_KEY = "potion-amplifier";
    public static final String POTION_DURATION_KEY = "potion-duration";
    public static final String POTION_TYPE_KEY = "potion-type";
    public static final String YIELD_KEY = "yield";

    public static final String ENTITY_TYPE_KEY = "type";

    private EntityGenerator value;
    private final EntityGenerator defaultValue;

    private final String namePrefix;

    public ConfiguredEntityOption(EntityOption annotation) {
        super(annotation);

        defaultValue = new EntityGenerator(annotation);
        value = new EntityGenerator(annotation);
        namePrefix = optionName + '-';

        addParameter(new OptionParameter(namePrefix + BABY_KEY, true, false));
        addParameter(new OptionParameter(namePrefix + CHARGED_KEY, true, false));
        addParameter(new OptionParameter(namePrefix + DISPLAY_NAME_KEY));
        addParameter(new OptionParameter(namePrefix + DO_DROPS_KEY, true, false));
        addParameter(new OptionParameter(namePrefix + FIRE_DURATION_KEY,
                annotation.fireDuration(), 1, 10, 30, 60));
        addParameter(new OptionParameter(namePrefix + FUSE_DURATION_KEY,
                annotation.fuseDuration(), 0, 1, 2, 5, 10));
        addParameter(new OptionParameter(namePrefix + LINGERING_KEY, true, false));
        addParameter(new OptionParameter(namePrefix + POTION_AMPLIFIER_KEY,
                annotation.potionAmplifier(), 1, 2, 3, 4, 5));
        addParameter(new OptionParameter(namePrefix + POTION_DURATION_KEY,
                annotation.potionDuration(), 10, 30, 60));
        addParameter(new OptionParameter(namePrefix + YIELD_KEY,
                annotation.yield(), 1, 3, 5, 8));

        Class<? extends Entity> classRestriction = annotation.classRestriction();

        OptionParameter param = new OptionParameter(optionName);
        for (EntityType type : EntityType.values()) {
            Class<? extends Entity> typeClass = type.getEntityClass();
            if (typeClass != null && classRestriction.isAssignableFrom(typeClass)) {
                param.addSuggestion(type.name().toLowerCase());
            }
        }
        addParameter(param);

        param = new OptionParameter(namePrefix + HOLDING_ITEM_KEY);
        for (Material material : Material.values()) {
            // Include AIR as a way to remove the default item for some mobs (skeletons, vex, pillagers etc)
            if (material.isItem() || material == Material.AIR) {
                param.addSuggestion(material.name().toLowerCase());
            }
        }
        addParameter(param);

        param = new OptionParameter(namePrefix + MATERIAL_KEY);
        for (Material material : Material.values()) {
            param.addSuggestion(material.name().toLowerCase());
        }
        addParameter(param);

        param = new OptionParameter(namePrefix + COLOUR_KEY);
        for (DyeColor colour : DyeColor.values()) {
            param.addSuggestion(colour.name().toLowerCase());
        }
        addParameter(param);

        param = new OptionParameter(namePrefix + POTION_TYPE_KEY);
        for (PotionEffectType type : PotionEffectType.values()) {
            param.addSuggestion(type.getName().toLowerCase());
        }
        addParameter(param);
    }

    @Override
    public void configure(ConfigurationSection config, String directory) {
        String usedKey;
        String typeString;
        if (config.isConfigurationSection(optionName)) {
            config = Objects.requireNonNull(config.getConfigurationSection(optionName));
            usedKey = ENTITY_TYPE_KEY;
        } else {
            usedKey = optionName;
        }
        typeString = config.getString(usedKey);

        if (typeString == null) {
            usedKey = namePrefix + ENTITY_TYPE_KEY;
            typeString = config.getString(usedKey);
        }

        if (typeString != null) {
            EntityType type = WbsEnums.getEnumFromString(EntityType.class, typeString);
            if (type != null) {
                value.setType(type);
            } else {
                MagicSettings.getInstance().logError("Invalid entity type: " + typeString, directory + "/" + usedKey);
            }
        }

        Boolean isBaby = tryGetBoolean(config, BABY_KEY, directory);
        if (isBaby != null) {
            value.setBaby(isBaby);
        }

        Boolean isCharged = tryGetBoolean(config, CHARGED_KEY, directory);
        if (isCharged != null) {
            value.setCharged(isCharged);
        }

        usedKey = namePrefix + COLOUR_KEY;
        String colourString = config.getString(usedKey);
        if (colourString == null) {
            usedKey = COLOUR_KEY;
            colourString = config.getString(usedKey);
        }

        if (colourString != null && !colourString.trim().isEmpty()) {
            Color colour = WbsColours.fromHexOrDyeString(colourString);
            if (colour != null) {
                value.setColour(colour);
            } else {
                MagicSettings.getInstance().logError("Invalid colour: " + colourString, directory + "/" + usedKey);
            }
        }

        String displayName = config.getString(namePrefix + DISPLAY_NAME_KEY);
        if (displayName == null) {
            displayName = config.getString(DISPLAY_NAME_KEY);
        }

        if (displayName != null) {
            value.setDisplayName(displayName);
        }

        Boolean doDrops = tryGetBoolean(config, DO_DROPS_KEY, directory);
        if (doDrops != null) {
            value.setDoDrops(doDrops);
        }

        Double fireDuration = tryGetDouble(config, FIRE_DURATION_KEY, directory);
        if (fireDuration != null) {
            value.setFireTicks((int) (fireDuration * 20));
        }

        Double fuseDuration = tryGetDouble(config, FUSE_DURATION_KEY, directory);
        if (fuseDuration != null) {
            value.setFuseDuration((int) (fuseDuration * 20));
        }

        Boolean isLingering = tryGetBoolean(config, LINGERING_KEY, directory);
        if (isLingering != null) {
            value.setLingering(isLingering);
        }

        usedKey = namePrefix + MATERIAL_KEY;
        String materialString = config.getString(usedKey);
        if (materialString == null) {
            usedKey = MATERIAL_KEY;
            materialString = config.getString(usedKey);
        }

        if (materialString != null) {
            Material material = WbsEnums.getEnumFromString(Material.class, materialString);
            if (material != null) {
                value.setMaterial(material);
            } else {
                MagicSettings.getInstance().logError("Invalid material: " + materialString, directory + "/" + usedKey);
            }
        }

        usedKey = namePrefix + HOLDING_ITEM_KEY;
        String heldItemString = config.getString(usedKey);
        if (heldItemString == null) {
            usedKey = HOLDING_ITEM_KEY;
            heldItemString = config.getString(usedKey);
        }

        if (heldItemString != null) {
            Material material = WbsEnums.getEnumFromString(Material.class, heldItemString);
            if (material != null) {
                value.setHoldingItem(material);
            } else {
                MagicSettings.getInstance().logError("Invalid item: " + materialString, directory + "/" + usedKey);
            }
        }

        Integer potionAmplifier = tryGet(config, POTION_AMPLIFIER_KEY, directory, this::configureInt);
        if (potionAmplifier != null) {
            value.setPotionAmplifier(potionAmplifier);
        }

        Double potionDuration = tryGetDouble(config, POTION_DURATION_KEY, directory);
        if (potionDuration != null) {
            value.setPotionDuration((int) (potionDuration * 20));
        }

        usedKey = namePrefix + POTION_TYPE_KEY;
        String potionString = config.getString(usedKey);
        if (potionString == null) {
            usedKey = POTION_TYPE_KEY;
            potionString = config.getString(POTION_TYPE_KEY);
        }

        if (potionString != null) {
            PotionEffectType type = PotionEffectType.getByName(potionString);
            if (type != null) {
                value.setPotionType(type);
            } else {
                MagicSettings.getInstance().logError("Invalid potion type: " + potionString, directory + "/" + usedKey);
            }
        }

        Double yield = tryGetDouble(config, YIELD_KEY, directory);
        if (yield != null) {
            value.setYield((float) (double) yield);
        }
    }

    private Boolean tryGetBoolean(ConfigurationSection config, String key, String directory) {
        return tryGet(config, key, directory, ConfiguredBooleanOption::boolFromString);
    }

    private Double tryGetDouble(ConfigurationSection config, String key, String directory) {
        return tryGet(config, key, directory, this::configureDouble);
    }

    private <T> T tryGet(ConfigurationSection config, String key, String directory, Function<String, T> fromStringFunction) {
        String usedKey = namePrefix + key;

        // Parse from String in case of user error
        String asString = config.getString(usedKey);

        if (asString == null) {
            usedKey = key;
            asString = config.getString(key);
        }

        if (asString == null) {
            return null;
        }

        try {
            return fromStringFunction.apply(asString);
        } catch (InvalidConfigurationException e) {
            MagicSettings.getInstance().logError(e.getMessage(), directory + "/" + usedKey);
            return null;
        }
    }

    @Override
    public String configure(String key, String value) {
        String optionName = this.optionName.toLowerCase();
        key = key.trim().toLowerCase();

        if (key.equals(optionName)) {
            this.value.setTypeFromString(value);
        }

        if (!key.startsWith(optionName + "-")) {
            return null;
        }

        key = key.substring(optionName.length() + 1);

        try {
            switch (key) {
                case DISPLAY_NAME_KEY:
                    this.value.setDisplayName(value);
                    break;
                case FIRE_DURATION_KEY:
                    this.value.setFireTicks((int) (configureDouble(value) * 20));
                    break;
                case YIELD_KEY:
                    this.value.setYield((float) (configureDouble(value) * 20));
                    break;
                case POTION_DURATION_KEY:
                    this.value.setPotionDuration((int) (configureDouble(value) * 20));
                    break;
                case POTION_AMPLIFIER_KEY:
                    this.value.setPotionAmplifier(configureInt(value));
                    break;
                case POTION_TYPE_KEY:
                    this.value.setPotionType(PotionEffectType.getByName(value));
                    break;
                case FUSE_DURATION_KEY:
                    this.value.setFuseDuration((int) (configureDouble(value) * 20));
                    break;
                case LINGERING_KEY:
                    this.value.setLingering(ConfiguredBooleanOption.boolFromString(value));
                    break;
                case MATERIAL_KEY:
                    this.value.setMaterial(WbsEnums.getEnumFromString(Material.class, value));
                    break;
                case CHARGED_KEY:
                    this.value.setCharged(ConfiguredBooleanOption.boolFromString(value));
                    break;
                case BABY_KEY:
                    this.value.setBaby(ConfiguredBooleanOption.boolFromString(value));
                    break;
                case COLOUR_KEY:
                    this.value.setColour(WbsColours.fromHexOrDyeString(value));
                    break;
                case DO_DROPS_KEY:
                    this.value.setDoDrops(ConfiguredBooleanOption.boolFromString(value));
                    break;
                case HOLDING_ITEM_KEY:
                    this.value.setHoldingItem(WbsEnums.getEnumFromString(Material.class, value));
                    break;

            }
        } catch (InvalidConfigurationException e) {
            return e.getMessage();
        }

        return null;
    }

    private double configureDouble(String value) throws InvalidConfigurationException {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationException(e.getMessage());
        }
    }

    private int configureInt(String value) throws InvalidConfigurationException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationException(e.getMessage());
        }
    }

    @Override
    public void setValue(EntityGenerator value) {
        this.value = value;
    }

    @Override
    public @NotNull Class<EntityGenerator> getValueClass() {
        return EntityGenerator.class;
    }

    @Override
    public @Nullable EntityGenerator get() {
        return value;
    }

    @Override
    public @NotNull EntityGenerator getDefault() {
        return defaultValue;
    }

    @Override
    protected String getOptionName(EntityOption annotation) {
        return annotation.optionName();
    }

    @Override
    protected String[] getAliases(EntityOption annotation) {
        return annotation.aliases();
    }

    @Override
    protected boolean getSaveToDefaults(EntityOption annotation) {
        return annotation.saveToDefaults();
    }

    @Override
    public void writeToConfig(ConfigurationSection config) {

    }
}
