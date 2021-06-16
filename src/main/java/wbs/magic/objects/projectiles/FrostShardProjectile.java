package wbs.magic.objects.projectiles;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.data.BlockData;

import wbs.magic.objects.generics.DamagingProjectileObject;
import wbs.magic.objects.generics.ProjectileObject;
import wbs.magic.spellinstances.SpellInstance;
import wbs.magic.wrappers.SpellCaster;

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

	public FrostShardProjectile(Location location, SpellCaster caster, SpellInstance castingSpell) {
		super(location, caster, castingSpell);
	}
	
	@Override
	public boolean tick() {
		boolean cancel = super.tick();
		if (step > 5) {
			effects.play(location);
		}

		return cancel;
	}

	@Override
	public boolean hitEntity() {
		int maxNoDamageTicks = hitEntity.getMaximumNoDamageTicks();
		hitEntity.setMaximumNoDamageTicks(0);
		caster.damage(hitEntity, damage, castingSpell);
		hitEntity.setMaximumNoDamageTicks(maxNoDamageTicks);
		return false;
	}
}
