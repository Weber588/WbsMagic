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
	protected boolean step(int step, int stepsThisTick) {
		boolean cancel = super.step(step, stepsThisTick);

		// in degrees
		double rotationChange = 10;
		spiralEffect.setRotation(stepsTaken() * rotationChange);

		spiralEffect.buildAndPlay(mainParticle, location);

		return cancel;
	}

	@Override
	protected void onRemove() {
		Collection<LivingEntity> hits = radiusTargeter.getTargets(caster, getLocation());
		for (LivingEntity hit : hits) {
			hit.setVelocity(throwVector);
			double damage = 3;
			caster.damage(hit, damage, castingSpell);
			hit.setVelocity(throwVector);
		}
	}

	public void setSpiralEffect(SpiralParticleEffect effect) {
		this.spiralEffect = effect;
		effect.setAbout(getVelocity());
	}

	@Override
	public EnergyBurstProjectile setDirection(Vector direction) {
		super.setDirection(direction);
		spiralEffect.setAbout(direction);
		
		return this;
	}
	
	public void setForce(double force) {
		throwVector = new Vector(0, force, 0);
	}
	
	public void setRadius(double radius) {
		radiusTargeter = new RadiusTargeter(radius);
	}
}
