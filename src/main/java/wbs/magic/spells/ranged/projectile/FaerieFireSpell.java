package wbs.magic.spells.ranged.projectile;

import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.DamageSpell;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.objects.projectiles.FaerieFireProjectile;
import wbs.magic.SpellCaster;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

@Spell(name = "Faerie Fire",
		cost = 50,
		cooldown = 25,
		description = "Shoot a small orb of fire that persists for several seconds and damages nearby creatures.")
@DamageSpell(deathFormat = "%victim% burned away in %attacker%'s magic flames!",
		defaultDamage = 2,
		suicidePossible = true,
		suicideFormat = "%player% burnt themselves with their Faerie Fire!")
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 3)
@SpellOption(optionName = "amount", type = SpellOptionType.INT, defaultDouble = 3)
// Override parent class defaults for these
@SpellOption(optionName = "gravity", type = SpellOptionType.DOUBLE, defaultDouble = 4)
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 25)
@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 100)
public class FaerieFireSpell extends ProjectileSpell {

	public FaerieFireSpell(SpellConfig config, String directory) {
		super(config, directory);

		duration = config.getDouble("duration");
		damage = config.getDouble("damage");
		amount = config.getInt("amount");
		
		hitbox = 0;
		stepSize = 0.1;

		NormalParticleEffect effect = new NormalParticleEffect();
		effect.setAmount(1);
		effect.setSpeed(0.3);
		effect.setXYZ(0.1);

		Particle particle = Particle.SPELL_WITCH;
		effects.addEffect(effect, particle);
	}
	
	private final double duration;
	private final double damage;
	private final int amount;

	private final WbsParticleGroup effects = new WbsParticleGroup();
	
	@Override
	public boolean cast(SpellCaster caster) {
		new BukkitRunnable() {
			int fired = 0;
			@Override
			public void run() {
				fire(caster);
				fired++;
				if (fired >= amount) {
					cancel();
				}
			}
			
		}.runTaskTimer(plugin, 0L, 5L);
		
		return true;
	}
	
	private void fire(SpellCaster caster) {
		FaerieFireProjectile projectile = new FaerieFireProjectile(caster.getEyeLocation(), caster, this);

		projectile.configure(this);
		
		projectile.setDamage(damage);
		projectile.setDuration(duration);
		
		projectile.setParticle(effects);
		
		projectile.setFireDirection(caster.getFacingVector());
		projectile.run();
	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDamage: &7" + damage;
		asString += "\n&rDuration: &7" + duration + " seconds";
		
		return asString;
	}
}