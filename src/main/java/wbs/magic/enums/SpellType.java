package wbs.magic.enums;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import wbs.magic.MagicSettings;
import wbs.magic.WbsMagic;
import wbs.magic.spellinstances.*;
import wbs.magic.spellinstances.ranged.projectile.*;

import wbs.utils.util.WbsSound;
import wbs.utils.util.WbsSoundGroup;
import wbs.utils.util.string.WbsStrings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public enum SpellType {
	LEAP, ELDRITCH_BLAST, SHIELD, HOLD, CHECK_MANA,
	BLINK, FIREBOLT, REGENERATE, CONE_OF_COLD, CHAIN_LIGHTNING,
	MAGIC_MISSILE, REGENERATE_MANA, NEGATE_MAGIC,
	
	ARCANE_SURGE, VOID_STEP, CONFUSE, PUSH, PULL, ENERGY_BURST,
	WARP, PRISMATIC_RAY, DISARM, COUNTER_SPELL,
	
	DRAIN_LIFE, DISPLACE, TORNADO, FROST_SHARDS, HEX, FAERIE_FIRE,
	DIVINE_SHIELD, LIFE_LINK, NECROTIC_PACT, HASTE, RADIANCE,
	
	BLIZZARD, INFLICT_WOUNDS, IMBUE_CREATURE, FIREBALL, DEPTH_SURGE,
	WATER_WALK, FLY,

	HALLUCINATION, RECALL;
	
	
	// How often the shield should refresh its position
	public static int shieldRefreshRate = 5;

	/************ Default Features *************/
	
	private int defaultCost;
	private double cooldown; // in seconds
	private boolean isConcentration = false;
	private String description = null;
	
	private String failDescription = null; // How the spell can go wrong.
	
	// If applicable, the death message format with %victim% and %attacker% for the names.
	private String deathMessageFormat = null; 
	// If a player kills themself, this format will replace %player% with their username.
	private String suicideFormat = null; 
	
	/************ Immutable Features *************/

	private Class<? extends SpellInstance> spellClass;

	private boolean isContinuousCast = false;
	private boolean canBeConcentration = false;
	private boolean isDamageSpell = false;
	private boolean canFail = false;
	private boolean requiresShift = false;
	
	private WbsSoundGroup castSound = null;

	/************ Methods *************/
	
	/**
	 * Configure the spell defaults where applicable from
	 * a yaml config
	 * @param config The config to load from.
	 */
	public static void loadDefaults(FileConfiguration config) {
		MagicSettings settings = MagicSettings.getInstance();
		String directory = "spells.yml/";
		for (SpellType type : values()) {
			ConfigurationSection section = config.getConfigurationSection(type.name());
			if (section != null) {
				type.defaultCost = section.getInt("default-cost");
				type.cooldown = section.getInt("cooldown");
				type.description = section.getString("description");
				
				if (type.canBeConcentration) {
					type.isConcentration = section.getBoolean("is-concentration");
				} else {
					if (section.contains("is-concentration")) {
						settings.logError("This spell cannot be concentration.", directory + type.name());
					}
				}
				
				if (type.isDamageSpell) {
					type.deathMessageFormat = section.getString("death-message-format");
					type.suicideFormat = section.getString("suicide-format");
				} else {
					if (section.contains("death-message-format") || section.contains("suicide-format")) {
						settings.logError("This spell cannot deal damage.", directory + type.name());
					}
				}
				
				if (type.canFail) {
					type.failDescription = section.getString("fail-description");
				} else {
					if (section.contains("fail-description")) {
						settings.logError("This spell cannot fail.", directory + type.name());
					}
				}
				
			} else {
				settings.logError("A spell config was missing", directory + type.name());
			}
		}
	}
	
	static {

	    LEAP.spellClass = Leap.class;
		ELDRITCH_BLAST.spellClass = EldritchBlast.class;

		LEAP.isContinuousCast = false;
		LEAP.canBeConcentration = false;
		LEAP.isDamageSpell = false;
		LEAP.canFail = true;
		LEAP.requiresShift = false;
		
		SHIELD.isContinuousCast = true;
		SHIELD.canBeConcentration = false;
		SHIELD.isDamageSpell = false;
		SHIELD.canFail = true;
		SHIELD.requiresShift = true;
		
		HOLD.isContinuousCast = false;
		HOLD.canBeConcentration = true;
		HOLD.isDamageSpell = false;
		HOLD.canFail = false;
		HOLD.requiresShift = false;
		
		ELDRITCH_BLAST.isContinuousCast = false;
		ELDRITCH_BLAST.canBeConcentration = false;
		ELDRITCH_BLAST.isDamageSpell = true;
		ELDRITCH_BLAST.canFail = false;
		ELDRITCH_BLAST.requiresShift = false;
		
		FIREBOLT.isContinuousCast = false;
		FIREBOLT.canBeConcentration = false;
		FIREBOLT.isDamageSpell = true;
		FIREBOLT.canFail = true;
		FIREBOLT.requiresShift = false;

		BLINK.isContinuousCast = false;
		BLINK.canBeConcentration = false;
		BLINK.isDamageSpell = false;
		BLINK.canFail = true;
		BLINK.requiresShift = false;

		REGENERATE.isContinuousCast = true;
		REGENERATE.canBeConcentration = false;
		REGENERATE.isDamageSpell = false;
		REGENERATE.canFail = false;
		REGENERATE.requiresShift = true;

		REGENERATE_MANA.isContinuousCast = true;
		REGENERATE_MANA.canBeConcentration = false;
		REGENERATE_MANA.isDamageSpell = false;
		REGENERATE_MANA.canFail = false;
		REGENERATE_MANA.requiresShift = true;

		CONE_OF_COLD.isContinuousCast = true;
		CONE_OF_COLD.canBeConcentration = false;
		CONE_OF_COLD.isDamageSpell = true;
		CONE_OF_COLD.canFail = false;
		CONE_OF_COLD.requiresShift = true;

		CONFUSE.isContinuousCast = false;
		CONFUSE.canBeConcentration = false;
		CONFUSE.isDamageSpell = false;
		CONFUSE.canFail = false;
		CONFUSE.requiresShift = false;

		NEGATE_MAGIC.isContinuousCast = false;
		NEGATE_MAGIC.canBeConcentration = false;
		NEGATE_MAGIC.isDamageSpell = false;
		NEGATE_MAGIC.canFail = false;
		NEGATE_MAGIC.requiresShift = false;

		MAGIC_MISSILE.isContinuousCast = false;
		MAGIC_MISSILE.canBeConcentration = true;
		MAGIC_MISSILE.isDamageSpell = true;
		MAGIC_MISSILE.canFail = false;
		MAGIC_MISSILE.requiresShift = false;

		ARCANE_SURGE.isContinuousCast = true; // TODO: update code to make continuous
		ARCANE_SURGE.canBeConcentration = false;
		ARCANE_SURGE.isDamageSpell = true;
		ARCANE_SURGE.canFail = false;
		ARCANE_SURGE.requiresShift = false;

		VOID_STEP.isContinuousCast = false;
		VOID_STEP.canBeConcentration = false;
		VOID_STEP.isDamageSpell = true;
		VOID_STEP.canFail = true;
		VOID_STEP.requiresShift = false;

		CHAIN_LIGHTNING.isContinuousCast = false;
		CHAIN_LIGHTNING.canBeConcentration = false;
		CHAIN_LIGHTNING.isDamageSpell = true;
		CHAIN_LIGHTNING.canFail = true;
		CHAIN_LIGHTNING.requiresShift = false;

		PUSH.isContinuousCast = false;
		PUSH.canBeConcentration = false;
		PUSH.isDamageSpell = false;
		PUSH.canFail = false;
		PUSH.requiresShift = false;

		PULL.isContinuousCast = false;
		PULL.canBeConcentration = false;
		PULL.isDamageSpell = false;
		PULL.canFail = false;
		PULL.requiresShift = false;
		
		ENERGY_BURST.isContinuousCast = false;
		ENERGY_BURST.canBeConcentration = false;
		ENERGY_BURST.isDamageSpell = true;
		ENERGY_BURST.canFail = false;
		ENERGY_BURST.requiresShift = false;

		WARP.isContinuousCast = false;
		WARP.canBeConcentration = false;
		WARP.isDamageSpell = false;
		WARP.canFail = true;
		WARP.requiresShift = false;

		PRISMATIC_RAY.isContinuousCast = false;
		PRISMATIC_RAY.canBeConcentration = false;
		PRISMATIC_RAY.isDamageSpell = true;
		PRISMATIC_RAY.canFail = false;
		PRISMATIC_RAY.requiresShift = false;

		DISARM.isContinuousCast = false;
		DISARM.canBeConcentration = false;
		DISARM.isDamageSpell = false;
		DISARM.canFail = true;
		DISARM.requiresShift = false;

		COUNTER_SPELL.isContinuousCast = false;
		COUNTER_SPELL.canBeConcentration = false;
		COUNTER_SPELL.isDamageSpell = false;
		COUNTER_SPELL.canFail = true;
		COUNTER_SPELL.requiresShift = false;

		DRAIN_LIFE.isContinuousCast = true; // TODO: Add continuous cast nature to drain life
		DRAIN_LIFE.canBeConcentration = false;
		DRAIN_LIFE.isDamageSpell = true;
		DRAIN_LIFE.canFail = true;
		DRAIN_LIFE.requiresShift = true;

		DISPLACE.isContinuousCast = false;
		DISPLACE.canBeConcentration = false;
		DISPLACE.isDamageSpell = false;
		DISPLACE.canFail = false;
		DISPLACE.requiresShift = false;

		TORNADO.isContinuousCast = false;
		TORNADO.canBeConcentration = true;
		TORNADO.isDamageSpell = false;
		TORNADO.canFail = true;
		TORNADO.requiresShift = false;

		FROST_SHARDS.isContinuousCast = true;
		FROST_SHARDS.canBeConcentration = false;
		FROST_SHARDS.isDamageSpell = true;
		FROST_SHARDS.canFail = false;
		FROST_SHARDS.requiresShift = true;

		HEX.isContinuousCast = false;
		HEX.canBeConcentration = false;
		HEX.isDamageSpell = false;
		HEX.canFail = false;
		HEX.requiresShift = false;

		FAERIE_FIRE.isContinuousCast = false;
		FAERIE_FIRE.canBeConcentration = false;
		FAERIE_FIRE.isDamageSpell = true;
		FAERIE_FIRE.canFail = false;
		FAERIE_FIRE.requiresShift = false;

		DIVINE_SHIELD.isContinuousCast = false;
		DIVINE_SHIELD.canBeConcentration = false;
		DIVINE_SHIELD.isDamageSpell = false;
		DIVINE_SHIELD.canFail = true;
		DIVINE_SHIELD.requiresShift = false;

		LIFE_LINK.isContinuousCast = false;
		LIFE_LINK.canBeConcentration = false;
		LIFE_LINK.isDamageSpell = true;
		LIFE_LINK.canFail = false;
		LIFE_LINK.requiresShift = false;

		NECROTIC_PACT.isContinuousCast = false;
		NECROTIC_PACT.canBeConcentration = false;
		NECROTIC_PACT.isDamageSpell = false;
		NECROTIC_PACT.canFail = false;
		NECROTIC_PACT.requiresShift = false;

		BLIZZARD.isContinuousCast = false;
		BLIZZARD.canBeConcentration = true;
		BLIZZARD.isDamageSpell = true;
		BLIZZARD.canFail = false;
		BLIZZARD.requiresShift = false;

		INFLICT_WOUNDS.isContinuousCast = false;
		INFLICT_WOUNDS.canBeConcentration = false;
		INFLICT_WOUNDS.isDamageSpell = true;
		INFLICT_WOUNDS.canFail = false;
		INFLICT_WOUNDS.requiresShift = false;

		FIREBALL.isContinuousCast = false;
		FIREBALL.canBeConcentration = false;
		FIREBALL.isDamageSpell = true;
		FIREBALL.canFail = false;
		FIREBALL.requiresShift = false;

		DEPTH_SURGE.isContinuousCast = false;
		DEPTH_SURGE.canBeConcentration = false;
		DEPTH_SURGE.isDamageSpell = true;
		DEPTH_SURGE.canFail = true;
		DEPTH_SURGE.requiresShift = false;

		WATER_WALK.isContinuousCast = false;
		WATER_WALK.canBeConcentration = false;
		WATER_WALK.isDamageSpell = false;
		WATER_WALK.canFail = true;
		WATER_WALK.requiresShift = false;
		
		FLY.isContinuousCast = false;
		FLY.canBeConcentration = true;
		FLY.isDamageSpell = false;
		FLY.canFail = false;
		FLY.requiresShift = false;
		
		
		// Cast sounds
		ELDRITCH_BLAST.castSound = new WbsSoundGroup(
				new WbsSound(Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.5F)
				);

		FIREBOLT.castSound = new WbsSoundGroup(
				new WbsSound(Sound.ENTITY_BLAZE_SHOOT, 0.5F)
				);
		
		ENERGY_BURST.castSound = new WbsSoundGroup(
				new WbsSound(Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.5F)
				);
		
		LEAP.castSound = new WbsSoundGroup(
				new WbsSound(Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1),
				new WbsSound(Sound.ENTITY_PHANTOM_FLAP, 1, 0.5F)
				);
		
		
		BLINK.castSound = new WbsSoundGroup(
				new WbsSound(Sound.ENTITY_ENDERMAN_TELEPORT, 1.25F)
				);
		
		WARP.castSound = new WbsSoundGroup(
				new WbsSound(Sound.BLOCK_BEACON_POWER_SELECT, 1F)
				);

		NEGATE_MAGIC.castSound = new WbsSoundGroup(
				new WbsSound(Sound.ENTITY_WITHER_SPAWN, 2, 0.3F)
				);

		VOID_STEP.castSound = new WbsSoundGroup(
				new WbsSound(Sound.ENTITY_WITHER_DEATH, 2, 0.5F)
				);
		
		ARCANE_SURGE.castSound = new WbsSoundGroup(
				new WbsSound(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED)
				);
	}
	
	public SpellInstance toSpell(ConfigurationSection config, String directory) {


		SpellInstance spell;

		try {
			Constructor<? extends SpellInstance> constructor =
					getSpellClass().getConstructor(ConfigurationSection.class, String.class);

			spell = constructor.newInstance(config, directory);

		} catch (SecurityException | NoSuchMethodException | InstantiationException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			WbsMagic.getInstance().settings.logError("Invalid constructor for spell type " + this, "SpellConfig generation");
			e.printStackTrace();
			return null;
		}

		return spell;
	}

	public Class<? extends SpellInstance> getSpellClass() {
	    return spellClass;
    }

	public String getDescription() {
		return description;
	}
	
	public String getFailDescription() {
		return failDescription;
	}
	
	public int getDefaultCost() {
		return defaultCost;
	}

	public double getCooldown() {
		return cooldown;
	}
	
	public boolean requiresShift() {
		return requiresShift;
	}
	
	public String getName() {
		return WbsStrings.capitalizeAll(this.toString().replace('_', ' '));
	}
	
	public boolean isConcentration() {
		return isConcentration;
	}

	/**
     * Whether or not a spell needs to be continually cast
     * @return True if the spell is continuous-cast
     */
	public boolean isContinuousCast() {
		return isContinuousCast;
	}


	public WbsSoundGroup getCastSound() {
		return castSound;
	}

	public void setCastSound(WbsSoundGroup castSound) {
		this.castSound = castSound;
	}
	
	public String getDeathMessage(Player victim, Player attacker) {
		String deathMessage = null;
		
		if (victim.equals(attacker)) {
			if (suicideFormat == null) {
				deathMessage = deathMessageFormat.replaceAll("%victim%", victim.getName());
				deathMessage = deathMessage.replaceAll("%attacker%", attacker.getName());
			} else {
				deathMessage = suicideFormat.replaceAll("%player%", victim.getName());
			}
		} else {
			if (deathMessageFormat == null) {
				return null;
			} else {
				deathMessage = deathMessageFormat.replaceAll("%victim%", victim.getName());
				deathMessage = deathMessage.replaceAll("%attacker%", attacker.getName());
			}
		}
		
		return deathMessage;
	}
}