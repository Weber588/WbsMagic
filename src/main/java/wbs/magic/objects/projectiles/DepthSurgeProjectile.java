package wbs.magic.objects.projectiles;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import wbs.magic.objects.generics.DamagingProjectileObject;
import wbs.magic.spells.ranged.projectile.ProjectileSpell;
import wbs.magic.SpellCaster;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.WbsMath;
import wbs.utils.util.WbsSound;
import wbs.utils.util.WbsSoundGroup;
import wbs.utils.util.particles.SpiralParticleEffect;

public class DepthSurgeProjectile extends DamagingProjectileObject {
	public DepthSurgeProjectile(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
		super(location, caster, castingSpell);

		hitSound.addSound(new WbsSound(Sound.ITEM_TRIDENT_RIPTIDE_3, 2, 0.5f));
	}

	private final Particle particle = Particle.WATER_BUBBLE;
	private SpiralParticleEffect spiralEffect;

	private double rotation = 0;

	@Override
	protected boolean step(int step, int stepsThisTick) {
		boolean cancel = super.step(step, stepsThisTick);
		spiralEffect.setRotation(rotation);
		rotation += 15;
		spiralEffect.buildAndPlay(particle, location);
		return location.getBlock().getType() != Material.WATER || cancel;
	}

	@Override
	protected boolean hitEntity(Location hitLocation, LivingEntity hitEntity) {
		if (hitEntity.getNoDamageTicks() < 5) {
			caster.damage(hitEntity, damage, castingSpell);
			Vector pushVec = WbsMath.scaleVector(getVelocity(), 0.5);
			pushVec.add(WbsMath.randomVector(0.5));
			WbsEntities.push(hitEntity, pushVec);
		}

		return false;
	}
	
	public DepthSurgeProjectile setSpiralEffect(SpiralParticleEffect effect) {
		this.spiralEffect = effect.clone();
		return this;
	}

	@Override
	public DepthSurgeProjectile setDirection(Vector direction) {
		super.setVelocity(direction);
		spiralEffect.setAbout(direction);
		
		return this;
	}
}
