package wbs.magic.spellinstances.ranged.targeted.missile;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.DamageSpell;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.objects.missiles.MagicMissile;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

import java.util.Set;

@Spell(name = "Magic Missile",
		cost = 60,
		cooldown = 45,
		description = "Energy missiles follow the target creature, dealing damage on contact. The orbs  leave behind a purple flame that damages nearby creatures (except the caster) if they hit a block."
)
@DamageSpell(deathFormat = "%victim% was destroyed by %attacker% with a magic missile!",
		defaultDamage = 7
)
@SpellOption(optionName = "delay", type = SpellOptionType.DOUBLE, defaultDouble = 20)
@SpellOption(optionName = "amount", type = SpellOptionType.INT, defaultInt = 3)
// Override parent class defaults for these
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 0.5)
@SpellOption(optionName = "agility", type = SpellOptionType.DOUBLE, defaultDouble = 100)
public class MagicMissiles extends MissileSpell {
	private final int amount;
	private final double damage;
	private final double delay; // Delay between each missile launched in ticks
	
	public MagicMissiles(SpellConfig config, String directory) {
		super(config, directory);

		amount = config.getInt("amount");
		damage = config.getDouble("damage");
		delay = config.getDouble("delay") * 20;

		double size = 0.1;
		int particleAmount = (int) (size * 25);
		NormalParticleEffect effect = new NormalParticleEffect();
		effect.setAmount(particleAmount);
		effect.setXYZ(size);
		NormalParticleEffect coreEffect = new NormalParticleEffect();
		coreEffect.setAmount(particleAmount /3);
		coreEffect.setXYZ(size /2);

		Particle coreParticle = Particle.SMOKE_NORMAL;
		Particle particle = Particle.SPELL_WITCH;
		effects.addEffect(coreEffect, coreParticle)
				.addEffect(effect, particle);
	}

	private final WbsParticleGroup effects = new WbsParticleGroup();

	@Override
	protected <T extends LivingEntity> boolean preCast(SpellCaster caster, Set<T> targets) {
		if (isConcentration) {
			caster.setConcentration(this);
		}
		return false;
	}
	
	@Override
	protected void castOn(SpellCaster caster, LivingEntity target) {
		MagicMissiles castingSpell = this;
		
		new BukkitRunnable() {
			
			int i = 0;
			@Override
			public void run() {
				if ((isConcentration && !caster.isConcentratingOn(MagicMissiles.this))) {
					caster.concentrationBroken();
					cancel();
				} else if (i < amount) {
					MagicMissile missile = new MagicMissile(caster.getEyeLocation(), caster, castingSpell);
					missile.setSpeed(speed);
					missile.setDamage(damage);
					missile.setAgility(agility);
					missile.setTarget(target);

					missile.setParticle(effects);
					
					missile.setTrajectory(caster.getFacingVector());
					missile.run();
				} else {
					caster.stopConcentration();
					cancel();
				}
				i++;
			}
		}.runTaskTimer(plugin, 0, (long) (delay));
	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDamage: &7" + damage;
		asString += "\n&rAmount: &7" + amount;
		
		return asString;
	}
}











