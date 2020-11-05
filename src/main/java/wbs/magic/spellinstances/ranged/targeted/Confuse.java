package wbs.magic.spellinstances.ranged.targeted;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.*;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.enums.SpellType;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.LineOfSightTargeter;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.WbsMath;

@Spell(name = "Confuse",
		description = "") // TODO: Finish this spell and add description
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 2)
public class Confuse extends TargetedSpell {

	private final static GenericTargeter DEFAULT_TARGETER = new LineOfSightTargeter();
	
	public Confuse(SpellConfig config, String directory) {
		super(config, directory, DEFAULT_TARGETER);

		duration = config.getDouble("duration", duration);
	}
	
	private double duration = 2; // in seconds

	private PotionEffectType potionType = PotionEffectType.CONFUSION; // aka nausea
	private PotionEffect potion = new PotionEffect(potionType, (int) duration*20, 0, true, false, true);
	
	@Override
	protected void castOn(SpellCaster caster, LivingEntity target) {
		target.addPotionEffect(potion, true);
		Vector facing = WbsMath.getFacingVector(target).add(WbsMath.randomVector(0.5));
		
		Location newLoc = target.getLocation();
		Location pitchLoc = facing.toLocation(newLoc.getWorld());
		newLoc.setPitch(pitchLoc.getPitch());
		newLoc.setYaw(pitchLoc.getYaw());
		target.teleport(newLoc);
	}

	@Override
	public SpellType getType() {
		return SpellType.CONFUSE;
	}
	
	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDuration: &7" + duration + " seconds";
		
		return asString;
	}
}
