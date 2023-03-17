package wbs.magic.spells.ranged.targeted;

import org.bukkit.entity.LivingEntity;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spells.framework.LivingEntitySpell;
import wbs.magic.targeters.GenericTargeter;

@TargeterOption(optionName = "targeter")
public abstract class TargetedSpell extends SpellInstance implements LivingEntitySpell {

	public TargetedSpell(SpellConfig config, String directory) {
		super(config, directory);

		targeter = config.getTargeter("targeter");
		targeter.setDefaultTargetClass(targetClass);
	}

	protected GenericTargeter targeter;

	protected Class<? extends LivingEntity> targetClass = LivingEntity.class;

	public String toString() {
		String asString = super.toString();

		asString += "\n&rTarget: &7" + targeter.toString();

		return asString;
	}

	@Override
	public GenericTargeter getTargeter() {
		return targeter;
	}
}
