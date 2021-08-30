package wbs.magic.objects.projectiles;

import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.data.BlockData;

import org.bukkit.entity.LivingEntity;
import wbs.magic.objects.generics.DamagingProjectileObject;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;

import wbs.magic.spells.ranged.projectile.ProjectileSpell;
import wbs.utils.util.WbsSound;
import wbs.utils.util.WbsSoundGroup;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

public class FrostShardProjectile extends DamagingProjectileObject {
	
	private static final WbsParticleGroup effects = new WbsParticleGroup();
	
	static {
		Particle particle = Particle.REDSTONE;
		Particle snowParticle = Particle.FALLING_DUST;
		Particle iceParticle = Particle.BLOCK_DUST;

		DustOptions data = new DustOptions(Color.fromRGB(100, 130, 150), 0.8F);
		BlockData iceData = Bukkit.createBlockData(Material.ICE);
		BlockData snowData = Bukkit.createBlockData(Material.SNOW);
		
		NormalParticleEffect effect = new NormalParticleEffect();
		NormalParticleEffect coreEffect = new NormalParticleEffect();
		NormalParticleEffect coreEffect2 = new NormalParticleEffect();
		
		effect.setXYZ(0.075)
				.setOptions(data)
				.setAmount(1);
		
		coreEffect.setXYZ(0.4)
					.setOptions(snowData)
					.setAmount(1);
		
		coreEffect2.setXYZ(0.075)
					.setOptions(iceData)
					.setAmount(1);
		
		effects.addEffect(effect, particle)
				.addEffect(coreEffect, snowParticle, 10)
				.addEffect(coreEffect2, iceParticle, 90);
	}

	/*.*************************** END OF STATIC ***************************.*/

	public FrostShardProjectile(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
		super(location, caster, castingSpell);

		hitSound.addSound(new WbsSound(Sound.BLOCK_GLASS_BREAK, 2, 1));
	}

	@Override
	protected boolean step(int step, int stepsThisTick) {
		boolean cancel = super.step(step, stepsThisTick);

		if (getAge() * getStepsPerTick() > 5) {
			effects.play(location);
		}

		return cancel;
	}

	@Override
	protected boolean hitEntity(Location hitLocation, LivingEntity hitEntity) {
		int maxNoDamageTicks = hitEntity.getMaximumNoDamageTicks();
		hitEntity.setMaximumNoDamageTicks(0);
		caster.damage(hitEntity, damage, castingSpell);
		hitEntity.setMaximumNoDamageTicks(maxNoDamageTicks);
		return false;
	}
}
