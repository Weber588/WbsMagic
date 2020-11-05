package wbs.magic.passives;

import org.bukkit.configuration.ConfigurationSection;

public class FreedomOfMovementPassive extends PassiveEffect {
	private boolean enabled = false;

	public FreedomOfMovementPassive(ConfigurationSection config, String directory) {
		super(PassiveEffectType.FREEDOM_OF_MOVEMENT, config, directory);

		this.enabled = config.getBoolean("enabled");
	}
	
	public boolean isEnabled() {
		return enabled;
	}
}
