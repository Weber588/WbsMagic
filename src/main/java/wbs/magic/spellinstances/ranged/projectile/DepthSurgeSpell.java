package wbs.magic.spellinstances.ranged.projectile;

import wbs.magic.enums.SpellOptionType;
import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.*;
import wbs.magic.objects.projectiles.DepthSurgeProjectile;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.particles.SpiralParticleEffect;

import wbs.utils.util.WbsEntities;

@Spell(name = "Depth Surge",
		cost = 25,
		description =
				"Fire a beam of heated water that deals damage to all entities it passes!"
)
@RestrictWandControls(dontRestrictLineOfSight = true)
@FailableSpell("This spell may only be cast under water")
@DamageSpell(deathFormat = "%victim% was pulled into the watery depths by %attacker%!",
		defaultDamage = 6)
// Override parent class defaults for these
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 80)
public class DepthSurgeSpell extends ProjectileSpell {
	public DepthSurgeSpell(SpellConfig config, String directory) {
		super(config, directory);

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

		projectile.setSpiralEffect(effect)
				.setDamage(damage)
				.configure(this)
				.setFireDirection(caster.getFacingVector())
				.run();
		return true;
	}
}
