package wbs.magic.spellinstances.ranged.targeted;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

import wbs.magic.annotations.SpellSound;
import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.Spell;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.LineOfSightTargeter;
import wbs.magic.wrappers.SpellCaster;

@Spell(name = "Displace",
		cost = 35,
		cooldown = 15,
		description = "Swap places with the target entity. If the spell has multiple targets, all targets have their location swapped with another random target"
)
@SpellSound(sound = Sound.ENTITY_ENDERMAN_TELEPORT)
public class Displace extends TargetedSpell {

	private final static GenericTargeter DEFAULT_TARGETER = new LineOfSightTargeter();
	
	public Displace(SpellConfig config, String directory) {
		super(config, directory, DEFAULT_TARGETER);
	}

	@Override
	protected void castOn(SpellCaster caster, LivingEntity target) {
		Location savePos = caster.getLocation();
		
		caster.getPlayer().teleport(target);
		target.teleport(savePos);
	}
}
