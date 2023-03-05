package wbs.magic.spells.ranged.targeted;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.configuration.FailableSpell;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.SpellCaster;
import wbs.utils.util.WbsSound;

@Spell(name = "Counter Spell",
		description = "The targeted players next spell within a certain amount of time is 'countered', meaning the spell will not take effect, but will still start its cooldown and take mana from the user")
@FailableSpell("If the targeted player does not cast a spell within the duration of counter spell, the effect will fade and no spell will be countered.")
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 15)
// Overrides
@TargeterOption(optionName = "targeter", defaultRange = 30)
public class CounterSpell extends TargetedSpell {
	
	public CounterSpell(SpellConfig config, String directory) {
		super(config, directory);

		duration = config.getDurationFromDouble("duration");
		
		targetClass = Player.class;
	}
	
	private final int duration; // time in ticks to wait to counter a spell

	private final WbsSound sound = new WbsSound(Sound.ENTITY_VEX_CHARGE, 2, 2);
	
	@Override
	public void castOn(CastingContext context, LivingEntity target) {
		Player playerTarget = (Player) target;
		if (SpellCaster.isRegistered(playerTarget)) {
			SpellCaster otherCaster = SpellCaster.getCaster(playerTarget);

			MagicEntityEffect counterEffect = new MagicEntityEffect(playerTarget, context.caster, this);

			counterEffect.setMaxAge(duration);

			counterEffect.run();

			sound.play(playerTarget.getLocation());
		}
	}
	
	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDuration: &7" + duration + " seconds";
		
		return asString;
	}
}
