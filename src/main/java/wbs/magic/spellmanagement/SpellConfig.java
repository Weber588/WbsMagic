package wbs.magic.spellmanagement;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.generators.EntityGenerator;
import wbs.magic.WbsMagic;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.spellmanagement.configuration.options.*;
import wbs.magic.spells.SpellInstance;
import wbs.magic.targeters.GenericTargeter;
import wbs.utils.exceptions.MissingRequiredKeyException;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

public class SpellConfig {

	public static SpellConfig fromConfigSection(@NotNull ConfigurationSection config, String directory) {
		return fromConfigSection(config, directory, false);
	}

	@Nullable
	public static SpellConfig fromConfigSection(@NotNull ConfigurationSection config, String directory, boolean makeDefaultConfig) {
		String spellString;

		if (makeDefaultConfig) {
			spellString = config.getName();
		} else {
			if (config.get("spell") == null) {
				WbsMagic.getInstance().settings.logError("Missing spell name.", directory + "/spell");
				return null;
			}

			spellString = config.getString("spell");
		}

		RegisteredSpell registration;

		try {
			registration = SpellManager.getSpell(spellString);
		} catch (IllegalArgumentException e) {
			WbsMagic.getInstance().settings.logError("Invalid spell: " + spellString, directory + "/spell");
			return null;
		}

		return new SpellConfig(registration, config, directory);
	}

	/*===============*/
	/* END OF STATIC */
	/*===============*/

	private final RegisteredSpell registeredSpell;

	public SpellConfig(@NotNull RegisteredSpell registration) {
		registeredSpell = registration;

		List<Annotation> allOptions = new LinkedList<>(registeredSpell.getOptions().values());
		List<String> keys = allOptions.stream().map(SpellOptionManager::getOptionName).collect(Collectors.toList());

		SpellSettings settings = registeredSpell.getSettings();
		if (settings != null) {
			if (settings.canBeConcentration()) {
				set("concentration", false, Boolean.class);
			}
		}

		for (Annotation option : allOptions) {
			ConfiguredSpellOption<?, Annotation> configuredOption = SpellOptionManager.getConfiguredOption(option);
			Objects.requireNonNull(configuredOption);

			options.put(SpellOptionManager.getOptionName(option), configuredOption);
		}

		Spell spellAnnotation = registration.getSpell();
		set("cost", spellAnnotation.cost(), Integer.class);
		set("cooldown", spellAnnotation.cooldown(), Double.class);
		set("custom-name", spellAnnotation.name(), String.class);
		saveToDefaults.put("custom-name", false);

		int cost = spellAnnotation.cost();
		double cooldown = spellAnnotation.cooldown();
		String customName = spellAnnotation.name();

        ConfiguredDoubleOption cooldownOption = new ConfiguredDoubleOption(cooldown,
                cooldown,
                "cooldown",
                new String[0],
                true);

        ConfiguredIntegerOption costOption = new ConfiguredIntegerOption(cost,
                cost,
                "cost",
                new String[0],
                true);

        ConfiguredStringOption customNameOption = new ConfiguredStringOption(customName,
                customName,
                "custom-name",
                new String[0],
                true);

        set("cost", costOption);
        set("cooldown", cooldownOption);
        set("custom-name", customNameOption);

		if (registration.getDamageSpell() != null) {
			double defaultDamage = registration.getDamageSpell().defaultDamage();
			ConfiguredDoubleOption damageOption = new ConfiguredDoubleOption(defaultDamage,
					defaultDamage,
					"damage",
					new String[0],
					true);

			set("damage", damageOption);
		}
	}

	public SpellConfig(@NotNull RegisteredSpell spell, @NotNull ConfigurationSection config, String directory) {
		this(spell);

		for (ConfiguredSpellOption<?, ?> option : options.values()) {
			option.configure(config, directory);
		}

		List<Annotation> allOptions = new LinkedList<>(registeredSpell.getOptions().values());
		List<String> keys = allOptions.stream().map(SpellOptionManager::getOptionName).collect(Collectors.toList());

		SpellSettings settings = registeredSpell.getSettings();
		if (settings != null) {
			if (settings.canBeConcentration()) {
				set("concentration", false, Boolean.class);
			}
		}

		for (Annotation option : allOptions) {
			ConfiguredSpellOption<?, Annotation> configuredOption = SpellOptionManager.getConfiguredOption(option);
			Objects.requireNonNull(configuredOption);

			configuredOption.configure(config, directory);

			options.put(SpellOptionManager.getOptionName(option), configuredOption);
		}

		itemCost = new ItemCost(config, directory);
	}

