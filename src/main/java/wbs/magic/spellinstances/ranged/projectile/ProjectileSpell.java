package wbs.magic.spellinstances.ranged.projectile;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.spellinstances.ranged.RangedSpell;

@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 40)
@SpellOption(optionName = "gravity", type = SpellOptionType.DOUBLE, defaultDouble = 0)
public abstract class ProjectileSpell extends RangedSpell {
	protected double speed; // in blocks per second
	protected double gravity; // in blocks per second
	protected double stepSize = 0.2; // in blocks per second
	protected double hitbox = 0.8;
	
	public ProjectileSpell(SpellConfig config, String directory) {
		super(config, directory);
		speed = config.getDouble("speed");
		gravity = config.getDouble("gravity");
	}

	@Override
	public String toString() {
		String asString = super.toString();
		
		asString += "\n&rSpeed: &7" + speed;
		
		return asString;
	}

	public double getSpeed() {
		return speed;
	}
	public double getStepSize() {
		return stepSize;
	}
	public double getGravity() {
		return gravity;
	}
	public double getHitbox() {
		return hitbox;
	}
}
