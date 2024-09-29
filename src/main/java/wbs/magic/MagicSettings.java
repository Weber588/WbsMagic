package wbs.magic;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import wbs.magic.controls.CastTrigger;
import wbs.magic.controls.WandControl;
import wbs.magic.controls.conditions.CastCondition;
import wbs.magic.controls.conditions.SneakCondition;
import wbs.magic.passives.PassiveEffect;
import wbs.magic.passives.PassiveEffectType;
import wbs.magic.spellmanagement.*;
import wbs.magic.spellmanagement.configuration.ControlRestrictions;
import wbs.magic.spells.ChangeTier;
import wbs.magic.spells.SpellInstance;
import wbs.magic.wand.ConfiguredAttribute;
import wbs.magic.wand.MagicWand;
import wbs.magic.wand.SimpleWandControl;
import wbs.magic.wand.SpellBinding;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.configuration.WbsConfigReader;
import wbs.utils.util.plugin.WbsSettings;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MagicSettings extends WbsSettings {

	public static final PotionEffectType[] DEFAULT_NEGATIVE_POTIONS = {
			PotionEffectType.BLINDNESS,
			PotionEffectType.NAUSEA,
			PotionEffectType.DARKNESS,
			PotionEffectType.INSTANT_DAMAGE,
			PotionEffectType.HUNGER,
			PotionEffectType.LEVITATION,
			PotionEffectType.POISON,
			PotionEffectType.SLOWNESS,
			PotionEffectType.MINING_FATIGUE,
			PotionEffectType.UNLUCK,
			PotionEffectType.WEAKNESS,
			PotionEffectType.WITHER
	};

	public static final PotionEffectType[] DEFAULT_POSITIVE_POTIONS = {
			PotionEffectType.ABSORPTION,
			PotionEffectType.CONDUIT_POWER,
			PotionEffectType.RESISTANCE,
			PotionEffectType.DOLPHINS_GRACE,
			PotionEffectType.HASTE,
			PotionEffectType.FIRE_RESISTANCE,
			PotionEffectType.INSTANT_HEALTH,
			PotionEffectType.HEALTH_BOOST,
			PotionEffectType.HERO_OF_THE_VILLAGE,
			PotionEffectType.STRENGTH,
			PotionEffectType.INVISIBILITY,
			PotionEffectType.JUMP_BOOST,
			PotionEffectType.LUCK,
			PotionEffectType.NIGHT_VISION,
			PotionEffectType.REGENERATION,
			PotionEffectType.SATURATION,
			PotionEffectType.SPEED,
			PotionEffectType.WATER_BREATHING
	};
	
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

		CastCondition.loadConditions();

		loadConfigs();

		loadNativeSpells();

		loadSpellDefaults();
		
		loadMain();
		loadWands();
	}

	private void loadNativeSpells() {
		SpellManager.clear();
		SpellLoader loader = new NativeSpellLoader();
		SpellManager.registerClasses(loader);
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

		Map<RegisteredSpell, Boolean> spellToExisting = new LinkedHashMap<>();
		SpellManager.getSpells().values().forEach(spell -> spellToExisting.put(spell, false));

		int added = spellToExisting.size();

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

			added--;
			spellToExisting.put(checkSpell, true);
		}

		for (RegisteredSpell registration : spellToExisting.keySet()) {
			ConfigurationSection spellSection;
			if (!spellToExisting.get(registration)) { // New
				spellSection = config.createSection(registration.getName());
			} else { // Existing
				spellSection = config.getConfigurationSection(registration.getName());
				assert spellSection != null; // If section was null, spell would be new
			}

			registration.buildDefaultConfig(spellSection,"spells.yml/" + registration.getName());

			if (!spellToExisting.get(registration)) {
				registration.toConfigSection(spellSection);
				config.set(registration.getName(), spellSection);
			}
		}

		try {
			config.save(configFile);
		} catch (IOException e) {
			logger.severe("An unknown error occurred when saving spells.yml");
			e.printStackTrace();
		}

		if (added != 0) {
			logger.info("Added " + added + " new spells to spells.yml");
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
			retrieveByWandName = settings.getBoolean("retrieve-by-wand-name", retrieveByWandName);
			useXPForCost = settings.getBoolean("use-xp-for-cost", useXPForCost);
			maxMana = settings.getInt("max-mana", 500);

			String directory = "config.yml/settings/";

			List<String> positivePotionStrings = settings.getStringList("positive-potions");
			for (String potString : positivePotionStrings) {
				PotionEffectType type = PotionEffectType.getByName(potString);

				if (type != null) {
					positivePotions.add(type);
				} else {
					logError("Unknown potion type: " + potString + ". Outdated/future potion effect?", directory + "/positive-potions");
				}
			}

			if (positivePotions.isEmpty()) {
				positivePotions.addAll(Arrays.asList(DEFAULT_POSITIVE_POTIONS));
			}

			List<String> negativePotionStrings = settings.getStringList("negative-potions");
			for (String potString : negativePotionStrings) {
				PotionEffectType type = PotionEffectType.getByName(potString);

				if (type != null) {
					negativePotions.add(type);
				} else {
					logError("Unknown potion type: " + potString + ". Outdated/future potion effect?", directory + "/negative-potions");
				}
			}
		}
	}
		
	private int passiveRefreshRate = 20;
	public int getPassiveRefreshRate() {
		return passiveRefreshRate;
	}
	private boolean retrieveByWandName = false;
	public boolean retrieveByWandName() {
		return retrieveByWandName;
	}
	private boolean useXPForCost = false;
	public boolean useXPForCost() {
		return useXPForCost;
	}
	public int maxMana;

	@NotNull
	public final List<PotionEffectType> positivePotions = new LinkedList<>();
	@NotNull
	public final List<PotionEffectType> negativePotions = new LinkedList<>();

	/*==================*/
	//		WANDS		//
	/*==================*/

	private static final ArrayList<File> wandFiles = new ArrayList<>();

	private void loadWands() {
		final File wandsDirectory =  new File(plugin.getDataFolder() + File.separator + "wands");
		
		MagicWand.clear();
		errors.clear();
		wandFiles.clear();
		
		for (File file : Objects.requireNonNull(wandsDirectory.listFiles())) {
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
	
	@SuppressWarnings("ConstantConditions")
	private void parseWandConfig(ConfigurationSection specs, String wandName) {

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
		
		MagicWand newWand = new MagicWand(wandName, displayName, material);

		if (specs.get("lore") != null) {
			List<String> givenLore = specs.getStringList("lore");
			List<String> lore = new LinkedList<>();
			for (String line : givenLore) {
				lore.add(ChatColor.translateAlternateColorCodes('&', line));
			}
			
			newWand.setLore(lore);
		}
		
		if (specs.isBoolean("shiny")) {
			newWand.setShiny(specs.getBoolean("shiny"));
		}

		ConfigurationSection optionsSection = specs.getConfigurationSection("options");

		if (optionsSection != null) {
			boolean cancelDrops = false;
			cancelDrops = optionsSection.getBoolean("prevent-drops", cancelDrops);
			cancelDrops = optionsSection.getBoolean("cancel-drops", cancelDrops);

			boolean preventCombat = false;
			preventCombat = !optionsSection.getBoolean("allow-combat", !preventCombat);
			preventCombat = optionsSection.getBoolean("prevent-combat", preventCombat);

			boolean preventBlockPlacing = false;
			preventBlockPlacing = !optionsSection.getBoolean("allow-block-placing", !preventBlockPlacing);
			preventBlockPlacing = optionsSection.getBoolean("prevent-block-placing", preventBlockPlacing);

			boolean preventBlockBreaking = false;
			preventBlockBreaking = !optionsSection.getBoolean("allow-block-breaking", !preventBlockBreaking);
			preventBlockBreaking = !optionsSection.getBoolean("prevent-block-breaking", preventBlockBreaking);

			boolean disarmImmune = false;
			disarmImmune = optionsSection.getBoolean("disarm-immune", disarmImmune);

			newWand.preventDrops(cancelDrops);
			newWand.setPreventCombat(preventCombat);
			newWand.setPreventBlockPlacing(preventBlockPlacing);
			newWand.setPreventBlockBreaking(preventBlockBreaking);
			newWand.setDisarmImmune(disarmImmune);
		}

		String permission;
		if (specs.isString("permission")) {
			permission = specs.getString("permission");
			newWand.setPermission(permission);
		}

		List<ItemFlag> itemFlags = WbsConfigReader.getEnumList(specs, "item-flags", this, directory, ItemFlag.class);
		newWand.addItemFlags(itemFlags);

		ConfigurationSection enchantments = specs.getConfigurationSection("enchantments");
		if (enchantments != null) {
			for (String key : enchantments.getKeys(false)) {
				if (!enchantments.isInt(key)) {
					logError("Enchantment must be an integer.", directory + "/enchantments/" + key);
					continue;
				}

				int level = enchantments.getInt(key);
				if (level < 1) {
					logError("Level must be 1 or greater.", directory + "/enchantments/" + key);
					continue;
				}

				boolean enchantmentFound = false;

				String enchantmentName = key.trim().replace(" ", "").replace("_", "");

				for (Enchantment enchantment : Enchantment.values()) {
					String check = enchantment.getKey().getKey();
					check = check.replace(" ", "").replace("_", "");
					if (check.equalsIgnoreCase(enchantmentName)) {
						newWand.addEnchantment(enchantment, level);
						enchantmentFound = true;
						break;
					}
				}

				if (!enchantmentFound) {
					for (Enchantment enchantment : Enchantment.values()) {
						//noinspection deprecation
						String check = enchantment.getName();
						check = check.replace(" ", "").replace("_", "");
						if (check.equalsIgnoreCase(enchantmentName)) {
							newWand.addEnchantment(enchantment, level);
							enchantmentFound = true;
							break;
						}
					}
				}

				if (!enchantmentFound) {
					logError("Enchantment not found: " + key, directory + "/enchantments/" + key);
				}
			}
		}

		ConfigurationSection attributes = specs.getConfigurationSection("attributes");
		if (attributes != null) {
			String attributeDirectory = directory + "/attributes";
			for (String slotKey : attributes.getKeys(false)) {
				EquipmentSlot slot = WbsEnums.getEnumFromString(EquipmentSlot.class, slotKey);

				if (slot == null) {
					logError("Invalid slot: " + slotKey, attributeDirectory);
					continue;
				}

				ConfigurationSection attributeSection = attributes.getConfigurationSection(slotKey);

				if (attributeSection != null) {
					for (String attributeKey : attributeSection.getKeys(false)) {
						Attribute attribute = WbsEnums.getEnumFromString(Attribute.class, attributeKey);

						if (attribute == null) {
							logError("Invalid attribute: " + attributeKey, attributeDirectory + "/" + slotKey);
							continue;
						}

						if (!attributeSection.isDouble(attributeKey)) {
							logError(attributeKey + " must be a double.", attributeDirectory + "/" + slotKey);
							continue;
						}

						double amount = attributeSection.getDouble(attributeKey);

						ConfiguredAttribute configuredAttribute = new ConfiguredAttribute(slot, attribute, amount);
						newWand.addAttribute(configuredAttribute);
					}
				}
			}
		}

		ConfigurationSection passives = specs.getConfigurationSection("passives");
		if (passives != null) {
			String passiveDir = directory + "/passives";
			for (String slotKey : passives.getKeys(false)) {
				EquipmentSlot slot = WbsEnums.getEnumFromString(EquipmentSlot.class, slotKey);

				if (slot == null) {
					logError("Invalid slot: " + slotKey, passiveDir + "/" + slotKey);
					continue;
				}

				ConfigurationSection slotSection = passives.getConfigurationSection(slotKey);
				if (slotSection != null) {
					String slotDirectory = passiveDir + "/" + slotKey;
					for (String passiveKey : slotSection.getKeys(false)) {
						ConfigurationSection passiveSection = slotSection.getConfigurationSection(passiveKey);
						PassiveEffectType passiveType = WbsEnums.getEnumFromString(PassiveEffectType.class, passiveKey);

						if (passiveType == null) {
							logError("There is no passive type for \"" + passiveKey + "\".", slotDirectory);
							continue;
						}

						if (passiveSection == null) {
							logError(passiveKey + " must be a section.", slotDirectory);
							continue;
						}

						PassiveEffect effect = PassiveEffectType.newObject(passiveType, passiveSection, directory);
						assert(effect != null);
						newWand.addPassive(slot, effect);
					}
				}
			}
		}
		
		ConfigurationSection bindings = specs.getConfigurationSection("bindings");

		int maxTier = 1;
		if (bindings != null) {
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
					
					SimpleWandControl control = WbsEnums.getEnumFromString(SimpleWandControl.class, controlSec);

					ConfigurationSection spellSection = binding.getConfigurationSection(controlSec);

					if (control != null) {
						addSimpleBinding(newWand, internalTier, control, spellSection, directory);
					} else {
						addComplexBinding(newWand, internalTier, spellSection, directory);
					}
				}
			}

			if (maxTier > 1) {
				boolean wasMissingTierChange = false;

				for (int tier = 1; tier <= maxTier; tier++) {
					boolean foundTierChange = false;
					for (SpellBinding binding : newWand.bindingMap().get(tier)) {
						if (binding.getSpell().getClass().equals(ChangeTier.class)) {
							foundTierChange = true;
							break;
						}
					}
					// No tier change found with higher tiers on wand
					if (!foundTierChange) {
						wasMissingTierChange = true;

						SpellInstance changeTierSpell =
								new ChangeTier(new SpellConfig(SpellManager.getSpell(ChangeTier.class)), "Internal");

						CastTrigger trigger = new CastTrigger(WandControl.DROP);
						trigger.setPriority(Integer.MAX_VALUE);
						SpellBinding binding = new SpellBinding(trigger, changeTierSpell);

						newWand.addSpell(tier, binding);
					}
				}

				if (wasMissingTierChange) {
					plugin.logger.warning("No tier change found on one or more tiers for wand \"" + wandName + "\". " +
							"Wands with tiers require the Change Tier spell to be on every tier. " +
							"Change Tier has been defined as Drop for all tiers missing.");
				}
			}
		}

		newWand.setMaxTier(maxTier);
	}

	private void addComplexBinding(MagicWand wand, int tier, ConfigurationSection spellSection, String directory) {
		SpellConfig spellConfig = SpellConfig.fromConfigSection(spellSection, directory);

		if (spellConfig == null) return;

		SpellInstance spell = spellConfig.buildSpell(directory);
		if (spell == null) return;

		RegisteredSpell registeredSpell = spell.getRegisteredSpell();

		ConfigurationSection triggerSection = spellSection.getConfigurationSection("trigger");

		if (triggerSection == null) {
			logError("Missing trigger/control for " + spellSection.getName() + ". " +
					"Create a trigger section, or use a SimpleWandControl (see &h/wbsmagic guide controls&w)", directory);
			return;
		}

		CastTrigger trigger;
		try {
			trigger = new CastTrigger(triggerSection, directory + "/trigger");
		} catch (InvalidConfigurationException e) {
			logError(e.getMessage(), directory);
			return;
		}

		ControlRestrictions controlRestrictions = registeredSpell.getControlRestrictions();

		List<WandControl> validControls = controlRestrictions.validControls();
		// Only process if the list is populated, otherwise unrestricted
		if (!validControls.isEmpty()) {
			if (!validControls.contains(trigger.getControl())) {
				logError(registeredSpell.getName() + " is not valid with " + WbsEnums.toPrettyString(trigger.getControl()) + "!"
						+ " Please choose from the following: "
						+ validControls.stream().map(WbsEnums::toPrettyString).collect(Collectors.joining(", ")), directory);
				return;
			}
		}

		if (controlRestrictions.requiresShift()) {
			List<SneakCondition> sneakConditions = trigger.getConditions(SneakCondition.class);
			for (SneakCondition condition : sneakConditions) {
				if (!condition.getComparison()) {
					logError(registeredSpell.getName() + " must allow shifting.", directory);
					return;
				}
			}

			if (sneakConditions.isEmpty()) {
				trigger.addCondition(new SneakCondition(Collections.singletonList("true"), "Internal"));
			}
		}

		SpellBinding binding = new SpellBinding(trigger, spell);

		wand.addSpell(tier, binding);
	}

	private void addSimpleBinding(MagicWand wand, int tier, SimpleWandControl control, ConfigurationSection spellSection, String directory) {
		SpellConfig spellConfig = SpellConfig.fromConfigSection(spellSection, directory);

		if (spellConfig == null) return;

		SpellInstance spell = spellConfig.buildSpell(directory);
		if (spell == null) return;

		RegisteredSpell registeredSpell = spell.getRegisteredSpell();

		ControlRestrictions controlRestrictions = registeredSpell.getControlRestrictions();

		List<WandControl> validControls = controlRestrictions.validControls();
		if (!validControls.isEmpty()) {
			if (!validControls.contains(control.toWandControl())) {
				logError(registeredSpell.getName() + " is not valid with " + WbsEnums.toPrettyString(control.toWandControl()) + "!"
						+ " Please choose from the following: "
						+ validControls.stream().map(WbsEnums::toPrettyString).collect(Collectors.joining(", ")), directory);
				return;
			}
		}

		if (controlRestrictions.requiresShift()) {
			if (!control.isShift()) {
				logError(registeredSpell.getName() + " must be on a control that starts with SHIFT. (You used: " + control + ")", directory);
				return;
			}
		}

		SpellBinding binding = new SpellBinding(control.toTrigger(directory), spell);

		wand.addSpell(tier, binding);
	}
	
}
