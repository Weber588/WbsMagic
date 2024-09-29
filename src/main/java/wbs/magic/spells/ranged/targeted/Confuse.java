package wbs.magic.spells.ranged.targeted;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import wbs.magic.objects.AlignmentType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.SpellCaster;

import wbs.magic.spellmanagement.configuration.options.EnumOptions;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.WbsMath;

@Spell(name = "Confuse",
		description = "Make a mob forget it's angry at you, or, if the target is a player, give them nausea" +
				"and make them look in a random direction at the start and end of the duration.")
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 5)
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.NEGATIVE, enumType = AlignmentType.class)
public class Confuse extends TargetedSpell {
	public Confuse(SpellConfig config, String directory) {
		super(config, directory);

		duration = (int) (config.getDouble("duration") * 20);
		// aka nausea
		PotionEffectType potionType = PotionEffectType.NAUSEA;
		potion = new PotionEffect(potionType, duration, 0, true, false, true);
	}

	private final int duration;
	private final PotionEffect potion;

	@Override
	public void castOn(CastingContext context, LivingEntity target) {
		Location newLoc = target.getLocation();
		newLoc.setDirection(WbsMath.randomVector());
		target.teleport(newLoc);

		if (target instanceof Player) {
			target.addPotionEffect(potion, true);

		} else if (target instanceof Mob) {
			Mob mob = (Mob) target;

			new BukkitRunnable() {
				int age = 0;

				@Override
				public void run() {
					age++;

					mob.setTarget(null);

					if (age % 20 == 0) {
						Location newLoc = target.getLocation();
						newLoc.setDirection(WbsMath.randomVector());
						target.teleport(newLoc);
					}

					if (age > duration) {
						cancel();
					}
				}
			}.runTaskTimer(plugin, 0, 1);
		}
	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDuration: &7" + duration + " seconds";

		return asString;
	}
}
