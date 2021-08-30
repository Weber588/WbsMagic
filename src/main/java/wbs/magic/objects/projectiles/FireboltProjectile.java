package wbs.magic.objects.projectiles;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import wbs.magic.objects.generics.DamagingProjectileObject;
import wbs.magic.spells.ranged.projectile.ProjectileSpell;
import wbs.magic.SpellCaster;
import wbs.utils.util.WbsSound;

public class FireboltProjectile extends DamagingProjectileObject {

	public FireboltProjectile(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
		super(location, caster, castingSpell);

		hitSound.addSound(new WbsSound(Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1f));
	}

	private double size = 0.1;
	
	private final Particle finalParticle = Particle.LAVA;
	private final Particle steamParticle = Particle.CLOUD;

	@Override
	protected boolean step(int step, int stepsThisTick) {
		boolean cancel = super.step(step, stepsThisTick);

		if (getAge() * getStepsPerTick() > 5) {
			effects.play(location);
		}

		if (location.getBlock().getType() == Material.WATER) {
			cancel = true;
		}

		return cancel;
	}

	@Override
	protected void onRemove() {
		super.onRemove();

		Particle particleToPlay;
		if (location.getBlock().getType() == Material.WATER) {
			particleToPlay = steamParticle;
		} else {
			particleToPlay = finalParticle;
		}

		world.spawnParticle(particleToPlay, location, 25, size*3, size*4, size*3, 0.1);
	}

	@Override
	protected boolean hitEntity(Location hitLocation, LivingEntity hitEntity) {
		double initialHealth = hitEntity.getHealth();
		caster.damage(hitEntity, damage, castingSpell);
		if (hitEntity.getHealth() < initialHealth) {
			hitEntity.setFireTicks((int) damage*60);
		}
		return false;
	}

	public void setSize(double size) {
		this.size = size;
	}
}
