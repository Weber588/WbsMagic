package wbs.magic.spellmanagement.configuration;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import wbs.magic.MagicSettings;
import wbs.magic.WbsMagic;
import wbs.magic.wand.MagicWand;
import wbs.utils.util.WbsEnums;

public class ItemCost {

    private final static String ITEM_KEY = "item-cost";

    private Material material;
    private int amount;

    private boolean active = false;

    public ItemCost(ConfigurationSection section, String directory) {
        ConfigurationSection itemCostSection = section.getConfigurationSection(ITEM_KEY);

        if (itemCostSection == null) {
            return;
        }

        String materialString = itemCostSection.getString("material");
        if (materialString == null) {
            MagicSettings.getInstance()
                    .logError("Material is a required field for item costs.",
                            directory + "/" + ITEM_KEY + "/material");
            return;
        }
        Material checkMaterial = WbsEnums.getEnumFromString(Material.class, materialString);
        if (checkMaterial == null) {
            MagicSettings.getInstance()
                    .logError("Invalid material: " + materialString,
                            directory + "/" + ITEM_KEY + "/material");
            return;
        }

        material = checkMaterial;
        amount = section.getInt("amount", 1);
        active = true;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isActive() {
        return active;
    }

    public boolean checkPlayer(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) return true;

        int found = 0;
        for (ItemStack item : player.getInventory()) {
            if (item.getType() == material) {
                if (!MagicWand.isWand(item)) {
                    found += item.getAmount();
                }
            }
        }

        return found >= amount;
    }

    public boolean takeFromPlayer(Player player) {
        if (!checkPlayer(player)) return false;
        if (player.getGameMode() == GameMode.CREATIVE) return true;

        int leftToTake = amount;
        PlayerInventory inv = player.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() == material) {
                if (MagicWand.isWand(item)) continue;

                int itemAmount = item.getAmount();

                if (itemAmount > leftToTake) {
                    item.setAmount(itemAmount - leftToTake);
                    leftToTake = 0;
                    break;
                } else if (itemAmount == leftToTake) {
                    inv.remove(item);
                    leftToTake = 0;
                    break;
                } else {
                    inv.remove(item);
                    leftToTake -= itemAmount;
                }
            }
        }

        if (leftToTake > 0) {
            MagicSettings.getInstance().logError("An item cost failed to find enough items! " +
                    "Please report this to the developer.", "Internal");
            throw new AssertionError("Inventory#contains(Material, Item) guaranteed items exist, " +
                    "but ItemCost#takeFromPlayer did not find enough.");
        }

        return true;
    }

    @Override
    public String toString() {
        return amount + "x " + WbsEnums.toPrettyString(material);
    }
}