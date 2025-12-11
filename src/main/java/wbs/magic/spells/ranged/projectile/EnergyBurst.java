package wbs.magic.spells.ranged.projectile;

import org.bukkit.Particle;
import org.bukkit.Sound;
import wbs.magic.objects.AlignmentType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.objects.projectiles.EnergyBurstProjectile;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.SpellCaster;

import wbs.magic.spellmanagement.configuration.options.EnumOptions;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.WbsSound;
import wbs.utils.util.WbsSoundGroup;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.SpiralParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

@Spell(name = "Energy Burst",
		cost = 100,
		cooldown = 30,
		description = "Create a burst of energy at a point you can see that throws nearby creatures upwards.")
@DamageSpell(deathFormat = "%victim% was vaporized by %attacker%!",
		defaultDamage = 4,
		suicidePossible = true,
		suicideFormat = "%player% vaporized themself!"
)
@SpellSound(sound = "entity.illusioner.mirror.move", pitch = 1.5F)
@RestrictWandControls(dontRestrictLineOfSight = true)
@SpellOption(optionName = "radius", type = SpellOptionType.DOUBLE)
@SpellOption(optionName = "force", type = SpellOptionType.DOUBLE)
// Override parent class defaults for these
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 100)
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.BAD, enumType = AlignmentType.class)
public class EnergyBurst extends ProjectileSpell {
	public EnergyBurst(SpellConfig config, String directory) {
		super(config, directory);

		damage = config.getDouble("damage");
		radius = config.getDouble("radius");
		force = config.getDouble("force");

		double size = 0.25;
		effect.setRadius(size);
		effect.setAmount(2);
		NormalParticleEffect explodeEffect = new NormalParticleEffect();
		explodeEffect.setXYZ(0);
		explodeEffect.setAmount(500);
		explodeEffect.setSpeed(force);

		Particle explodeParticle = Particle.TOTEM_OF_UNDYING;
		explodeGroup.addEffect(explodeEffect, explodeParticle);
	}

	private final double damage;
	private final double radius;
	private final double force;
	
	private final SpiralParticleEffect effect = new SpiralParticleEffect();

	private final WbsParticleGroup explodeGroup = new WbsParticleGroup();
	
	@Override
	public boolean cast(CastingContext context) {
		SpellCaster caster = context.caster;
		EnergyBurstProjectile projectile = new EnergyBurstProjectile(caster.getEyeLocation(), caster, this);

		projectile.setHitSound(hitSound);
		projectile.setRadius(radius);
		projectile.setForce(force);
		projectile.setDamage(damage);

		projectile.setSpiralEffect(effect);
		projectile.setEndEffects(explodeGroup);

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
		asString += "\n&rRadius: &7" + radius;
		asString += "\n&rForce: &7" + force;
		
		return asString;
	}
}
