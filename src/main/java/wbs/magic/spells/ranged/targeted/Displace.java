package wbs.magic.spells.ranged.targeted;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

import org.bukkit.entity.Player;
import wbs.magic.spellmanagement.configuration.SpellSound;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.SpellCaster;
import wbs.magic.spells.framework.CastingContext;

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
	public void castOn(CastingContext context, LivingEntity target) {
		SpellCaster caster = context.caster;
		Location savePos = caster.getLocation();
		
		if (caster.teleport(target)) {
			if (target instanceof Player) {
				Player targetPlayer = (Player) target;
				if (SpellCaster.isRegistered(targetPlayer)) {
					SpellCaster targetCaster = SpellCaster.getCaster(targetPlayer);

					if (!targetCaster.teleport(savePos)) {
						caster.teleport(savePos);
					}
					return;
				}
			}

			target.teleport(savePos);
		}
	}
}
