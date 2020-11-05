package wbs.magic.spellinstances.ranged.targeted;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.DamageSpell;
import wbs.magic.annotations.Spell;
import wbs.magic.enums.SpellType;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.NearestTargeter;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.WbsEntities;
import wbs.utils.util.particles.NormalParticleEffect;


@Spell(name = "Inflict Wounds",
		cost = 25,
		cooldown = 5,
		description = "The most simple damage spell, it instantly damages target creatures."
)
@DamageSpell(defaultDamage = 4) // Can't think of a custom death message right now
public class InflictWounds extends TargetedSpell {

	protected static final double DEFAULT_RANGE = 10;
	protected static final GenericTargeter DEFAULT_TARGETER = new NearestTargeter(DEFAULT_RANGE);
	
	public InflictWounds(SpellConfig config, String directory) {
		super(config, directory, DEFAULT_RANGE, DEFAULT_TARGETER);

		damage = config.getDouble("damage", damage);

		effect.setXYZ(0)
				.setAmount(15);
	}
	
	private NormalParticleEffect effect = new NormalParticleEffect();
	private Particle particle = Particle.DAMAGE_INDICATOR;
	
	private double damage = 6;

	@Override
	protected void castOn(SpellCaster caster, LivingEntity target) {
		caster.damage(target, damage, this);
		effect.play(particle, WbsEntities.getMiddleLocation(target));
	}

	@Override
	public SpellType getType() {
		return SpellType.INFLICT_WOUNDS;
	}

}
