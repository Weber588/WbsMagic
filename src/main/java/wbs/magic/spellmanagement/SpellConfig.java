package wbs.magic.spellmanagement;

import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.generators.EntityGenerator;
import wbs.magic.WbsMagic;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.spells.SpellInstance;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class SpellConfig {

	public static SpellConfig fromConfigSection(@NotNull ConfigurationSection config, String directory) {
		return fromConfigSection(config, directory, false, false);
	}

	// TODO: Make this a .configure(ConfigurationSection config) method. Not sure why I made this static
	@Nullable
	public static SpellConfig fromConfigSection(@NotNull ConfigurationSection config, String directory, boolean makeDefaultConfig, boolean logMissing) {
		SpellConfig spellConfig;

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

		RegisteredSpell spell;

		try {
			spell = SpellManager.getSpell(spellString);
		} catch(IllegalArgumentException e) {
			WbsMagic.getInstance().settings.logError("Invalid spell: " + spellString, directory + "/spell");
			return null;
		}

		spellConfig = new SpellConfig(spell);

		Spell spellAnnotation = spell.getSpell();

		setInt(config, spellConfig, "cost", spellAnnotation.cost(), logMissing);
		setDouble(config, spellConfig, "cooldown", spellAnnotation.cooldown(), logMissing);
		setString(config, spellConfig, "custom-name", spell.getName(), false);

		DamageSpell damageSpell = spell.getDamageSpell();
		if (damageSpell != null) {
			setDouble(config, spellConfig, "damage", damageSpell.defaultDamage(), logMissing);
		}

		List<SpellOption> allOptions = new LinkedList<>(spell.getOptions().values());
		List<String> keys = allOptions.stream().map(SpellOption::optionName).collect(Collectors.toList());

		SpellSettings settings = spell.getSettings();
		if (settings != null) {
			if (settings.canBeConcentration()) {
				setBool(config, spellConfig, "concentration", false, logMissing);
			}

			if (settings.isEntitySpell()) {
				SpellOptions options = EntityGenerator.class.getAnnotation(SpellOptions.class);
				if (options != null) {
					Arrays.stream(options.value()).forEach(option -> {
						if (!keys.contains(option.optionName())) {
							allOptions.add(option);
						}
					});
				} else { // Try for single SpellOption
					SpellOption option = EntityGenerator.class.getAnnotation(SpellOption.class);
					if (option != null) {
						if (!keys.contains(option.optionName())) {
							allOptions.add(option);
						}
					}
				}
			}
		}

		for (SpellOption option : allOptions) {
			boolean localLogMissing = logMissing && option.saveToDefaults();
			switch (option.type()) {
				case INT:
					setInt(config, spellConfig,
							option.optionName(), option.defaultInt(),
							localLogMissing, option.aliases());
					break;
				case BOOLEAN:
					setBool(config, spellConfig,
							option.optionName(), option.defaultBool(),
							localLogMissing, option.aliases());
					break;
				case DOUBLE:
					setDouble(config, spellConfig,
							option.optionName(), option.defaultDouble(),
							localLogMissing, option.aliases());
					break;
				case STRING:
					setString(config, spellConfig,
							option.optionName(), option.defaultString(),
							localLogMissing, option.aliases());
					spellConfig.setEnum(option);
					break;
				case STRING_LIST:
					setStringList(config, spellConfig,
							option.optionName(), option.defaultStrings(),
							localLogMissing, option.aliases());
					break;
				case PARTICLE:
					setParticle(config, spellConfig,
							option.optionName(), option.defaultParticle(),
							localLogMissing, option.aliases());
					break;
				default:
					WbsMagic.getInstance().getLogger().severe(
							"An option type was not configured while reading from a config. Please report this error."
					);
			}
		}

		spellConfig.itemCost = new ItemCost(config, directory);

		return spellConfig;
	}

	private void setEnum(SpellOption option) {
		if (option.enumType() != Enum.class) {
			enumTypes.put(option.optionName(), option.enumType());
		}
	}

	private static void logIfOptionMissing(ConfigurationSection config, SpellConfig spellConfig, String key) {
		if (config.get(key) == null) {
			WbsMagic.getInstance().settings.logError(
					spellConfig.spellName + " was missing a default option in spells.yml: " + key,
					"spells.yml/" + spellConfig.spellName
			);
		}
	}

	private static void setInt(ConfigurationSection config, SpellConfig spellConfig, String key, int defaultValue, boolean logMissing) {
		setInt(config, spellConfig, key, defaultValue, logMissing, null);
	}
	private static void setInt(ConfigurationSection config, SpellConfig spellConfig, String key, int defaultValue, boolean logMissing, String[] aliases) {

		if (logMissing) logIfOptionMissing(config, spellConfig, key);

		int value = config.getInt(key, defaultValue);
		if (value == defaultValue && (aliases != null && aliases.length != 0)) {
			for (String alias : aliases) {
				value = config.getInt(alias, defaultValue);
				if (value != defaultValue) break;
			}
		}
		spellConfig.set(key, value);
	}

	private static void setBool(ConfigurationSection config, SpellConfig spellConfig, String key, boolean defaultValue, boolean logMissing) {
		setBool(config, spellConfig, key, defaultValue, logMissing, null);
	}
	private static void setBool(ConfigurationSection config, SpellConfig spellConfig, String key, boolean defaultValue, boolean logMissing, String[] aliases) {
		if (logMissing) logIfOptionMissing(config, spellConfig, key);

		boolean value = config.getBoolean(key, defaultValue);
		if (value == defaultValue && (aliases != null && aliases.length != 0)) {
			for (String alias : aliases) {
				value = config.getBoolean(alias, defaultValue);
				if (value != defaultValue) break;
			}
		}
		spellConfig.set(key, value);
	}

	private static void setDouble(ConfigurationSection config, SpellConfig spellConfig, String key, double defaultValue, boolean logMissing) {
		setDouble(config, spellConfig, key, defaultValue, logMissing, null);
	}
	private static void setDouble(ConfigurationSection config, SpellConfig spellConfig, String key, double defaultValue, boolean logMissing, String[] aliases) {
		if (logMissing) logIfOptionMissing(config, spellConfig, key);

		double value = config.getDouble(key, defaultValue);
		if (value == defaultValue && (aliases != null && aliases.length != 0)) {
			for (String alias : aliases) {
				value = config.getDouble(alias, defaultValue);
				if (value != defaultValue) break;
			}
		}
		spellConfig.set(key, value);
	}

	private static void setString(ConfigurationSection config, SpellConfig spellConfig, String key, @NotNull String defaultValue, boolean logMissing) {
		setString(config, spellConfig, key, defaultValue, logMissing, null);
	}
	private static void setString(ConfigurationSection config, SpellConfig spellConfig, String key, @NotNull String defaultValue, boolean logMissing, String[] aliases) {
		if (logMissing) logIfOptionMissing(config, spellConfig, key);

		String value = config.getString(key, defaultValue);
		assert value != null;
		if (value.equalsIgnoreCase(defaultValue) && (aliases != null && aliases.length != 0)) {
			for (String alias : aliases) {
				value = config.getString(alias, defaultValue);
				assert value != null;
				if (!value.equalsIgnoreCase(defaultValue)) break;
			}
		}
		spellConfig.set(key, value);
	}

	private static void setStringList(ConfigurationSection config, SpellConfig spellConfig, String key, @NotNull String[] defaultValue, boolean logMissing) {
		setStringList(config, spellConfig, key, defaultValue, logMissing, null);
	}
	private static void setStringList(ConfigurationSection config, SpellConfig spellConfig, String key, @NotNull String[] defaultValue, boolean logMissing, String[] aliases) {
		if (logMissing) logIfOptionMissing(config, spellConfig, key);

		List<String> value = config.getStringList(key);
		if (value.isEmpty() && (aliases != null && aliases.length != 0)) {
			for (String alias : aliases) {
				value = config.getStringList(alias);

				if (!value.isEmpty()) break;
			}
		}

		if (value.isEmpty()) {
			value.addAll(Arrays.asList(defaultValue));
		}
		spellConfig.set(key, value);
	}

	private static void setParticle(ConfigurationSection config, SpellConfig spellConfig, String key, @NotNull Particle defaultValue, boolean logMissing) {
		setParticle(config, spellConfig, key, defaultValue, logMissing, null);
	}
	private static void setParticle(ConfigurationSection config, SpellConfig spellConfig, String key, @NotNull Particle defaultValue, boolean logMissing, String[] aliases) {
		if (logMissing) logIfOptionMissing(config, spellConfig, key);

		String defaultParticleName = defaultValue.name();

		// Read particles from a subsection
		String stringValue = config.getString("particle." + key, defaultParticleName);
		assert stringValue != null;
		if (stringValue.equalsIgnoreCase(defaultParticleName) && (aliases != null && aliases.length != 0)) {
			for (String alias : aliases) {
				stringValue = config.getString("particle." + alias, defaultParticleName);
				assert stringValue != null;
				if (!stringValue.equalsIgnoreCase(defaultParticleName)) break;
			}
		}

		Particle particle = WbsEnums.getEnumFromString(Particle.class, stringValue);
		if (particle == null) {
			spellConfig.set(key, defaultValue);
		} else {
			spellConfig.set(key, particle);
		}
	}

	/*===============*/
	/* END OF STATIC */
	/*===============*/

	private final String spellName;
	private final Class<? extends SpellInstance> spellClass;
	private final RegisteredSpell registeredSpell;

	public SpellConfig(@NotNull RegisteredSpell spell) {
		this.spellName = spell.getName();
		this.spellClass = spell.getSpellClass();
		registeredSpell = spell;

		Spell spellAnnotation = spell.getSpell();
		set("cost", spellAnnotation.cost());
		set("cooldown", spellAnnotation.cooldown());
		set("custom-name", spellAnnotation.name());
		saveToDefaults.put("custom-name", false);

		if (spell.getDamageSpell() != null) {
			set("damage", spell.getDamageSpell().defaultDamage());
		}

		List<SpellOption> allOptions = new LinkedList<>(spell.getOptions().values());
		List<String> keys = allOptions.stream().map(SpellOption::optionName).collect(Collectors.toList());

		SpellSettings settings = spell.getSettings();
		if (settings != null) {
			if (settings.canBeConcentration()) {
				set("concentration", false);
			}

			if (settings.isEntitySpell()) {
				SpellOptions options = EntityGenerator.class.getAnnotation(SpellOptions.class);
				if (options != null) {
					Arrays.stream(options.value()).forEach(option -> {
						if (!keys.contains(option.optionName())) {
							allOptions.add(option);
						}
					});
				} else { // Try for single SpellOption
					SpellOption option = EntityGenerator.class.getAnnotation(SpellOption.class);
					if (option != null) {
						if (!keys.contains(option.optionName())) {
							allOptions.add(option);
						}
					}
				}
			}
		}

		for (SpellOption option : allOptions) {
			saveToDefaults.put(option.optionName(), option.saveToDefaults());

			switch (option.type()) {
				case INT:
					set(option.optionName(), option.defaultInt());
					break;
				case BOOLEAN:
					set(option.optionName(), option.defaultBool());
					break;
				case DOUBLE:
					set(option.optionName(), option.defaultDouble());
					break;
				case STRING:
					set(option.optionName(), option.defaultString());
					break;
				case STRING_LIST:
					set(option.optionName(), Arrays.asList(option.defaultStrings()));
					break;
				case PARTICLE:
					set(option.optionName(), option.defaultParticle());
					break;
				default:
					WbsMagic.getInstance().getLogger().severe(
							"An option type was not configured while building a spell config. Please report this error."
					);
			}
		}
	}

	public SpellInstance buildSpell(String directory) {
		SpellInstance spell;
		
		try {
			Constructor<? extends SpellInstance> constructor = spellClass.getConstructor(SpellConfig.class, String.class);
			spell = constructor.newInstance(this, directory);
			
		} catch (SecurityException | NoSuchMethodException | InstantiationException 
				| IllegalAccessException | IllegalArgumentException e) {
			WbsMagic.getInstance().settings.logError("Invalid constructor for spell type " + spellName, directory);
			e.printStackTrace();
			return null;
		} catch (InvocationTargetException e){
			Throwable cause = e.getCause();
			if (cause instanceof InvalidConfigurationException) {
				WbsMagic.getInstance().settings.logError(cause.getMessage(), directory);
			} else {
				WbsMagic.getInstance().settings.logError("An error occurred while constructing " + spellName, directory);
				e.printStackTrace();
			}
			return null;
		}
		
		return spell;
	}

	private final Map<String, SpellOptionType> keyPairs = new HashMap<>();

	private final Map<String, Double> doubles = new HashMap<>();
	private final Map<String, Integer> ints = new HashMap<>();
	private final Map<String, String> strings = new HashMap<>();
	private final Map<String, Boolean> bools = new HashMap<>();
	private final Map<String, List<String>> stringLists = new HashMap<>();
	private final Map<String, Particle> particles = new HashMap<>();

	private final Map<String, Class<? extends Enum>> enumTypes = new HashMap<>();
	private final Map<String, Boolean> saveToDefaults = new HashMap<>();

	private ItemCost itemCost;

	/**
	 * Check if a given key exists in any type
	 * @param key The key to check
	 * @return True if the key exists
	 */
	public boolean contains(String key) {
		return	doubles.containsKey(key)
				|| ints.containsKey(key)
				|| strings.containsKey(key)
				|| bools.containsKey(key);
	}

	public Set<String> getAllKeys() {
		Set<String> allKeys = new HashSet<>();
		allKeys.addAll(getDoubleKeys());
		allKeys.addAll(getIntKeys());
		allKeys.addAll(getStringKeys());
		allKeys.addAll(getBoolKeys());
		return allKeys;
	}

	public Set<String> getDoubleKeys() {
		return doubles.keySet();
	}
	public Set<String> getIntKeys() {
		return ints.keySet();
	}

	public Set<String> getStringKeys() {
		return strings.keySet();
	}
	public Set<String> getBoolKeys() {
		return bools.keySet();
	}

	public SpellConfig set(String key, double value) {
		doubles.put(key, value);
		keyPairs.put(key, SpellOptionType.DOUBLE);
	//	System.out.println("Set " + key + " to " + value + " in " + spellName);
		return this;
	}
	public SpellConfig set(String key, int value) {
		ints.put(key, value);
		keyPairs.put(key, SpellOptionType.INT);
	//	System.out.println("Set " + key + " to " + value + " in " + spellName);
		return this;
	}
	public SpellConfig set(String key, String value) {
		strings.put(key, value);
		keyPairs.put(key, SpellOptionType.STRING);
	//	System.out.println("Set " + key + " to " + value + " in " + spellName);
		return this;
	}
	public SpellConfig set(String key, boolean value) {
		bools.put(key, value);
		keyPairs.put(key, SpellOptionType.BOOLEAN);
		//	System.out.println("Set " + key + " to " + value + " in " + spellName);
		return this;
	}
	public SpellConfig set(String key, List<String> value) {
		stringLists.put(key, value);
		keyPairs.put(key, SpellOptionType.STRING_LIST);
		//	System.out.println("Set " + key + " to " + value + " in " + spellName);
		return this;
	}
	public SpellConfig set(String key, Particle value) {
		particles.put(key, value);
		keyPairs.put(key, SpellOptionType.PARTICLE);
		//	System.out.println("Set " + key + " to " + value + " in " + spellName);
		return this;
	}
	
	
	public double getDouble(String key) {
		return doubles.get(key);
	}
	public int getInt(String key) {
		return ints.get(key);
	}
	public String getString(String key) {
		return strings.get(key);
	}
	public boolean getBoolean(String key) {
		return bools.get(key);
	}
	public List<String> getStringList(String key) {
		return stringLists.get(key);
	}
	public Particle getParticle(String key) {
		return particles.get(key);
	}


	public double getDouble(String key, double defaultDouble) {
		Double value = doubles.get(key);
		return value == null ? defaultDouble : value;
	}
	public int getInt(String key, int defaultInt) {
		Integer value = ints.get(key);
		return value == null ? defaultInt : value;
	}
	public String getString(String key, String defaultString) {
		String value = strings.get(key);
		return value == null ? defaultString : value;
	}
	public boolean getBoolean(String key, boolean defaultBool) {
		Boolean value = bools.get(key);
		return value == null ? defaultBool : value;
	}
	public List<String> getStringList(String key, List<String> defaultStrings) {
		List<String> value = stringLists.get(key);
		return value == null || value.isEmpty() ? defaultStrings : value;
	}
	public Class<? extends Enum> getEnumType(String key) {
		return enumTypes.get(key);
	}

	@NotNull
	protected <T extends Enum<T>> T getEnum(Class<T> clazz, String option, @NotNull T defaultVal) {
		String checkString = getString(option);
		T check = WbsEnums.getEnumFromString(clazz, checkString);

		if (check == null) {
			check = defaultVal;
		}

		return check;
	}

	public RegisteredSpell getSpellClass() {
		return registeredSpell;
	}

	public ConfigurationSection writeToConfig(ConfigurationSection config) {
		List<String> optionKeys = new LinkedList<>(getAllKeys());
		optionKeys.sort(String::compareTo);

		for (String optionName : optionKeys) {
			if (!saveToDefaults.getOrDefault(optionName, true)) continue;

			switch (keyPairs.get(optionName)) {
				case INT:
					config.set(optionName, getInt(optionName));
					break;
				case BOOLEAN:
					config.set(optionName, getBoolean(optionName));
					break;
				case STRING:
					config.set(optionName, getString(optionName));
					break;
				case DOUBLE:
					config.set(optionName, getDouble(optionName));
					break;
				case STRING_LIST:
					config.set(optionName, getStringList(optionName));
					break;
				case PARTICLE:
					config.set("particle." + optionName, getParticle(optionName));
					break;
				default:
					WbsMagic.getInstance().getLogger().severe(
							"An option type was not configured while writing to config. Please report this error."
					);
			}
		}

		return config;
	}

	public ItemCost getItemCost() {
		return itemCost;
	}
}
