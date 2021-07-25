package wbs.magic.spells.ranged.projectile;

import org.bukkit.Sound;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.objects.projectiles.DepthSurgeProjectile;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.SpellCaster;
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
@SpellSound(sound = Sound.ITEM_TRIDENT_RIPTIDE_3, pitch = 2, volume = 1)
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

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDamage: &7" + damage;

		return asString;
	}
}
