package wbs.magic.spellinstances.ranged.projectile;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.*;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.enums.SpellType;
import wbs.magic.objects.projectiles.WarpProjectile;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.particles.RingParticleEffect;
import wbs.utils.util.WbsSound;
import wbs.utils.util.WbsSoundGroup;

@Spell(name = "Warp",
		cost = 100,
		cooldown = 30,
		description = "Teleport to a point you're looking at within range.")
@FailableSpell("The spell will fail if the caster is not looking at a block, or the block is too far away")
@RestrictWandControls(dontRestrictLineOfSight = true)
@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 300)
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 50)
public class Warp extends ProjectileSpell {

	protected final static double DEFAULT_SPEED = 50;
	protected final static double DEFAULT_RANGE = 300;
	
	public Warp(SpellConfig config, String directory) {
		super(config, directory, DEFAULT_RANGE, DEFAULT_SPEED);
		
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

	@Override
	public SpellType getType() {
		return SpellType.WARP;
	}
}
