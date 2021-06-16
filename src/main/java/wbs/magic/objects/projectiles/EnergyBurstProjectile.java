package wbs.magic.objects.projectiles;

import java.util.Collection;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import wbs.magic.objects.generics.DamagingProjectileObject;
import wbs.magic.objects.generics.ProjectileObject;
import wbs.magic.spellinstances.ranged.projectile.ProjectileSpell;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.WbsEntities;
import wbs.utils.util.WbsSoundGroup;
import wbs.utils.util.particles.SpiralParticleEffect;

public class EnergyBurstProjectile extends DamagingProjectileObject {

	public EnergyBurstProjectile(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
		super(location, caster, castingSpell);
	}
	private final double rotationChange = 10; // in degrees
	
	private double damage = 3;
	private double radius = 4;
	
	private double force = 1.5;
	private Vector throwVector = new Vector(0, force, 0);
	
	private final Particle mainParticle = Particle.VILLAGER_HAPPY;

	private SpiralParticleEffect spiralEffect; // The spiral effect

	private WbsSoundGroup hitSound;
	
	@Override
	public boolean tick() {
		boolean cancel = super.tick();
		
		spiralEffect.setRotation(step * rotationChange);

		spiralEffect.buildAndPlay(mainParticle, location);
		
		if (cancel) {
			hitSound.play(hitLocation);
			
			Collection<LivingEntity> hits = WbsEntities.getNearbyLiving(hitLocation, radius, new HashSet<>());
			for (LivingEntity hit : hits) {
				hit.setVelocity(throwVector);
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
		this.force = force;
		throwVector = new Vector(0, force, 0);
	}
	
	public void setSound(WbsSoundGroup hitSound) {
		this.hitSound = hitSound;
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}
}
