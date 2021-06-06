package wbs.magic.spells;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.WbsMagic;
import wbs.magic.annotations.DamageSpell;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.annotations.SpellSettings;
import wbs.magic.spellinstances.SpellInstance;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SpellConfig {

	public static SpellConfig fromConfigSection(@NotNull ConfigurationSection config, String directory) {
		return fromConfigSection(config, directory, false);
	}

	@Nullable
	public static SpellConfig fromConfigSection(@NotNull ConfigurationSection config, String directory, boolean makeDefaultConfig) {
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

		setInt(config, spellConfig, "cost", spellAnnotation.cost(), makeDefaultConfig);
		setDouble(config, spellConfig, "cooldown", spellAnnotation.cooldown(), makeDefaultConfig);

		DamageSpell damageSpell = spell.getDamageSpell();
		if (damageSpell != null) {
			setDouble(config, spellConfig, "damage", damageSpell.defaultDamage(), makeDefaultConfig);
		}

		SpellSettings settings = spell.getSettings();
		if (settings != null) {
			if (settings.canBeConcentration()) {
				setBool(config, spellConfig, "concentration", false, makeDefaultConfig);
			}
		}

		for (SpellOption option : spell.getOptions().values()) {
			switch (option.type()) {
				case INT:
					setInt(config, spellConfig,
							option.optionName(), option.defaultInt(),
							makeDefaultConfig, option.aliases());
					break;
				case BOOLEAN:
					setBool(config, spellConfig,
							option.optionName(), option.defaultBool(),
							makeDefaultConfig, option.aliases());
					break;
				case DOUBLE:
					setDouble(config, spellConfig,
							option.optionName(), option.defaultDouble(),
							makeDefaultConfig, option.aliases());
					break;
				case STRING:
					setString(config, spellConfig,
							option.optionName(), option.defaultString(),
							makeDefaultConfig, option.aliases());
					break;
			}
		}

		return spellConfig;
	}

	private static void logIfOptionMissing(ConfigurationSection config, SpellConfig spellConfig, String key) {
		if (config.get(key) == null) {
			WbsMagic.getInstance().settings.logError(
					spellConfig.spellName + " was missing a default option in spells.yml: " + key,
					"spells.yml/" + spellConfig.spellName
			);
		}
	}

	private static void setInt(ConfigurationSection config, SpellConfig spellConfig, String key, int defaultValue, boolean makeDefaultConfig) {
		setDouble(config, spellConfig, key, defaultValue, makeDefaultConfig, null);
	}
	private static void setInt(ConfigurationSection config, SpellConfig spellConfig, String key, int defaultValue, boolean makeDefaultConfig, String[] aliases) {
		if (makeDefaultConfig) logIfOptionMissing(config, spellConfig, key);

		double value = config.getDouble(key, defaultValue);
		if (value == defaultValue && (aliases != null && aliases.length != 0)) {
			for (String alias : aliases) {
				value = config.getDouble(alias, defaultValue);
				if (value != defaultValue) break;
			}
		}
		spellConfig.set(key, value);
	}

	private static void setBool(ConfigurationSection config, SpellConfig spellConfig, String key, boolean defaultValue, boolean makeDefaultConfig) {
		setBool(config, spellConfig, key, defaultValue, makeDefaultConfig, null);
	}
	private static void setBool(ConfigurationSection config, SpellConfig spellConfig, String key, boolean defaultValue, boolean makeDefaultConfig, String[] aliases) {
		if (makeDefaultConfig) logIfOptionMissing(config, spellConfig, key);

		boolean value = config.getBoolean(key, defaultValue);
		if (value == defaultValue && (aliases != null && aliases.length != 0)) {
			for (String alias : aliases) {
				value = config.getBoolean(alias, defaultValue);
				if (value != defaultValue) break;
			}
		}
		spellConfig.set(key, value);
	}

	private static void setDouble(ConfigurationSection config, SpellConfig spellConfig, String key, double defaultValue, boolean makeDefaultConfig) {
		setDouble(config, spellConfig, key, defaultValue, makeDefaultConfig, null);
	}
	private static void setDouble(ConfigurationSection config, SpellConfig spellConfig, String key, double defaultValue, boolean makeDefaultConfig, String[] aliases) {
		if (makeDefaultConfig) logIfOptionMissing(config, spellConfig, key);

		double value = config.getDouble(key, defaultValue);
		if (value == defaultValue && (aliases != null && aliases.length != 0)) {
			for (String alias : aliases) {
				value = config.getDouble(alias, defaultValue);
				if (value != defaultValue) break;
			}
		}
		spellConfig.set(key, value);
	}

	private static void setString(ConfigurationSection config, SpellConfig spellConfig, String key, @NotNull String defaultValue, boolean makeDefaultConfig) {
		setString(config, spellConfig, key, defaultValue, makeDefaultConfig, null);
	}
	private static void setString(ConfigurationSection config, SpellConfig spellConfig, String key, @NotNull String defaultValue, boolean makeDefaultConfig, String[] aliases) {
		if (makeDefaultConfig) logIfOptionMissing(config, spellConfig, key);

		String value = config.getString(key, defaultValue);
		assert value != null;
		if (value.equals(defaultValue) && (aliases != null && aliases.length != 0)) {
			for (String alias : aliases) {
				value = config.getString(alias, defaultValue);
				assert value != null;
				if (!value.equals(defaultValue)) break;
			}
		}
		spellConfig.set(key, value);
	}

	/*===============*/
	/* END OF STATIC */
	/*===============*/

	private final String spellName;
	private final Class<? extends SpellInstance> spellClass;
	private final RegisteredSpell registeredSpell;

	public SpellConfig(RegisteredSpell spell) {
		this.spellName = spell.getName();
		this.spellClass = spell.getSpellClass();
		registeredSpell = spell;

		for (SpellOption option : spell.getOptions().values()) {
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
			}
		}

		Spell spellAnnotation = spell.getSpell();
		set("cost", spellAnnotation.cost());
		set("cooldown", spellAnnotation.cooldown());

		if (spell.getDamageSpell() != null) {
			set("damage", spell.getDamageSpell().defaultDamage());
		}

		SpellSettings settings = spell.getSettings();
		if (settings != null) {
			if (settings.canBeConcentration()) {
				set("concentration", false);
			}
		}
	}

	public SpellInstance buildSpell(String directory) {
		SpellInstance spell;
		
		try {
			Constructor<? extends SpellInstance> constructor = spellClass.getConstructor(SpellConfig.class, String.class);
			spell = constructor.newInstance(this, directory);
			
		} catch (SecurityException | NoSuchMethodException | InstantiationException 
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			WbsMagic.getInstance().settings.logError("Invalid constructor for spell type " + spellName, "SpellConfig generation");
			e.printStackTrace();
			return null;
		}
		
		return spell;
	}

	private final Map<String, Double> doubles = new HashMap<>();
	private final Map<String, Integer> ints = new HashMap<>();
	private final Map<String, String> strings = new HashMap<>();
	private final Map<String, Boolean> bools = new HashMap<>();

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
	//	System.out.println("Set " + key + " to " + value + " in " + spellName);
		return this;
	}
	public SpellConfig set(String key, int value) {
		ints.put(key, value);
	//	System.out.println("Set " + key + " to " + value + " in " + spellName);
		return this;
	}
	public SpellConfig set(String key, String value) {
		strings.put(key, value);
	//	System.out.println("Set " + key + " to " + value + " in " + spellName);
		return this;
	}
	public SpellConfig set(String key, boolean value) {
		bools.put(key, value);
	//	System.out.println("Set " + key + " to " + value + " in " + spellName);
		return this;
	}
	
	
	public double getDouble(String key) {
		return doubles.get(key);
	}
	public int getInt(String key) {
	//	System.out.println("Getting " + key + "...");
	//	System.out.println("ints: " + ints.keySet());
		return ints.get(key);
	}
	public String getString(String key) {
		return strings.get(key);
	}
	public boolean getBoolean(String key) {
		return bools.get(key);
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

	public RegisteredSpell getSpellClass() {
		return registeredSpell;
	}
}
