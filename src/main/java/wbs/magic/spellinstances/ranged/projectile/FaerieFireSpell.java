package wbs.magic.spellinstances.ranged.projectile;

import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.DamageSpell;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.objects.projectiles.FaerieFireProjectile;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

@Spell(name = "Faerie Fire",
		cost = 50,
		cooldown = 25,
		description = "Shoot a small orb of fire that persists for several seconds and damages nearby creatures.")
@DamageSpell(deathFormat = "%victim% burned away in %attacker%'s magic flames!",
		defaultDamage = 2,
		suicidePossible = true,
		suicideFormat = "%player% burnt themselves with their Faerie Fire!"
)
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 3)
@SpellOption(optionName = "amount", type = SpellOptionType.INT, defaultDouble = 3)
@SpellOption(optionName = "gravity", type = SpellOptionType.DOUBLE, defaultDouble = 4)
public class FaerieFireSpell extends ProjectileSpell {

	private final static double DEFAULT_SPEED = 25;
	private final static double DEFAULT_GRAVITY = 4;
	private final static double DEFAULT_RANGE = 100;
	
	public FaerieFireSpell(SpellConfig config, String directory) {
		super(config, directory, DEFAULT_RANGE, DEFAULT_SPEED, DEFAULT_GRAVITY);

		duration = config.getDouble("duration", duration);
		damage = config.getDouble("damage", damage);
		amount = config.getInt("amount", amount);
		
		hitbox = 0;
		stepSize = 0.1;

		NormalParticleEffect effect = new NormalParticleEffect();
		effect.setAmount(1);
		effect.setSpeed(0.3);
		effect.setXYZ(0.1);

		Particle particle = Particle.SPELL_WITCH;
		effects.addEffect(effect, particle);
	}
	
	private double duration = 3;
	private double damage = 2;
	private int amount = 3;

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
