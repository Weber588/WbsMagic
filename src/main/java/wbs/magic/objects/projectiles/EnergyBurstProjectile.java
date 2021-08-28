package wbs.magic.objects.projectiles;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import wbs.magic.SpellCaster;
import wbs.magic.objects.generics.DamagingProjectileObject;
import wbs.magic.spells.ranged.projectile.ProjectileSpell;
import wbs.magic.targeters.RadiusTargeter;
import wbs.utils.util.particles.SpiralParticleEffect;

import java.util.Collection;

public class EnergyBurstProjectile extends DamagingProjectileObject {

	public EnergyBurstProjectile(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
		super(location, caster, castingSpell);
	}

	private Vector throwVector = new Vector(0, 1.5, 0);
	
	private final Particle mainParticle = Particle.VILLAGER_HAPPY;

	private SpiralParticleEffect spiralEffect; // The spiral effect

	private RadiusTargeter radiusTargeter;
	
	@Override
	public boolean tick() {
		boolean cancel = super.tick();

		// in degrees
		double rotationChange = 10;
		spiralEffect.setRotation(step * rotationChange);

		spiralEffect.buildAndPlay(mainParticle, location);
		
		if (cancel) {
			Collection<LivingEntity> hits = radiusTargeter.getTargets(caster, hitLocation);
			for (LivingEntity hit : hits) {
				hit.setVelocity(throwVector);
				double damage = 3;
				caster.damage(hit, damage, castingSpell);
				hit.setVelocity(throwVector);
			}
		}
		return cancel;
	}
	
	public void setSpiralEffect(SpiralParticleEffect effect) {
		this.spiralEffect = effect;
	}

	@Override
	public EnergyBurstProjectile setFireDirection(Vector fireDirection) {
		this.fireDirection = fireDirection;
		spiralEffect.setAbout(fireDirection);
		
		return this;
	}
	
	public void setForce(double force) {
		throwVector = new Vector(0, force, 0);
	}
	
	public void setRadius(double radius) {
		radiusTargeter = new RadiusTargeter(radius);
	}
}
