package wbs.magic.spells.ranged.targeted;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

import wbs.magic.spellmanagement.configuration.SpellSound;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.SpellCaster;

@Spell(name = "Displace",
		cost = 35,
		cooldown = 15,
		description = "Swap places with the target entity. If the spell has multiple targets, all targets have their location swapped with another random target"
)
@SpellSound(sound = Sound.ENTITY_ENDERMAN_TELEPORT)
public class Displace extends TargetedSpell {
	public Displace(SpellConfig config, String directory) {
		super(config, directory);
	}

	@Override
	protected void castOn(SpellCaster caster, LivingEntity target) {
		Location savePos = caster.getLocation();
		
		caster.getPlayer().teleport(target);
		target.teleport(savePos);
	}
}
