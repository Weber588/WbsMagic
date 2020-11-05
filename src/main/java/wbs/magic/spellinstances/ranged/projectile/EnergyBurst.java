package wbs.magic.spellinstances.ranged.projectile;

import org.bukkit.Particle;
import org.bukkit.Sound;
import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.*;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.enums.SpellType;
import wbs.magic.objects.projectiles.EnergyBurstProjectile;
import wbs.magic.wrappers.SpellCaster;

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
@RestrictWandControls(dontRestrictLineOfSight = true)
@SpellOption(optionName = "radius", type = SpellOptionType.DOUBLE)
@SpellOption(optionName = "force", type = SpellOptionType.DOUBLE)
public class EnergyBurst extends ProjectileSpell {
	protected final static double DEFAULT_SPEED = 100;
	public EnergyBurst(SpellConfig config, String directory) {
		super(config, directory, DEFAULT_SPEED);

		damage = config.getDouble("damage", damage);
		radius = config.getDouble("radius", radius);
		force = config.getDouble("force", force);
		
		stepSize = 0.25;

		double size = 0.25;
		effect.setRadius(size);
		effect.setAmount(2);
		NormalParticleEffect explodeEffect = new NormalParticleEffect();
		explodeEffect.setXYZ(0);
		explodeEffect.setAmount(500);
		explodeEffect.setSpeed(force);

		Particle explodeParticle = Particle.TOTEM;
		explodeGroup.addEffect(explodeEffect, explodeParticle);
	}

	private double damage = 4;
	private double radius = 4;
	private double force = 1.5;
	
	private final SpiralParticleEffect effect = new SpiralParticleEffect();

	private final WbsParticleGroup explodeGroup = new WbsParticleGroup();
	
	@Override
	public boolean cast(SpellCaster caster) {
		EnergyBurstProjectile projectile = new EnergyBurstProjectile(caster.getEyeLocation(), caster, this);

		projectile.configure(this);
		
		projectile.setSound(hitSound);
		projectile.setRadius(radius);
		
		projectile.setSpiralEffect(effect);
		projectile.setFizzleEffect(explodeGroup);
		
		projectile.setFireDirection(caster.getFacingVector());
		projectile.run();
		return true;
	}

	@Override
	public SpellType getType() {
		return SpellType.ENERGY_BURST;
	}

	protected final WbsSoundGroup castSound = new WbsSoundGroup(
			new WbsSound(Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.5F)
			);
	protected final WbsSoundGroup hitSound = new WbsSoundGroup(
			new WbsSound(Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 2F)
			);
	
	@Override
	public WbsSoundGroup getCastSound() {
		return castSound;
	}
	
	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDamage: &7" + damage;
		asString += "\n&rRadius: &7" + radius;
		asString += "\n&rForce: &7" + force;
		
		return asString;
	}
}
