package wbs.magic.spellinstances.ranged.targeted;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.enums.SpellType;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.RadiusTargeter;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.particles.NormalParticleEffect;

@Spell(name = "Push",
		cost = 15,
		cooldown = 5,
		description = "The targeted creature is thrown away from the caster at high speed."
)
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 1.25)
// TODO: Rename these, they're super confusing
@SpellOption(optionName = "proportional", type = SpellOptionType.BOOLEAN, defaultBool = false)
@SpellOption(optionName = "relative", type = SpellOptionType.BOOLEAN, defaultBool = true)
public class Push extends TargetedSpell {
	
	private double speed = 1.25;
	private boolean proportional  = false; // When true, speed = speed / (distance^2)
	
	private NormalParticleEffect effect = new NormalParticleEffect();
	private Particle mainParticle = Particle.SPELL_INSTANT;
	
	private Vector upVector = new Vector(0, 0.5, 0);
	
	private static final GenericTargeter DEFAULT_TARGETER = new RadiusTargeter();
	
	public Push(SpellConfig config, String directory) {
		super(config, directory, DEFAULT_TARGETER);

		speed = config.getDouble("speed", speed);
		
		proportional = config.getBoolean("proportional", proportional);
		proportional = !config.getBoolean("relative", proportional);
		
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
			if (proportional) {
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
	public void castOn(SpellCaster caster, LivingEntity target) {
		
	}

	@Override
	public SpellType getType() {
		return SpellType.PUSH;
	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rSpeed: &7" + speed;
		
		return asString;
	}
}
