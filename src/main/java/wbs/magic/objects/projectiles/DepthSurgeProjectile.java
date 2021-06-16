package wbs.magic.objects.projectiles;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;
import wbs.magic.objects.generics.DamagingProjectileObject;
import wbs.magic.objects.generics.ProjectileObject;
import wbs.magic.spellinstances.ranged.projectile.ProjectileSpell;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.WbsMath;
import wbs.utils.util.particles.SpiralParticleEffect;

public class DepthSurgeProjectile extends DamagingProjectileObject {

	public DepthSurgeProjectile(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
		super(location, caster, castingSpell);
	}

	private final Particle particle = Particle.WATER_BUBBLE;
	private SpiralParticleEffect spiralEffect;

	private double rotation = 0;
	
	@Override
	protected boolean tick() {
		boolean cancel = super.tick();
		spiralEffect.setRotation(rotation);
		rotation += 15;
		spiralEffect.buildAndPlay(particle, location);
		return location.getBlock().getType() != Material.WATER || cancel;
	}
	
	@Override
	public boolean hitEntity() {
		if (hitEntity.getNoDamageTicks() < 5) {
			caster.damage(hitEntity, damage, castingSpell);
			Vector pushVec = WbsMath.scaleVector(fireDirection, 0.5);
			pushVec.add(WbsMath.randomVector(0.5));
			WbsEntities.push(hitEntity, pushVec);
		}
		return false;
	}
	
	@Override
	public boolean hitBlock() {
		
		return true;
	}
	
	public DepthSurgeProjectile setSpiralEffect(SpiralParticleEffect effect) {
		this.spiralEffect = effect.clone();
		return this;
	}

	@Override
	public DepthSurgeProjectile setFireDirection(Vector fireDirection) {
		this.fireDirection = fireDirection;
		spiralEffect.setAbout(fireDirection);
		
		return this;
	}
}
