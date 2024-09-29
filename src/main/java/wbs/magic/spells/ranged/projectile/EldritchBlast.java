package wbs.magic.spells.ranged.projectile;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import wbs.magic.objects.AlignmentType;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.objects.projectiles.EldritchBlastProjectile;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.SpellCaster;
import wbs.magic.spellmanagement.configuration.options.EnumOptions;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.spells.framework.RawSpell;
import wbs.utils.util.WbsSound;
import wbs.utils.util.WbsSoundGroup;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

@Spell(name = "Eldritch Blast",
		cost = 15,
		cooldown = 1,
		description = "A simple damaging spell that fires a blast of energy in the direction the caster is facing.")
@SpellSound(sound = Sound.ENTITY_ILLUSIONER_CAST_SPELL, pitch = 2)
@RestrictWandControls(dontRestrictLineOfSight = true)
@DamageSpell(deathFormat = "%victim% was blasted by %attacker%!", defaultDamage = 6)
// Override parent class defaults for these
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 80)
@SpellOption(optionName = "hitbox-size", type = SpellOptionType.DOUBLE, defaultDouble = 0.8)
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.BAD, enumType = AlignmentType.class)
public class EldritchBlast extends ProjectileSpell implements RawSpell {
	public EldritchBlast(SpellConfig config, String directory) {
		super(config, directory);

		damage = config.getDouble("damage");

		NormalParticleEffect effect = new NormalParticleEffect();
		double size = 0.1;
		int particleAmount = (int) (size * 25);
		effect.setAmount(particleAmount);
		effect.setXYZ(size);
		DustOptions data = new DustOptions(Color.fromRGB(100, 0, 150), 0.8F);
		effect.setOptions(data);

		NormalParticleEffect coreEffect = new NormalParticleEffect();
		coreEffect.setAmount(particleAmount /3);
		coreEffect.setXYZ(0.1);

		NormalParticleEffect endEffect = new NormalParticleEffect();
		endEffect.setAmount(50);
		endEffect.setXYZ(size);
		endEffect.setSpeed(0.1);

		Particle particle = Particle.DUST;
		effects.addEffect(effect, particle);
		Particle core = Particle.SMOKE;
		effects.addEffect(coreEffect, core);

		Particle finalParticle = Particle.WITCH;
		endEffects.addEffect(endEffect, finalParticle);
	}

	private final double damage;

	/*************/
	/* Particles */

	private final WbsParticleGroup effects = new WbsParticleGroup();
	private final WbsParticleGroup endEffects = new WbsParticleGroup();

	@Override
	public boolean castRaw(CastingContext context) {
		SpellCaster caster = context.caster;
		EldritchBlastProjectile projectile = new EldritchBlastProjectile(caster.getEyeLocation(), caster, this);

		projectile.setDamage(damage);

		projectile.setParticle(effects);
		projectile.setEndEffects(endEffects);

		projectile.setHitSound(hitSound);

		projectile.run();
		return true;
	}

	protected final WbsSoundGroup hitSound = new WbsSoundGroup(
			new WbsSound(Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 2F)
			);

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDamage: &7" + damage;
		
		return asString;
	}
}
