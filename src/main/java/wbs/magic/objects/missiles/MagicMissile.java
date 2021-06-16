package wbs.magic.objects.missiles;

import org.bukkit.Location;
import org.bukkit.Particle;

import wbs.magic.objects.MagicFireObject;
import wbs.magic.objects.generics.MissileObject;
import wbs.magic.spellinstances.ranged.targeted.missile.MissileSpell;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.particles.NormalParticleEffect;

public class MagicMissile extends MissileObject {
	public MagicMissile(Location location, SpellCaster caster, MissileSpell castingSpell) {
		super(location, caster, castingSpell);
	}
	
	private double damage;

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
		effect.play(Particle.SPELL_WITCH, location);
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}
}
