package wbs.magic.spellinstances.ranged.targeted;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.FailableSpell;
import wbs.magic.annotations.Spell;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.LineOfSightTargeter;
import wbs.magic.wrappers.SpellCaster;

@Spell(name = "Disarm",
		description = "The target creature drops whatever item they're currently holding."
)
@FailableSpell("If the target creature is not holding anything, the spell will fail. The spell will also fail if the target is holding a wand that is immune to disarming.")
public class Disarm extends TargetedSpell {
	public Disarm(SpellConfig config, String directory) {
		super(config, directory);
	}

	@Override
	protected void castOn(SpellCaster caster, LivingEntity target) {
		EntityEquipment equip = target.getEquipment();
		if (equip != null) {
			ItemStack heldItem = equip.getItemInMainHand();
			if (heldItem.getType() != Material.AIR) {
				Location dropLoc = target.getLocation();
				if (chance(50)) {
					if (chance(50)) {
						dropLoc.add(2, 0, 0);
					} else {
						dropLoc.add(-2, 0, 0);
					}
				} else {
					if (chance(50)) {
						dropLoc.add(0, 0, 2);
					} else {
						dropLoc.add(0, 0, -2);
					}
				}
				target.getWorld().dropItemNaturally(dropLoc, heldItem);
				equip.setItemInMainHand(new ItemStack(Material.AIR));
			}
		}
	}
}
