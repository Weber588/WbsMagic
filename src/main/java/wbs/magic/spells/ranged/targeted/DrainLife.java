package wbs.magic.spells.ranged.targeted;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

import org.bukkit.entity.Player;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.SpellCaster;

import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.WbsRunnable;
import wbs.utils.util.particles.NormalParticleEffect;

import java.util.Collection;
import java.util.Set;

@Spell(name = "Drain Life",
		cost = 50,
		cooldown = 25,
		description = "After casting this spell the targeted creature loses health while you gain it, until the spell is broken by taking damage, or the target dying."
)
@DamageSpell(deathFormat = "%victim% had their life drained by %attacker%!",
		defaultDamage = 2
)
@FailableSpell("If you take damage while using this spell, 50% of the health gained is lost and your concentration is broken.")
@SpellSettings(isContinuousCast = true)
@SpellOption(optionName = "heal", type = SpellOptionType.DOUBLE, defaultDouble = 1)
public class DrainLife extends TargetedSpell {

	public DrainLife(SpellConfig config, String directory) {
		super(config, directory);

		damage = config.getDouble("damage");
		heal = config.getDouble("heal");

		damageEffect.setAmount(3);
		damageEffect.setXYZ(0.2);
		damageEffect.setSpeed(0.2);

		healEffect.setAmount(3);
		healEffect.setXYZ(1);
	}

	private final double damage; // in half hearts
	private final double heal; // in half hearts

	private final NormalParticleEffect damageEffect = new NormalParticleEffect();
	private final NormalParticleEffect healEffect = new NormalParticleEffect();

	@Override
	public boolean preCastEntity(CastingContext context, Collection<LivingEntity> targets) {
		SpellCaster caster = context.caster;
		Player player = caster.getPlayer();


		WbsRunnable runnable = new WbsRunnable() {

			double playerHealth = player.getHealth();

			@Override
			public void run() {
				if (player.getHealth() < playerHealth) {
					cancel();
					caster.sendActionBar("Damage interrupted the spell!");
					return;
				}

				if (!player.isSneaking() || !caster.isCasting(DrainLife.this)) {
					cancel();
					return;
				}

				double toHeal = 0;
				for (LivingEntity target : targets) {
					if (!target.isDead()) {
						toHeal += heal;
						caster.damage(target, damage, DrainLife.this);
						damageEffect.play(Particle.DAMAGE_INDICATOR, target.getLocation());
					}
				}

				if (toHeal == 0) {
					cancel();
					caster.sendActionBar("No more targets!");
					return;
				}

				healEffect.play(Particle.HEART, player.getLocation());

				WbsEntities.heal(player, toHeal);
				playerHealth = player.getHealth();
			}
		};

		runnable.runTaskTimer(plugin, 0, 10);
		caster.setCasting(this, runnable);

		return true;
	}

	@Override
	public void castOn(CastingContext context, LivingEntity target) {

	}
	
	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rHeal: &7" + heal;
		asString += "\n&rDamage: &7" + damage;
		
		return asString;
	}
}
