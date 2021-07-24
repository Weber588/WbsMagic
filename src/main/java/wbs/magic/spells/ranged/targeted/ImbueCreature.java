package wbs.magic.spells.ranged.targeted;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.SpellCaster;

@Spell(name = "Imbue Creature",
		cost = 15,
		cooldown = 15,
		description = "The target creature is given a potion effect."
)
@SpellOption(optionName = "potion", type = SpellOptionType.STRING)
@SpellOption(optionName = "level", type = SpellOptionType.INT, defaultInt = 1, aliases = {"amplifier"})
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultInt = 1, aliases = {"time", "length"})
public class ImbueCreature extends TargetedSpell {
	public ImbueCreature(SpellConfig config, String directory) {
		super(config, directory);

		String potionTypeString = config.getString("potion");
		PotionEffectType potionType = PotionEffectType.getByName(potionTypeString);
		if (potionType == null) {
			logError("Invalid potion type: " + potionTypeString, directory);
			return;
		}

		int level = config.getInt("level");

		level--; // Translate between normal counting and computer counting
		if (level < 0 || level > 256) {
			logError("Invalid level (defaulting to 1); Use an int between 1 and 256", directory);
			level = 0;
		}

		double duration = config.getDouble("duration");

		if (duration <= 0) {
			logError("Invalid duration (defaulting to 10 seconds); Duration must be greater than 0.", directory);
			duration = 10;
		}
		
		duration *= 20; // Convert from seconds into ticks

		effect = potionType.createEffect((int) duration, level);
	}
	
	private PotionEffect effect;

	@Override
	protected <T extends LivingEntity> void castOn(SpellCaster caster, T target) {
		target.addPotionEffect(effect, true);
	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rPotion: &7" + effect.getType().getName();
		asString += "\n&rDuration: &7" + (((double) effect.getDuration())/20) + " seconds";
		asString += "\n&rLevel: &7" + (effect.getAmplifier() + 1);
		
		return asString;
	}
}
