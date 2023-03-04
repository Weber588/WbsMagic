package wbs.magic.spells.ranged.targeted;

import java.util.Collection;
import java.util.Set;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import wbs.magic.objects.AlignmentType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.options.EnumOptions;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.statuseffects.generics.StatusEffect;
import wbs.magic.SpellCaster;

@Spell(name = "Hex",
		cost = 20,
		cooldown = 60,
		description = "Place a curse on the target creature. You deal more damage to that mob for a set amount of time."
)
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.BAD, enumType = AlignmentType.class)
public class HexSpell extends TargetedSpell {
	public HexSpell(SpellConfig config, String directory) {
		super(config, directory);
	}

	private StatusEffect status = null;

	@Override
	public boolean preCastEntity(CastingContext context, Collection<LivingEntity> targets) {
		SpellCaster caster = context.caster;
		caster.sendActionBar("In progress");
	//	status = new StatusEffect(StatusEffectType., caster, 20 * (int) duration);
		return true;
	}
	
	@Override
	public void castOn(CastingContext context, LivingEntity target) {
		// Safe cast ensured by targetedClass declared in constructor
		Player playerTarget = (Player) target;
		if (SpellCaster.isRegistered(playerTarget)) {
			SpellCaster otherCaster = SpellCaster.getCaster(playerTarget);
			
			otherCaster.addStatusEffect(status);
		}
	}
}
