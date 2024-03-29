package wbs.magic.passives;

import org.bukkit.configuration.ConfigurationSection;

import org.jetbrains.annotations.NotNull;
import wbs.magic.MagicSettings;

import wbs.utils.util.WbsEnums;

public abstract class PassiveEffect {

	protected static void logError(String error, String directory) {
		MagicSettings settings = MagicSettings.getInstance();
		settings.logError(error, directory);
	}
	
	public PassiveEffectType type;
	
	public PassiveEffect(@NotNull PassiveEffectType type, @NotNull ConfigurationSection config, @NotNull String directory) {
		this.type = type;
	}
	
	public abstract boolean isEnabled();

	@Override
	public String toString() {
		return WbsEnums.toPrettyString(type) + ":";
	}
}