	public SpellInstance buildSpell(String directory) {
		return registeredSpell.buildSpell(this, directory);
	}

	private final Map<String, ConfiguredSpellOption<?, ?>> options = new HashMap<>();

	private final Map<String, Boolean> saveToDefaults = new HashMap<>();

	private ItemCost itemCost;

	/**
	 * Check if a given key exists in any type
	 * @param key The key to check
	 * @return True if the key exists
	 */
	public boolean contains(String key) {
		return options.containsKey(key);
	}

	public Set<String> getAllKeys() {
		return options.keySet();
	}

	public Set<String> getOptionKeys() {
		Set<String> optionKeys = new HashSet<>();

		for (ConfiguredSpellOption<?, ?> option : options.values()) {
			option.getOptionParameters().forEach(optionParameter -> optionKeys.add(optionParameter.getOptionName()));
		}



		return optionKeys;
	}

	public SpellConfig set(String key, ConfiguredSpellOption<?, ?> option) {
		options.put(key, option);
		return this;
	}

	/**
	 * Set a value based on the {@link ConfiguredSpellOption#configure(String, String)} method on
	 * all options.
	 * @param key The key being used
	 * @param value The value to configure the option
	 * @return The first error, if any were encountered.
	 */
	public String set(String key, String value) {
		for (ConfiguredSpellOption<?, ?> option : options.values()) {
			String error = option.configure(key, value);

			if (error != null) {
				return error;
			}
		}

		return null;
	}

	/**
	 * Set a value based on the {@link ConfiguredSpellOption#configure(String, String)} method on
	 * only the exact option identified by the provided key
	 * @param key The key used to identify the specific option to attempt to configure.
	 * @param value The value to configure the option
	 * @return The first error, if any were encountered.
	 */
	public String setSpecific(String key, String value) {
		ConfiguredSpellOption<?, ?> option = options.get(key);

		if (option != null) {
			return option.configure(key, value);
		}

		return null;
	}

	public <T> SpellConfig set(String key, T value, Class<T> clazz) {
		ConfiguredSpellOption<?, ?> option = options.get(key);

		if (option != null) {
			if (option.getValueClass() == clazz) {
				@SuppressWarnings("unchecked")
				ConfiguredSpellOption<T, ?> typedOption = (ConfiguredSpellOption<T, ?>) option;

				typedOption.setValue(value);
			}
		}
		return this;
	}

	public Object get(String key) {
		return options.get(key).get();
	}

	public <T> T get(String key, Class<? extends ConfiguredSpellOption<T, ?>> clazz) {
		ConfiguredSpellOption<?, ?> option = options.get(key);

		return clazz.cast(option).get();
	}

	public double getDouble(String key) {
		ConfiguredSpellOption<?, ?> option = options.get(key);

		if (option instanceof ConfiguredDoubleOption) {
			ConfiguredDoubleOption doubleOption = (ConfiguredDoubleOption) option;
			Double value = doubleOption.get();
			if (value != null) {
				return value;
			} else {
				return doubleOption.getDefault();
			}
		}

		if (option instanceof ConfiguredLegacySpellOption) {
			ConfiguredLegacySpellOption legacyOption = (ConfiguredLegacySpellOption) option;

			if (legacyOption.getType() == SpellOptionType.DOUBLE) {
				return legacyOption.getDouble();
			}
		}

		throw new MissingRequiredKeyException(key);
	}

	/**
	 * Gets the number of ticks for a given duration, based on a double given in seconds by
	 * configuration.
	 * @param key The key for the double, to parse as a duration int.
	 * @return The number of ticks represented by the duration
	 */
	public int getDurationFromDouble(String key) {
		return (int) (getDouble(key) * 20);
	}

