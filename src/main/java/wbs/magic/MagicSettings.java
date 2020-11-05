package wbs.magic;

import java.io.*;
import java.util.*;

import com.google.common.base.Charsets;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import wbs.magic.enums.WandControl;
import wbs.magic.passives.PassiveEffect;
import wbs.magic.passives.PassiveEffectType;
import wbs.magic.spellinstances.SpellInstance;
import wbs.magic.spells.*;
import wbs.magic.wrappers.MagicWand;

import wbs.utils.util.plugin.WbsSettings;

public class MagicSettings extends WbsSettings {
	
	private static MagicSettings instance;
	public static MagicSettings getInstance() {
		return instance;
	}

	protected MagicSettings(WbsMagic plugin) {
		super(plugin);
	}


	/*==================*/
	//		SETUP		//
	/*==================*/
	
	public void reload() {
		instance = this;
		genConfig("config.yml");
	//	genConfig("spells.yml");
		genConfig("wands/Example.yml");

		loadConfigs();

		loadNativeSpells();

		loadSpellDefaults();
		
		loadMain();
		loadWands();
	}

	private void loadNativeSpells() {
		SpellManager.clear();
		SpellLoader loader = new NativeSpellLoader();
		int loaded = SpellManager.registerClasses(loader);
		WbsMagic.getInstance().getLogger().info(
				loaded + " out of " + loader.getSpellCount() + " native spells were loaded.");
	}
	
	private Map<String, FileConfiguration> configs;
	
	private void loadConfigs() {
		configs = new HashMap<>();
		
		File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) { 
        	plugin.saveResource("config.yml", false);
        }
        configs.put("main", loadConfigSafely(configFile));

