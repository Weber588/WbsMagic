package wbs.magic.objects.missiles;

import org.bukkit.Location;
import org.bukkit.Particle;

import wbs.magic.objects.MagicFireObject;
import wbs.magic.objects.generics.MissileObject;
import wbs.magic.spells.ranged.targeted.missile.MissileSpell;
import wbs.magic.SpellCaster;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

public class MagicMissile extends MissileObject {
	public MagicMissile(Location location, SpellCaster caster, MissileSpell castingSpell) {
		super(location, caster, castingSpell);

		double size = 0.1;
		int particleAmount = (int) (size * 25);
		NormalParticleEffect effect = new NormalParticleEffect();
		effect.setAmount(particleAmount);
		effect.setXYZ(size);
		NormalParticleEffect coreEffect = new NormalParticleEffect();
		coreEffect.setAmount(particleAmount /3);
		coreEffect.setXYZ(size /2);

		Particle coreParticle = Particle.SMOKE;
		Particle particle = Particle.WITCH;
		effects.addEffect(coreEffect, coreParticle)
				.addEffect(effect, particle);
	}
	
	private double damage;

	private final WbsParticleGroup effects = new WbsParticleGroup();

	@Override
	public boolean tick() {
		effects.play(location);
		return false;
	}
	
	@Override
	public void hit() {
		caster.damage(getTarget(), damage, castingSpell);
	}

	@Override
	public void hitBlock() {
		remove(true);
		MagicFireObject magicFire = new MagicFireObject(location, caster, castingSpell);
		magicFire.setDamage(damage/5);
		magicFire.setDuration(100);
		magicFire.run();
	}

	@Override
	public void targetDead() {
		
	}

	@Override
	public void timedOut() {
		fizzleEffect();
	}
	
	private void fizzleEffect() {
		NormalParticleEffect effect = new NormalParticleEffect();
		effect.setAmount(2);
		effect.setSpeed(1);
		effect.setXYZ(0.05);
		effect.play(Particle.WITCH, location);
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}
}