	public double getDefaultDouble(String key) {
		ConfiguredSpellOption<?, ?> option = options.get(key);

		if (option instanceof ConfiguredDoubleOption) {
			ConfiguredDoubleOption doubleOption = (ConfiguredDoubleOption) option;
			return doubleOption.getDefault();
		}

		if (option instanceof ConfiguredLegacySpellOption) {
			ConfiguredLegacySpellOption legacyOption = (ConfiguredLegacySpellOption) option;

			if (legacyOption.getType() == SpellOptionType.DOUBLE) {
				return (double) legacyOption.getDefault();
			}
		}

		throw new MissingRequiredKeyException(key);
	}

	@NotNull
	public List<Double> getDoubleList(String key) {
		ConfiguredSpellOption<?, ?> option = options.get(key);

		if (option instanceof ConfiguredDoubleOption) {
			ConfiguredDoubleOption doubleOption = (ConfiguredDoubleOption) option;
			return doubleOption.getList();
		}

		throw new MissingRequiredKeyException(key);
	}

	public int getInt(String key) {
		ConfiguredSpellOption<?, ?> option = options.get(key);

		if (option instanceof ConfiguredIntegerOption) {
			ConfiguredIntegerOption intOption = (ConfiguredIntegerOption) option;
			Integer value = intOption.get();
			if (value != null) {
				return value;
			} else {
				return intOption.getDefault();
			}
		}

		if (option instanceof ConfiguredLegacySpellOption) {
			ConfiguredLegacySpellOption legacyOption = (ConfiguredLegacySpellOption) option;

			if (legacyOption.getType() == SpellOptionType.INT) {
				return legacyOption.getInt();
			}
		}

		throw new MissingRequiredKeyException(key);
	}

	@NotNull
	public List<Integer> getIntList(String key) {
		ConfiguredSpellOption<?, ?> option = options.get(key);

		if (option instanceof ConfiguredIntegerOption) {
			ConfiguredIntegerOption intOption = (ConfiguredIntegerOption) option;
			return intOption.getList();
		}

		throw new MissingRequiredKeyException(key);
	}

	public String getString(String key) {
		ConfiguredSpellOption<?, ?> option = options.get(key);

		if (option instanceof ConfiguredStringOption) {
			ConfiguredStringOption stringOption = (ConfiguredStringOption) option;
			String value = stringOption.get();
			if (value != null) {
				return value;
			} else {
				return stringOption.getDefault();
			}
		}

		if (option instanceof ConfiguredLegacySpellOption) {
			ConfiguredLegacySpellOption legacyOption = (ConfiguredLegacySpellOption) option;

			if (legacyOption.getType() == SpellOptionType.STRING || legacyOption.getType() == SpellOptionType.PARTICLE) {
				return legacyOption.getString();
			}
		}

		throw new MissingRequiredKeyException(key);
	}

	public String getDefaultString(String key) {
		ConfiguredSpellOption<?, ?> option = options.get(key);

		if (option instanceof ConfiguredStringOption) {
			ConfiguredStringOption stringOption = (ConfiguredStringOption) option;
			return stringOption.getDefault();
		}

		if (option instanceof ConfiguredLegacySpellOption) {
			ConfiguredLegacySpellOption legacyOption = (ConfiguredLegacySpellOption) option;

			if (legacyOption.getType() == SpellOptionType.STRING || legacyOption.getType() == SpellOptionType.PARTICLE) {
				return (String) legacyOption.getDefault();
			}
		}

		throw new MissingRequiredKeyException(key);
	}

	@NotNull
	public List<String> getStringList(String key) {
		ConfiguredSpellOption<?, ?> option = options.get(key);

		if (option instanceof ConfiguredStringOption) {
			ConfiguredStringOption stringOption = (ConfiguredStringOption) option;
			return stringOption.getList();
		}

		throw new MissingRequiredKeyException(key);
	}

	public boolean getBoolean(String key) {
		ConfiguredSpellOption<?, ?> option = options.get(key);

		if (option instanceof ConfiguredBooleanOption) {
			ConfiguredBooleanOption booleanOption = (ConfiguredBooleanOption) option;
			Boolean value = booleanOption.get();
			if (value != null) {
				return value;
			} else {
				return booleanOption.getDefault();
			}
		}

		if (option instanceof ConfiguredLegacySpellOption) {
			ConfiguredLegacySpellOption legacyOption = (ConfiguredLegacySpellOption) option;

			if (legacyOption.getType() == SpellOptionType.BOOLEAN) {
				return legacyOption.getBool();
			}
		}

		throw new MissingRequiredKeyException(key);
	}

