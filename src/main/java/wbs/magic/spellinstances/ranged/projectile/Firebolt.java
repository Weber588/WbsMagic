package wbs.magic.spellinstances.ranged.projectile;

import org.bukkit.Particle;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.*;
import wbs.magic.enums.SpellType;
import wbs.magic.objects.projectiles.FireboltProjectile;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

@Spell(name = "Firebolt",
		cost = 50,
		cooldown = 10,
		description = "The caster shoots a beam of flames, damaging the first creatures it hits, and leaving them on fire.")
@DamageSpell(deathFormat = "%victim% was scorched by %attacker%!",
		defaultDamage = 6
)
@FailableSpell("Firebolt will fail if it used under water, or it comes into contact with water before finding a target.")
@RestrictWandControls(dontRestrictLineOfSight = true)
public class Firebolt extends ProjectileSpell {
	protected final static double DEFAULT_SPEED = 80;
	
	public Firebolt(SpellConfig config, String directory) {
		super(config, directory, DEFAULT_SPEED);
		
		size = config.getDouble("size", size);
		damage = config.getDouble("damage", damage);
		
		stepSize = 0.33;
		hitbox = 0.8;

		int particleAmount = (int) (size * 25);
		NormalParticleEffect effect = new NormalParticleEffect();
		effect.setAmount(particleAmount);
		effect.setXYZ(size);

		Particle particle = Particle.FLAME;
		effects.addEffect(effect, particle);
	}

	private double size = 0.1;
	private double damage = 7;

	private final WbsParticleGroup effects = new WbsParticleGroup();
	
	public boolean cast(SpellCaster caster) {
		FireboltProjectile projectile = new FireboltProjectile(caster.getEyeLocation(), caster, this);

		projectile.configure(this);
		
		projectile.setDamage(damage);
		
		projectile.setSize(size);
		projectile.setParticle(effects);
		
		projectile.setFireDirection(caster.getFacingVector());
		projectile.run();
		return true;
	}
	
	@Override
	public SpellType getType() {
		return SpellType.FIREBOLT;
	}
	
	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDamage: &7" + damage;
		
		return asString;
	}
}
