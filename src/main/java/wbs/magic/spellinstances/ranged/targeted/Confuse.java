package wbs.magic.spellinstances.ranged.targeted;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.*;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.LineOfSightTargeter;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.WbsMath;

@Spell(name = "Confuse",
		description = "Make a mob forget it's angry at you, or, if the target is a player, give them nausea" +
				"and make them look in a random direction at the start and end of the duration.")
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 5)
public class Confuse extends TargetedSpell {
	public Confuse(SpellConfig config, String directory) {
		super(config, directory);

		duration = (int) (config.getDouble("duration") * 20);
		// aka nausea
		PotionEffectType potionType = PotionEffectType.CONFUSION;
		potion = new PotionEffect(potionType, duration, 0, true, false, true);
	}

	private final int duration;
	private final PotionEffect potion;

	@Override
	protected void castOn(SpellCaster caster, LivingEntity target) {
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