        configs.put("spells", loadConfigSafely(configFile));
	}


	private void loadSpellDefaults() {
		File configFile = new File(plugin.getDataFolder(), "spells.yml");
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
				logger.info("Loaded spells.yml for the first time.");
			} catch (IOException e) {
				logger.severe("An unknown error occurred when creating spells.yml");
				e.printStackTrace();
			}
		}

		YamlConfiguration config = loadConfigSafely(configFile);

		List<RegisteredSpell> spells = new ArrayList<>(SpellManager.getSpells().values());

		// Loop through spells.yml and load default SpellConfig from there.
		// Then remove it from configuredSpells, so the ones remaining need to be generated.
		for (String key : config.getKeys(false)) {
			RegisteredSpell checkSpell;
			try {
				checkSpell = SpellManager.getSpell(key);
			} catch (IllegalArgumentException e) {
				logError("spells.yml contained an unrecognized spell: " + key, "spells.yml/" + key);
				continue;
			}
		//	logger.info("(Existing) " + key + " config keys = " + config.getConfigurationSection(key).getKeys(false));
		//	checkSpell.buildDefaultConfig(config.getConfigurationSection(key), "spells.yml/" + key);

			spells.remove(checkSpell);
		}

		spells.sort(Comparator.comparing(RegisteredSpell::getName));

		for (RegisteredSpell spell : spells) {
			ConfigurationSection spellSection = config.createSection(spell.getName());

			spell.toConfigSection(spellSection);

			config.set(spell.getName(), spellSection);
		}

		try {
			config.save(configFile);
		} catch (IOException e) {
			logger.severe("An unknown error occurred when saving spells.yml");
			e.printStackTrace();
		}
		int addedSpells = spells.size();
		if (addedSpells != 0) {
			logger.info("Added " + addedSpells + " new spells to spells.yml");
		}

		// Load default SpellConfigs
		List<RegisteredSpell> allSpells = new ArrayList<>(SpellManager.getSpells().values());

		for (RegisteredSpell spell : allSpells) {
			spell.buildDefaultConfig(
					config.getConfigurationSection(spell.getName()),
					"spells.yml/" + spell.getName()
			);

		//	logger.info(spell.getName() + " config keys = " + config.getKeys(false));
		}
	}

	/*==================*/
	//		MAIN		//
	/*==================*/

	private static final String DEFAULT_PREFIX = "&0[&5mag&0]&r";
	private static final ChatColor DEFAULT_MESSAGE_COLOUR = ChatColor.YELLOW;
	private static final ChatColor DEFAULT_HIGHLIGHT_COLOUR = ChatColor.AQUA;
	private static final ChatColor DEFAULT_ERROR_COLOUR = ChatColor.RED;

	private void loadMain() {
		FileConfiguration main = configs.get("main");
		
        String newPrefix = main.getString("message-prefix");
        if (newPrefix == null) newPrefix = DEFAULT_PREFIX;
        String mainColourString = main.getString("message-colour");
        ChatColor newColour = mainColourString != null ? ChatColor.getByChar(mainColourString) : DEFAULT_MESSAGE_COLOUR;
		String highlightString = main.getString("highlight-colour");
        ChatColor newHighlight = highlightString != null ? ChatColor.getByChar(highlightString) : DEFAULT_HIGHLIGHT_COLOUR;
		String errorColourString = main.getString("error-colour");
        ChatColor newErrorColour = errorColourString != null ? ChatColor.getByChar(errorColourString) : DEFAULT_ERROR_COLOUR;
        plugin.setDisplays(newPrefix, newColour, newHighlight, newErrorColour);
        
        ConfigurationSection settings = main.getConfigurationSection("settings");
		if (settings != null) {
			passiveRefreshRate = (int) (settings.getDouble("passives-refresh-rate", 1) * 20);
		}
	}
		
	private int passiveRefreshRate = 20;
	public int getPassiveRefreshRate() {
		return passiveRefreshRate;
	}

	/*==================*/
	//		WANDS		//
	/*==================*/

	private static final ArrayList<File> wandFiles = new ArrayList<>();

	private void loadWands() {
		final File wandsDirectory =  new File(plugin.getDataFolder() + File.separator + "wands");
		
		MagicWand.clear();
		errors.clear();
		wandFiles.clear();
		
		for (File file : wandsDirectory.listFiles()) {
			if (file.getName().endsWith(".yml")) {
				wandFiles.add(file);
			}
		}
		
		for (File wandFile : wandFiles) {
			YamlConfiguration specs = loadConfigSafely(wandFile);
			String wandName = wandFile.getName().substring(0, wandFile.getName().lastIndexOf('.'));

			parseWandConfig(specs, wandName);
		}
		if (errors.size() != 0) {
			logger.warning("The wands were loaded with " + errors.size() + " error(s). Do /magic errors to view them.");
		}
	}
	
	private void parseWandConfig(FileConfiguration specs, String wandName) {

		String directory = wandName + ".yml";
		String displayName = specs.getString("display");
		if (displayName != null) {
			/*
			 *  Due to the server removing white at the start of strings, this is needed
			 *  to ensure the wand is identifiable.
			 */
			if (displayName.startsWith("&f")) {
				displayName = "&r" + displayName.substring(2);
			}
			displayName = ChatColor.translateAlternateColorCodes('&', displayName);
		} else {
			logError("Missing display name of wand.", directory + "/display");
			displayName = wandName;
		}

		Material material = Material.STICK;
		String itemString = specs.getString("item");
		if (itemString != null) {
			String materialAsString = itemString.toUpperCase().replace(" ", "_");
			try {
				material = Material.valueOf(materialAsString);
			} catch (IllegalArgumentException e) {
				logError("Invalid material.", directory + "/item");
			}
		} else {
			logError("Item field missing.", directory + "/item");
		}

		int level = 0;
		if (specs.get("requires-level") != null) {
			level = specs.getInt("requires-level");
		}
		
		MagicWand newWand = new MagicWand(wandName, displayName, material, level);

		if (specs.get("lore") != null) {
			List<String> givenLore = specs.getStringList("lore");
			List<String> lore = new LinkedList<>();
			for (String line : givenLore) {
				lore.add(ChatColor.translateAlternateColorCodes('&', line));
			}
			
			newWand.setLore(lore);
		}
		
		if (specs.get("shiny") != null) {
			boolean shiny = specs.getBoolean("shiny");
			newWand.setShiny(shiny);
		}
		
		boolean sendErrors = false;
		if (specs.get("send-errors") != null) {
			sendErrors = specs.getBoolean("send-errors");
		}
		
		boolean cancelDrops = false;
		if (specs.get("cancel-drops") != null) {
			cancelDrops = specs.getBoolean("cancel-drops");
		}
		
		newWand.doErrorMessages(sendErrors);
		newWand.cancelDrops(cancelDrops);
		
		String permission;
		if (specs.get("permission") != null) {
			permission = specs.getString("permission");
			newWand.setPermission(permission);
		}

		ConfigurationSection passives = specs.getConfigurationSection("passives");
		if (passives != null) {
			String parentDir = wandName + ".yml/passives";
			for (String section : passives.getKeys(false)) { // Iterate over tiers
				ConfigurationSection passive = passives.getConfigurationSection(section);
				PassiveEffectType passiveType = PassiveEffectType.fromString(section);
				
				if (passiveType == null) {
					logError("There is no passive type for \"" + section + "\".", parentDir);
					continue;
				}
				
				directory = parentDir + "/" + section + "/";
				PassiveEffect effect = PassiveEffectType.newObject(passiveType, passive, directory);
				assert(effect != null);
				newWand.addPassive(effect);
			}
		}
		
		ConfigurationSection bindings = specs.getConfigurationSection("bindings");
		if (bindings == null && passives == null) {
			logError("Either the binding section, or the passives section must exist.", directory);
			return;
		}

		int maxTier = 1;
		if (bindings == null) {
			newWand.doErrorMessages(false);
		} else {
			for (String tier : bindings.getKeys(false)) { // Iterate over tiers
				String parentDir = wandName + ".yml/bindings/" + tier;

				int internalTier;
				try {
					internalTier = Integer.parseInt(tier);
				} catch (NumberFormatException e) {
					logError("Tiers must be numeric", directory);
					continue;
				}
				
				maxTier = Math.max(maxTier, internalTier);
				
				ConfigurationSection binding = bindings.getConfigurationSection(tier);

				assert(binding != null);

				for (String controlSec : binding.getKeys(false)) { // Iterate over bindings
					directory = parentDir + "/" + controlSec;
					
					WandControl control;
					String controlString = controlSec.toUpperCase().replace(" ", "_");
					try {
						control = WandControl.valueOf(controlString);
					} catch (IllegalArgumentException e) {
						logError("Invalid control.", directory);
						continue;
					}

					ConfigurationSection controlSection = binding.getConfigurationSection(controlString);
					assert(controlSection != null);

					SpellConfig spellConfig = SpellConfig.fromConfigSection(controlSection, directory);

					if (spellConfig == null) continue;

					SpellInstance spell = spellConfig.buildSpell(directory);
				//	GenericSpell spell = spellConfig.getSpellPreSpellTypeRemoval(binding.getConfigurationSection(controlString), directory);// = GenericSpell.getFromConfig(binding.getConfigurationSection(controlString), directory);
					
					if (spell == null) continue;

					if (spell.getType().requiresShift()) {
						if (!control.isShift()) {
							logError(spell.getType() + " must be on a control that starts with SHIFT. (You used: " + control.toString() + ")", directory);
							continue;
						}
					}

					newWand.addSpell(internalTier, control, spell);
						
				}
			}
		}
		newWand.setMaxTier(maxTier);
	}
	
}
