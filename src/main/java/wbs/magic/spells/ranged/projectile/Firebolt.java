package wbs.magic.spells.ranged.projectile;

import org.bukkit.Particle;

import org.bukkit.Sound;
import wbs.magic.DamageType;
import wbs.magic.objects.AlignmentType;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.objects.projectiles.FireboltProjectile;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.SpellCaster;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

@Spell(name = "Firebolt",
		cost = 50,
		description = "The caster shoots a beam of flames, damaging the first creatures it hits, and leaving them on fire.")
@SpellSound(sound = "entity.blaze.shoot", pitch = 0.5F)
@DamageSpell(deathFormat = "%victim% was scorched by %attacker%!",
		defaultDamage = 6,
		damageTypes = {DamageType.Name.FIRE}
)
@FailableSpell("Firebolt will fail if it used under water, or it comes into contact with water before finding a target.")
@RestrictWandControls(dontRestrictLineOfSight = true)
@SpellOption(optionName = "size", type = SpellOptionType.DOUBLE, defaultDouble = 0.3)
// Override parent class defaults for these
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 80)
@SpellOption(optionName = "hitbox-size", type = SpellOptionType.DOUBLE, defaultDouble = 0.8)
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.BAD, enumType = AlignmentType.class)
public class Firebolt extends ProjectileSpell {
	public Firebolt(SpellConfig config, String directory) {
		super(config, directory);
		
		size = config.getDouble("size");
		damage = config.getDouble("damage");

		int particleAmount = (int) (size * 25);
		NormalParticleEffect effect = new NormalParticleEffect();
		effect.setAmount(particleAmount);
		effect.setXYZ(size);

		Particle particle = Particle.FLAME;
		effects.addEffect(effect, particle);
	}

	private final double size;
	private final double damage;

	private final WbsParticleGroup effects = new WbsParticleGroup();

	public boolean cast(CastingContext context) {
		SpellCaster caster = context.caster;
		FireboltProjectile projectile = new FireboltProjectile(caster.getEyeLocation(), caster, this);

		projectile.setDamage(damage);
		
		projectile.setSize(size);
		projectile.setParticle(effects);

		projectile.run();
		return true;
	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDamage: &7" + damage;
		asString += "\n&rSize: &7" + size;

		return asString;
	}
}
