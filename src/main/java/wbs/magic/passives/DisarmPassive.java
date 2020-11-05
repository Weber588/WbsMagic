package wbs.magic.passives;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DisarmPassive extends PassiveEffect {

	private double chance = 0;
	private boolean canDisarmPlayers = true;
	private boolean enabled = false;
	
	public DisarmPassive(ConfigurationSection config, String directory) {
		super(PassiveEffectType.DISARM, config, directory);

		chance = config.getDouble("chance", chance);
		canDisarmPlayers = config.getBoolean("can-disarm-players", canDisarmPlayers);
		enabled = config.getBoolean("enabled");
	}

	public double getChance() {
		return chance;
	}
	
	public boolean canDisarmPlayers() {
		return canDisarmPlayers;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	@Override
	public String toString() {
		String asString = super.toString();
		
		asString += " &h" + chance + "%";

		asString += "\n&rCan disarm players: " + canDisarmPlayers;
		
		return asString;
	}
}
