package wbs.magic.spells.ranged.targeted.missile;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.magic.objects.AlignmentType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.DamageSpell;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.objects.missiles.MagicMissile;
import wbs.magic.SpellCaster;
import wbs.magic.spellmanagement.configuration.options.EnumOptions;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

import java.util.Collection;
import java.util.Set;

@Spell(name = "Magic Missile",
		cost = 60,
		cooldown = 45,
		description = "Energy missiles follow the target creature, dealing damage on contact. The orbs  leave behind a purple flame that damages nearby creatures (except the caster) if they hit a block."
)
@DamageSpell(deathFormat = "%victim% was destroyed by %attacker% with a magic missile!",
		defaultDamage = 7
)
@SpellOption(optionName = "delay", type = SpellOptionType.DOUBLE, defaultDouble = 1)
@SpellOption(optionName = "amount", type = SpellOptionType.INT, defaultInt = 3)
// Override parent class defaults for these
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 0.5)
@SpellOption(optionName = "agility", type = SpellOptionType.DOUBLE, defaultDouble = 100)
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.EVIL, enumType = AlignmentType.class)
public class MagicMissiles extends MissileSpell {
	private final int amount;
	private final double damage;
	private final double delay; // Delay between each missile launched in ticks
	
	public MagicMissiles(SpellConfig config, String directory) {
		super(config, directory);

		amount = config.getInt("amount");
		damage = config.getDouble("damage");
		delay = config.getDouble("delay") * 20;
	}

	@Override
	public boolean preCastEntity(CastingContext context, Collection<LivingEntity> targets) {
		if (isConcentration) {
			context.caster.setConcentration(this);
		}
		return false;
	}
	
	@Override
	public void castOn(CastingContext context, LivingEntity target) {
		MagicMissiles castingSpell = this;
		SpellCaster caster = context.caster;
		
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











