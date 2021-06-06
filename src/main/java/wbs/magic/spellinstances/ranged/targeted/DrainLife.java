package wbs.magic.spellinstances.ranged.targeted;

import org.bukkit.entity.LivingEntity;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.*;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.LineOfSightTargeter;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.WbsEntities;

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
	
	private final static GenericTargeter DEFAULT_TARGETER = new LineOfSightTargeter();
	
	public DrainLife(SpellConfig config, String directory) {
		super(config, directory, DEFAULT_TARGETER);

		damage = config.getDouble("damage", damage);
		heal = config.getDouble("heal", heal);
	}

	private double damage = 2; // in half hearts
	private double heal = 1; // in half hearts
	
	@Override
	protected void castOn(SpellCaster caster, LivingEntity target) {
		// TODO: Move to timer that slowly leeches life until the target dies, or the caster is damaged
		caster.damage(target, damage, this);
		WbsEntities.heal(target, heal);
	}
	
	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rHeal: &7" + heal;
		asString += "\n&rDamage: &7" + damage;
		
		return asString;
	}
}
