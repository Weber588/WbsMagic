package wbs.magic.spells.ranged.targeted;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.SpellCaster;

import wbs.utils.util.particles.NormalParticleEffect;

@Spell(name = "Push",
		cost = 15,
		cooldown = 5,
		description = "The targeted creature is thrown away from the caster at high speed."
)
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 1.25)
@SpellOption(optionName = "relative", type = SpellOptionType.BOOLEAN, defaultBool = true)
// Overrides
@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 5)
@SpellOption(optionName = "targeter", type = SpellOptionType.STRING, defaultString = "RADIUS")
public class Push extends TargetedSpell {
	
	private final double speed;
	private final boolean relative; // When true, speed = speed / (distance^2)
	
	private final NormalParticleEffect effect = new NormalParticleEffect();
	private final Particle mainParticle = Particle.SPELL_INSTANT;
	
	private final Vector upVector = new Vector(0, 0.5, 0);

	public Push(SpellConfig config, String directory) {
		super(config, directory);

		speed = config.getDouble("speed");

		relative = config.getBoolean("relative");
		
		effect.setAmount(50);
		effect.setXYZ(0);
		effect.setSpeed(5);
	}

	@Override
	public <T extends LivingEntity> boolean preCast(SpellCaster caster, Set<T> targets) {
		Player player = caster.getPlayer();
		Location location = player.getLocation();

		Vector casterToTarget;

		effect.play(mainParticle, location);

		for (LivingEntity target : targets) {
			Location targetLoc = target.getLocation();
			casterToTarget = targetLoc.clone().subtract(location).toVector();
			casterToTarget.setY(0);
			Vector toAdd;
			if (relative) {
				double distanceSquared = targetLoc.distanceSquared(location);
				if (distanceSquared == 0) {
					distanceSquared = 0.05;
				}
				toAdd = scaleVector(casterToTarget, speed / distanceSquared);
			} else {
				toAdd = scaleVector(casterToTarget, speed);
			}
			toAdd.add(upVector);

			target.setVelocity(target.getVelocity().add(toAdd));
		}

		return true;
	}

	@Override
	protected <T extends LivingEntity> void castOn(SpellCaster caster, T target) {

	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rSpeed: &7" + speed;
		asString += "\n&rRelative: &7" + relative;

		return asString;
	}
}
