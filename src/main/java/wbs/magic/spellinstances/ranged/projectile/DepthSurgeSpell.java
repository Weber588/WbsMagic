package wbs.magic.spellinstances.ranged.projectile;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.*;
import wbs.magic.objects.projectiles.DepthSurgeProjectile;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.particles.SpiralParticleEffect;

import wbs.utils.util.WbsEntities;

@Spell(name = "Depth Surge",
		cost = 25,
		cooldown = 10,
		description =
				"Fire a beam of heated water that deals damage to all entities it passes!"
)
@RestrictWandControls(dontRestrictLineOfSight = true)
@FailableSpell("This spell may only be cast under water")
@DamageSpell(deathFormat = "%victim% was pulled into the watery depths by %attacker%!",
		defaultDamage = 6)
public class DepthSurgeSpell extends ProjectileSpell {

	protected final static double DEFAULT_SPEED = 80;

	public DepthSurgeSpell(SpellConfig config, String directory) {
		super(config, directory, DEFAULT_SPEED);

		damage = config.getDouble("damage", damage);
		
		effect.setRadius(0.3)
				.setSpeed(3)
				.setVariation(0.2)
				.setAmount(1);
	}
	
	private double damage = 6;
	
	private final SpiralParticleEffect effect = new SpiralParticleEffect();

	@Override
	public boolean cast(SpellCaster caster) {
		if (!WbsEntities.isInWater(caster.getPlayer())) {
			caster.sendActionBar("You are not in water!");
			return false;
		}
		
		DepthSurgeProjectile projectile = new DepthSurgeProjectile(caster.getEyeLocation(), caster, this);

		projectile.setDamage(damage)
					.setSpiralEffect(effect)
					.configure(this)
					.setFireDirection(caster.getFacingVector())
					.run();
		return true;
	}
}
