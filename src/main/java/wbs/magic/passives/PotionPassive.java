package wbs.magic.passives;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;

import wbs.utils.util.string.WbsStrings;

public class PotionPassive extends PassiveEffect {

	private final Map<PotionEffectType, Integer> potionEffects = new HashMap<>();
	
	public PotionPassive(ConfigurationSection config, String directory) {
		super(PassiveEffectType.POTION, config, directory);

		// PotionEffectType isn't an enum for some reason, so WbsEnums won't be used here.
		
		for (String keyName : config.getKeys(false)) {
			if (PotionEffectType.getByName(keyName) != null) {
				PotionEffectType potionType = PotionEffectType.getByName(keyName);
				
				int level = config.getInt(keyName, 1);
				level = Math.max(1, level);
				level = Math.min(256, level);
				
				level--;
				potionEffects.put(potionType, level);
			} else {
				logError("Invalid potion type: " + keyName, directory);
			}
		}
	}

	public Map<PotionEffectType, Integer> getPotions() {
		return potionEffects;
	}

	@Override
	public boolean isEnabled() {
		return !potionEffects.isEmpty();
	}

	@Override
	public String toString() {
		StringBuilder asString = new StringBuilder(super.toString());
		
		for (PotionEffectType type : potionEffects.keySet()) {
			int level = potionEffects.get(type);
			asString.append("\n&r")
					.append(WbsStrings.capitalizeAll(type.getName().replace('_', ' ')))
					.append(": &7")
					.append(level + 1);
		}
		
		return asString.toString();
	}
}