	@NotNull
	public List<Boolean> getBooleanList(String key) {
		ConfiguredSpellOption<?, ?> option = options.get(key);

		if (option instanceof ConfiguredBooleanOption) {
			ConfiguredBooleanOption boolOption = (ConfiguredBooleanOption) option;
			return boolOption.getList();
		}

		throw new MissingRequiredKeyException(key);
	}

	public GenericTargeter getTargeter(String key) {
		ConfiguredSpellOption<?, ?> option = options.get(key);

		if (option instanceof ConfiguredTargeterOption) {
			ConfiguredTargeterOption targeterOption = (ConfiguredTargeterOption) option;
			GenericTargeter value = targeterOption.get();
			if (value != null) {
				return value;
			} else {
				return targeterOption.getDefault();
			}
		}

		throw new MissingRequiredKeyException(key);
	}

	public <T extends Enum<T>> T getEnum(String key, Class<T> enumClass) {
		ConfiguredSpellOption<?, ?> option = options.get(key);

		if (option instanceof ConfiguredEnumOption) {
			ConfiguredEnumOption enumOption = (ConfiguredEnumOption) option;
			Enum<?> value = enumOption.get();
			if (enumClass.isInstance(value)) {
				return enumClass.cast(value);
			}
		}

		throw new MissingRequiredKeyException(key);
	}

	public <T extends Enum<T>> T getDefaultEnum(String key, Class<T> enumClass) {
		ConfiguredSpellOption<?, ?> option = options.get(key);

		if (option instanceof ConfiguredEnumOption) {
			ConfiguredEnumOption enumOption = (ConfiguredEnumOption) option;
			Enum<?> value = enumOption.getDefault();
			if (enumClass.isInstance(value)) {
				return enumClass.cast(value);
			}
		}

		throw new MissingRequiredKeyException(key);
	}

	@NotNull
	public <T extends Enum<T>> List<T> getEnumList(String key, Class<T> enumClass) {
		ConfiguredSpellOption<?, ?> option = options.get(key);

		if (option instanceof ConfiguredEnumOption) {
			ConfiguredEnumOption enumOption = (ConfiguredEnumOption) option;
			@SuppressWarnings("rawtypes")
			List<Enum> value = enumOption.getList();
			List<T> returnList = new LinkedList<>();

			//noinspection rawtypes
			for (Enum check : value) {
				if (enumClass.isInstance(check)) {
					returnList.add(enumClass.cast(check));
				}
			}

			return returnList;
		}

		throw new MissingRequiredKeyException(key);
	}

	public double getDouble(String key, double defaultDouble) {
		try {
			return getDouble(key);
		} catch (MissingRequiredKeyException e) {
			return defaultDouble;
		}
	}

	public int getInt(String key, int defaultInt) {
		try {
			return getInt(key);
		} catch (MissingRequiredKeyException e) {
			return defaultInt;
		}
	}

	public String getString(String key, String defaultString) {
		try {
			return getString(key);
		} catch (MissingRequiredKeyException e) {
			return defaultString;
		}
	}

	public boolean getBoolean(String key, boolean defaultBool) {
		try {
			return getBoolean(key);
		} catch (MissingRequiredKeyException e) {
			return defaultBool;
		}
	}


	public RegisteredSpell getRegistration() {
		return registeredSpell;
	}

	public ConfigurationSection writeToConfig(ConfigurationSection config) {

		List<String> optionKeys = new LinkedList<>(getAllKeys());
		optionKeys.sort(String::compareTo);

		for (String optionName : optionKeys) {
			ConfiguredSpellOption<?, ?> option = options.get(optionName);

			option.writeToConfig(config);
		}

		return config;
	}

	public ItemCost getItemCost() {
		return itemCost;
	}

	public List<OptionParameter> getOptions(String key) {
		List<OptionParameter> parameters = new LinkedList<>();

		for (ConfiguredSpellOption<?, ?> option : options.values()) {
			for (OptionParameter param : option.getOptionParameters()) {
				if (param.getOptionName().equalsIgnoreCase(key)) {
					parameters.add(param);
				}
			}
		}

		return parameters;
	}
}
