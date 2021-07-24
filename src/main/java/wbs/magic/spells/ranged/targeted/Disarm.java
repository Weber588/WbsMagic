package wbs.magic.spells.ranged.targeted;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.FailableSpell;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.wand.MagicWand;
import wbs.magic.SpellCaster;
import wbs.utils.util.WbsMath;

@Spell(name = "Disarm",
		description = "The target creature drops whatever item they're currently holding."
)
// TODO: Investigate possibility of making castOn return a bool that gets counted in targeted spells
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
				MagicWand checkWand = MagicWand.getWand(heldItem);
				if (checkWand != null && checkWand.isDisarmImmune()) return;

				Location dropLoc = target.getLocation();

				Item droppedItem = target.getWorld().dropItemNaturally(dropLoc, heldItem);
				droppedItem.setVelocity(WbsMath.randomVector());

				equip.setItemInMainHand(new ItemStack(Material.AIR));
			}
		}
	}
}
