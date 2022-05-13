package wbs.magic.passives;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEnums;

public class DamageImmunityPassive extends PassiveEffect {
	
	private final Map<DamageCause, Double> immunities = new HashMap<>();
	
	public DamageImmunityPassive(@NotNull ConfigurationSection config, @NotNull String directory) {
		super(PassiveEffectType.DAMAGE_IMMUNITY, config, directory);
		
		for (String keyName : config.getKeys(false)) {
			try {
				DamageCause cause = WbsEnums.getEnumFromString(DamageCause.class, keyName);

				double chance = config.getDouble(cause.name(), 0);
				chance = Math.max(0, chance);
				chance = Math.min(100, chance);

				immunities.put(cause, chance);

			} catch (IllegalArgumentException ex) {
				logError("Invalid damage cause: " + keyName, directory);
			}
		}
	}
	
	public Map<DamageCause, Double> getImmunities() {
		return immunities;
	}
	
	public boolean isEnabled() {
		return !immunities.isEmpty();
	}

	@Override
	public String toString() {
		StringBuilder asString = new StringBuilder(super.toString());

		for (DamageCause cause : immunities.keySet()) {
			asString.append("\n&r")
					.append(cause);
		}
		
		return asString.toString();
	}
}
