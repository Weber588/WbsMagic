package wbs.magic.passives;

import org.bukkit.configuration.ConfigurationSection;

import wbs.utils.util.WbsEnums;

public enum PassiveEffectType {
	DAMAGE_IMMUNITY, DAMAGE_RESISTANCE, DISARM, POTION,
	FREEDOM_OF_MOVEMENT, AGILITY, ELUSIVE;
	
	private String description;
	
	static {
		DAMAGE_IMMUNITY.description = "The wand holder is immune to certain damage types.";
		DAMAGE_RESISTANCE.description = "The wand holder takes reduced damage from certain damage types.";
		DISARM.description = "When the wand holder takes damage, the attacker has a chance to be forced to drop their held item.";
		POTION.description = "The wand holder has a potion effect while holding the wand.";
		FREEDOM_OF_MOVEMENT.description = "The wand holder cannot be given the slowness potion effect, and the \"Hold\" spell has no effect.";
		AGILITY.description = "Projectiles shot near the wand holder are less accurate.";
		ELUSIVE.description = "Spells that target the wand holder have a chance to wiff.";
	}
	
	public String getDescription() {
		return description;
	}

	// TODO: Update to use dynamic classpath system like spells
	public static PassiveEffect newObject(PassiveEffectType type, ConfigurationSection config, String directory) {
		switch (type) {
		case AGILITY:
			return new AgilityPassive(config, directory);
		case DAMAGE_IMMUNITY:
			return new DamageImmunityPassive(config, directory);
		case DAMAGE_RESISTANCE:
			return new DamageResistancePassive(config, directory);
		case DISARM:
			return new DisarmPassive(config, directory);
		case ELUSIVE:
			return new ElusivePassive(config, directory);
		case FREEDOM_OF_MOVEMENT:
			return new FreedomOfMovementPassive(config, directory);
		case POTION:
			return new PotionPassive(config, directory);
		default:
			return null;
		
		}
	}
	
	public static PassiveEffectType fromString(String from) {
		if (from == null) {
			return null;
		}
		for (String typeString : WbsEnums.toStringArray(PassiveEffectType.class)) {
			if (typeString.equalsIgnoreCase(from)) {
				return PassiveEffectType.valueOf(typeString.toUpperCase());
			}
		}
		return null;
	}
}
