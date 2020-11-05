package wbs.magic.passives;

import org.bukkit.configuration.ConfigurationSection;

public class ElusivePassive extends PassiveEffect {

	private double chance = 0;
	
	public ElusivePassive(ConfigurationSection config, String directory) {
		super(PassiveEffectType.ELUSIVE, config, directory);

		chance = config.getDouble("chance", chance);
	}

	public double getChance() {
		return chance;
	}

	@Override
	public boolean isEnabled() {
		return chance != 0;
	}
	
	@Override
	public String toString() {
		String asString = super.toString();
		
		asString += " &h" + chance + "%";
		
		return asString;
	}
}
