package wbs.magic.passives;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import wbs.utils.util.WbsEnums;

public class DamageResistancePassive extends PassiveEffect {

	private Map<DamageCause, Double> resistances = new HashMap<>();
	
	public DamageResistancePassive(ConfigurationSection config, String directory) {
		super(PassiveEffectType.DAMAGE_RESISTANCE, config, directory);
		

		for (String keyName : config.getKeys(false)) {
			try {
				DamageCause cause = WbsEnums.getEnumFromString(DamageCause.class, keyName);

				double resistanceAmount = config.getDouble(cause.name(), 0);
				resistanceAmount = Math.max(0, resistanceAmount);
				resistanceAmount = Math.min(100, resistanceAmount);

				resistances.put(cause, resistanceAmount);

			} catch (IllegalArgumentException ex) {
				logError("Invalid damage cause: " + keyName, directory);
			}
		}
	}
	
	public Map<DamageCause, Double> getResistances() {
		return resistances;
	}

	@Override
	public boolean isEnabled() {
		return !resistances.isEmpty();
	}
	
	@Override
	public String toString() {
		StringBuilder asString = new StringBuilder(super.toString());
		
		for (DamageCause cause : resistances.keySet()) {
			asString.append("\n&r")
					.append(cause)
					.append(": &7")
					.append(resistances.get(cause))
					.append("%");
		}
		
		return asString.toString();
	}
}
