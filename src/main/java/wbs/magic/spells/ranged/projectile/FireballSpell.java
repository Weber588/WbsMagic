package wbs.magic.spells.ranged.projectile;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.objects.projectiles.FireballProjectile;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.SpellCaster;
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
// Override parent class defaults for these
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 60)
@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 40)
public class FireballSpell extends ProjectileSpell {
	protected FireballSpell(SpellConfig config, String directory) {
		super(config, directory);

		damage = config.getDouble("damage");
		radius = config.getDouble("radius");

		NormalParticleEffect effect = new NormalParticleEffect();
		BlockData blockData = Bukkit.createBlockData(Material.MAGMA_BLOCK);
		effect.setOptions(blockData);

		Particle particle = Particle.BLOCK_CRACK;
		effects.addEffect(effect, particle);
	}

	private final double damage;
	private final double radius;

	private final WbsParticleGroup effects = new WbsParticleGroup();

	@Override
	public boolean cast(SpellCaster caster) {
		FireballProjectile projectile = new FireballProjectile(caster.getEyeLocation(), caster, this);
		
		projectile.setDamage(damage);
		projectile.setRadius(radius);
		
		projectile.setParticle(effects);

		projectile.run();
		return true;
	}
}
