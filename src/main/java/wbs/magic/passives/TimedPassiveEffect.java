package wbs.magic.passives;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import wbs.magic.wand.MagicWand;

public interface TimedPassiveEffect {
    void onStart(MagicWand wand, Player player, ItemStack item, EquipmentSlot slot);
    void onTick(MagicWand wand, Player player, ItemStack item, EquipmentSlot slot);
    void onStop(MagicWand wand, Player player, ItemStack item, EquipmentSlot slot);
}
