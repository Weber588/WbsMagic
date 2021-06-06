package wbs.magic.spellinstances.ranged.targeted;

import java.util.Set;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.*;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.statuseffects.CounteredStatus;
import wbs.magic.statuseffects.generics.StatusEffect;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.LineOfSightTargeter;
import wbs.magic.wrappers.SpellCaster;

@Spell(name = "Counter Spell",
		description = "The targeted players next spell within a certain amount of time is 'countered', meaning the spell will not take effect, but will still start its cooldown and take mana from the user")
@FailableSpell("If the targeted player does not cast a spell within the duration of counter spell, the effect will fade and no spell will be countered.")
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 3)
public class CounterSpell extends TargetedSpell {

	protected final static double DEFAULT_RANGE = 30;
	protected final static GenericTargeter DEFAULT_TARGETER = new LineOfSightTargeter();
	
	public CounterSpell(SpellConfig config, String directory) {
		super(config, directory, DEFAULT_RANGE, DEFAULT_TARGETER);

		duration = config.getDouble("duration", duration);
		
		targetClass = Player.class;
	}
	
	private double duration = 3; // time in seconds to wait to counter a spell

	@Override
	public <T extends LivingEntity> boolean preCast(SpellCaster caster, Set<T> targets) {
		StatusEffect status = new CounteredStatus(caster, 20 * (int) duration); //new StatusEffect(StatusEffectType.COUNTERED, caster, 20 * (int) duration);
		for (LivingEntity target : targets) {
			Player playerTarget = (Player) target;
			if (SpellCaster.isRegistered(playerTarget)) {
				SpellCaster otherCaster = SpellCaster.getCaster(playerTarget);
				
				otherCaster.addStatusEffect(status);
			}
		}
		return true;
	}
	
	@Override
	protected <T extends LivingEntity> void castOn(SpellCaster caster, T target) {

	}
	
	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDuration: &7" + duration + " seconds";
		
		return asString;
	}
}
