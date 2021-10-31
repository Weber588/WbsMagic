package wbs.magic.spells.ranged.targeted;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.DamageSpell;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.SpellCaster;

import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.particles.NormalParticleEffect;


@Spell(name = "Inflict Wounds",
		cost = 25,
		cooldown = 5,
		description = "The most simple damage spell, it instantly damages target creatures."
)
@DamageSpell(defaultDamage = 4) // Can't think of a custom death message right now
// Overrides
@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 10)
@SpellOption(optionName = "targeter", type = SpellOptionType.STRING, defaultString = "NEAREST")
public class InflictWounds extends TargetedSpell {
	public InflictWounds(SpellConfig config, String directory) {
		super(config, directory);

		damage = config.getDouble("damage", damage);

		effect.setXYZ(0)
				.setAmount(15);
	}
	
	private final NormalParticleEffect effect = new NormalParticleEffect();
	private final Particle particle = Particle.DAMAGE_INDICATOR;
	
	private double damage = 6;

	@Override
	public void castOn(CastingContext context, LivingEntity target) {
		SpellCaster caster = context.caster;
		caster.damage(target, damage, this);
		effect.play(particle, WbsEntities.getMiddleLocation(target));
	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDamage: &7" + damage;

		return asString;
	}
}
