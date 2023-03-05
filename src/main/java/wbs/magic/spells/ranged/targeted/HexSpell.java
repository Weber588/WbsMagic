package wbs.magic.spells.ranged.targeted;

import org.bukkit.entity.LivingEntity;

import wbs.magic.objects.AlignmentType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.spells.framework.LivingEntitySpell;
import wbs.magic.targeters.GenericTargeter;

@Spell(name = "Hex",
		cost = 20,
		cooldown = 60,
		description = "Place a curse on the target creature. You deal more damage to that mob for a set amount of time."
)
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.BAD, enumType = AlignmentType.class)
@TargeterOption(optionName = "targeter")
public class HexSpell extends SpellInstance implements LivingEntitySpell {
	public HexSpell(SpellConfig config, String directory) {
		super(config, directory);

		targeter = config.getTargeter("targeter");
	}

	private final GenericTargeter targeter;

	@Override
	public void castOn(CastingContext context, LivingEntity target) {

	}

	@Override
	public GenericTargeter getTargeter() {
		return targeter;
	}
}
