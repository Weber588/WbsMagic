package wbs.magic.objects;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import wbs.magic.objects.generics.DynamicMagicObject;
import wbs.magic.spellinstances.SpellInstance;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.WbsEntities;
import wbs.utils.util.WbsSound;
import wbs.utils.util.particles.SpiralParticleEffect;

public class TornadoObject extends DynamicMagicObject {
	
	private final double duration;
	private final double radius;
	private final int amount;
	private final double force;

	public TornadoObject(Location location, SpellCaster caster, SpellInstance castingSpell, double duration, double radius, int amount, double force) {
		super(location, caster, castingSpell);
		this.duration = duration;
		this.radius = radius;
		this.amount = amount;
		this.force = force;
	}

	public void start() {
		double variation = 0.35 * radius;
		
		SpiralParticleEffect effect = new SpiralParticleEffect();

		effect.setRadius(radius);
		effect.setSpeed(force);
		effect.setAmount(amount);
		effect.setVariation(variation);
		effect.build();
		
		SpiralParticleEffect secondary = new SpiralParticleEffect();

		int secondaryAmount = amount/2;
		
		secondary.setRadius(radius/2);
		secondary.setSpeed(force*1.25);
		secondary.setAmount(secondaryAmount);
		secondary.setVariation(variation);
		secondary.setClockwise(false);
		secondary.build();
		
		
		new BukkitRunnable() {

			final Sound baseSound = Sound.ITEM_ELYTRA_FLYING;
			final WbsSound sound = new WbsSound(baseSound);
			
			final Location hitBoxCentre = location.clone().add(0, radius, 0);

			final Particle particle = Particle.CLOUD;

			final double maxForce = force/4;
			final Vector throwVector = new Vector(0, maxForce, 0);
			final Vector mobThrowVector = new Vector(0, maxForce*5, 0);
			
			double rotation = 0;
			int ticks = 0;
			double localRadius = 0;
			double localSpeed = force;
			double localVariation = variation;
			
			Vector offset; // A small force that pushes the player away from the centre to avoid bouncing
			
			@Override
			public void run() {
				rotation -= localSpeed*15;
				
				if (ticks < 41) {
					localRadius = radius * (21 - (ticks / 2));
					effect.setRadius(localRadius);
					secondary.setRadius(localRadius/2);
				} else {
					Set<LivingEntity> hits = WbsEntities.getNearbyLiving(hitBoxCentre, radius, new HashSet<>());
					for (LivingEntity hit : hits) {
						Location locDifference = hit.getLocation().subtract(hitBoxCentre);
						if (locDifference.getX() != 0 || locDifference.getZ() != 0) {
							offset = locDifference.toVector().setY(0);
							offset = scaleVector(offset, localSpeed/20);
						} else {
							offset = throwVector;
						}
						
						if (hit instanceof Player) { // Players seem to be affected more than mobs; possibly due to lag?
							hit.setVelocity(hit.getVelocity().add(throwVector).add(offset));
						} else {
							hit.setVelocity(hit.getVelocity().add(mobThrowVector).add(offset));
						}
						hit.setFallDistance(0);
					}
				}
				
				effect.setRotation(rotation);
				effect.buildAndPlay(particle, location);
				
				secondary.setRotation(-rotation);
				secondary.buildAndPlay(particle, location);

				world.spawnParticle(particle, location, 10, radius, radius/2, radius, 0);
				world.spawnParticle(particle, location, 1, 0, 0, 0, localSpeed);
				
				if (ticks % 100 == 0) {
					sound.play(location);
				}
				
				ticks++;
				
				if (ticks > duration*20) {
					remove(true);
				} else if (duration*20 - ticks < 20) {
					if (chance(25)) {
						localSpeed = localSpeed * 0.5;
						effect.setSpeed(localSpeed);
						secondary.setSpeed(localSpeed);
						
						localVariation = localVariation * 1.3;
						effect.setVariation(localVariation);
						secondary.setVariation(localVariation);
					}
				}
				
				if (castingSpell.isConcentration() && !caster.isConcentratingOn(castingSpell)) {
					caster.concentrationBroken();
					remove(true);
				}
				
				if (!active) {
					cancel();
					if (castingSpell.isConcentration()) {
						caster.stopConcentration();
					}
					for (LivingEntity nearby : WbsEntities.getNearbyLiving(location, 50, new HashSet<>())) {
						if (nearby instanceof Player) {
							((Player) nearby).stopSound(baseSound);
						}
					}
				}
			}
		}.runTaskTimer(plugin, 0L, 1L);
	}

	@Override
	protected boolean tick() {
		// TODO Auto-generated method stub
		return false;
	}
}
