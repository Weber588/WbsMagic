package wbs.magic.passives;

import org.bukkit.configuration.ConfigurationSection;

import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEnums;

public enum PassiveEffectType {
	DAMAGE_IMMUNITY, DAMAGE_RESISTANCE, POTION;
	
	private String description;
	
	static {
		DAMAGE_IMMUNITY.description = "The wand holder is immune to certain damage types.";
		DAMAGE_RESISTANCE.description = "The wand holder takes reduced damage from certain damage types.";
		POTION.description = "The wand holder has a potion effect while holding the wand.";
	}
	
	public String getDescription() {
		return description;
	}

	public static PassiveEffect newObject(@NotNull PassiveEffectType type, @NotNull ConfigurationSection config, @NotNull String directory) {
		switch (type) {
		case DAMAGE_IMMUNITY:
			return new DamageImmunityPassive(config, directory);
		case DAMAGE_RESISTANCE:
			return new DamageResistancePassive(config, directory);
		case POTION:
			return new PotionPassive(config, directory);
		default:
			return null;
		}
	}
}
