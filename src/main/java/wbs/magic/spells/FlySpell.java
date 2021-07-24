package wbs.magic.spells;

import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.RestrictWandControls;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellSettings;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.utils.util.WbsRunnable;
import wbs.magic.SpellCaster;

@Spell(name = "Fly",
		cost = 10,
		cooldown = 15,
		description = "Fly in the direction you're looking for several seconds"
)
@SpellSettings(canBeConcentration = true)
@RestrictWandControls(dontRestrictLineOfSight = true)
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 1)
@SpellOption(optionName = "duration", type = SpellOptionType.INT, defaultDouble = 3)
public class FlySpell extends SpellInstance {

	public FlySpell(SpellConfig config, String directory) {
		super(config, directory);
		
		speed = config.getDouble("speed");
		durationInTicks = config.getInt("duration") * 20;
	}
	
	private final double speed;
	private final int durationInTicks;

	@Override
	public boolean cast(SpellCaster caster) {
		
		WbsRunnable runnable = new WbsRunnable() {
			int age = 0;
			@Override
			public void run() {
				age++;
				if (age > durationInTicks) {
					cancel();
					return;
				}
				caster.push(caster.getFacingVector(speed));
				caster.getPlayer().setFallDistance(0);
			}
			
			@Override
			public void finish() {
				caster.stopConcentration();
			}
		};

		if (isConcentration) {
			caster.setConcentration(this);
		}
		
		runnable.runTaskTimer(plugin, 0, 1);
		return false;
	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDuration: &7" + durationInTicks / 20 + " seconds";
		asString += "\n&rSpeed: &7" + speed;

		return asString;
	}
}
