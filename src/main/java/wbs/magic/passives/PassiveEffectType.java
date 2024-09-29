package wbs.magic.passives;

import org.bukkit.configuration.ConfigurationSection;

import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEnums;

import java.util.function.BiFunction;

public enum PassiveEffectType {
	DAMAGE_IMMUNITY(DamageImmunityPassive::new),
	DAMAGE_RESISTANCE(DamageResistancePassive::new),
	POTION(PotionPassive::new),
	PARTICLES(ParticlePassive::new),
	;
	
	private String description;
	private BiFunction<ConfigurationSection, String, PassiveEffect> constructor;
	
	static {
		DAMAGE_IMMUNITY.description = "The wand holder is immune to certain damage types.";
		DAMAGE_RESISTANCE.description = "The wand holder takes reduced damage from certain damage types.";
		POTION.description = "The wand holder has a potion effect while holding the wand.";
	}

	PassiveEffectType(BiFunction<ConfigurationSection, String, PassiveEffect> constructor) {
		this.constructor = constructor;
	}
	
	public String getDescription() {
		return description;
	}

	public static PassiveEffect newObject(@NotNull PassiveEffectType type, @NotNull ConfigurationSection config, @NotNull String directory) {
		return type.constructor.apply(config, directory);
	}
}
