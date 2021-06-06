package wbs.magic.spellinstances.ranged.projectile;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.*;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.objects.projectiles.FireballProjectile;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

@Spell(name = "Fireball",
		cost = 50,
		cooldown = 25,
		description = "Shoot a ball of fire that deals damage to a large radius of creatures, igniting any that survive.")
@DamageSpell(deathFormat = "%victim% was scorched by %attacker%!",
		defaultDamage = 5,
		suicidePossible = true,
		suicideFormat = "%player% fireballed themself!"
)
@FailableSpell("This spell cannot be used under water, and will fail if it hits water before reaching a block or creature.")
@RestrictWandControls(dontRestrictLineOfSight = true)
@SpellOption(optionName = "radius", type = SpellOptionType.DOUBLE, defaultDouble = 5)
public class FireballSpell extends ProjectileSpell {

	private static final double DEFAULT_RANGE = 60;
	private static final double DEFAULT_SPEED = 40;
	
	protected FireballSpell(SpellConfig config, String directory) {
		super(config, directory, DEFAULT_RANGE, DEFAULT_SPEED);

		damage = config.getDouble("damage", damage);
		radius = config.getDouble("radius", radius);

		NormalParticleEffect effect = new NormalParticleEffect();
		BlockData blockData = Bukkit.createBlockData(Material.MAGMA_BLOCK);
		effect.setOptions(blockData);

		Particle particle = Particle.BLOCK_CRACK;
		effects.addEffect(effect, particle);
	}

	private double damage = 5;
	private double radius = 5;

	private final WbsParticleGroup effects = new WbsParticleGroup();

	@Override
	public boolean cast(SpellCaster caster) {
		FireballProjectile projectile = new FireballProjectile(caster.getEyeLocation(), caster, this);

		projectile.configure(this);
		
		projectile.setDamage(damage);
		projectile.setRadius(radius);
		
		projectile.setParticle(effects);
		
		projectile.setFireDirection(caster.getFacingVector());
		projectile.run();
		return true;
	}
}
