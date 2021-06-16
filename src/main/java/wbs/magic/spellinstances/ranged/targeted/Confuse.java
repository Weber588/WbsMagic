package wbs.magic.spellinstances.ranged.targeted;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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

	private final static GenericTargeter DEFAULT_TARGETER = new LineOfSightTargeter();

	public Confuse(SpellConfig config, String directory) {
		super(config, directory, DEFAULT_TARGETER);

		duration = config.getDouble("duration", duration);
		potion = new PotionEffect(potionType, (int) duration*20, 0, true, false, true);
	}

	private double duration = 2; // in seconds

	private PotionEffectType potionType = PotionEffectType.CONFUSION; // aka nausea
	private PotionEffect potion;

	@Override
	protected void castOn(SpellCaster caster, LivingEntity target) {

		if (target instanceof Player) {
			target.addPotionEffect(potion, true);
			Vector facing = WbsMath.getFacingVector(target).add(WbsMath.randomVector(0.5));

			Location newLoc = target.getLocation();
			Location pitchLoc = facing.toLocation(newLoc.getWorld());
			newLoc.setPitch(pitchLoc.getPitch());
			newLoc.setYaw(pitchLoc.getYaw());
			target.teleport(newLoc);
		} else if (target instanceof Mob) {
			((Mob) target).setTarget(null);;
		} else {
			caster.sendActionBar("...but it had no effect!");
		}
	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDuration: &7" + duration + " seconds";

		return asString;
	}
}
