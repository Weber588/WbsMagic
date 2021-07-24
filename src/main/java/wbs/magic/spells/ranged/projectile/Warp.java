package wbs.magic.spells.ranged.projectile;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.objects.projectiles.WarpProjectile;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.SpellCaster;

import wbs.utils.util.particles.RingParticleEffect;
import wbs.utils.util.WbsSound;
import wbs.utils.util.WbsSoundGroup;

@Spell(name = "Warp",
		cost = 100,
		cooldown = 30,
		description = "Teleport to a point you're looking at within range.")
@SpellSound(sound = Sound.BLOCK_BEACON_POWER_SELECT)
@FailableSpell("The spell will fail if the caster is not looking at a block, or the block is too far away")
@RestrictWandControls(dontRestrictLineOfSight = true)
// Override parent class defaults for these
@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 300)
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 50)
public class Warp extends ProjectileSpell {
	public Warp(SpellConfig config, String directory) {
		super(config, directory);
		
		effect.setRadius(0.5);
		effect.setAmount(3);
	}

	WbsSoundGroup hitSound = new WbsSoundGroup(
			new WbsSound(Sound.BLOCK_BEACON_POWER_SELECT, 1.5F, 1)
			);

	RingParticleEffect effect = new RingParticleEffect();
	
	
	@Override
	public boolean cast(SpellCaster caster) {
		Location targetPos = caster.getTargetPos(range);
		if (targetPos == null) {
			caster.sendActionBar("You need line of sight to a block!");
			return false;
		}

		WarpProjectile projectile = new WarpProjectile(caster.getEyeLocation(), caster, targetPos, this);
		
		projectile.setHitbox(0);
		projectile.setRange(range);
		projectile.setSpeed(speed);
		double stepSize = 0.25;
		projectile.setStepSize(stepSize);
		
		projectile.setHitSound(hitSound);

		Vector direction = caster.getFacingVector();
		effect.setAbout(direction);
		projectile.setEffect(effect);
		
		projectile.setFireDirection(caster.getFacingVector());
		projectile.run();
		return true;
	}
}
