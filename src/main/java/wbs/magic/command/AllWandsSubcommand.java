package wbs.magic.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import wbs.magic.wand.MagicWand;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.menus.MenuSlot;
import wbs.utils.util.menus.WbsMenu;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AllWandsSubcommand extends WbsSubcommand {
    public AllWandsSubcommand(WbsPlugin plugin) {
        super(plugin, "allwands");
        addAlias("wands");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage("Only players may use this command.", sender);
            return true;
        }

        // The number of slots excluding the borders
        // (i.e. 7 columns x 4 rows, down from 9 columns x 6 rows)
        int pageSize = 28;

        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sendMessage("Invalid page number &h" + args[1] + "&r; use an integer.", sender);
                return true;
            }
        }

        page--;

        Player player = (Player) sender;

        List<MagicWand> usableWands = MagicWand.allWands()
                .stream()
                .filter(wand -> player.hasPermission(wand.getPermission()))
                .collect(Collectors.toList());

        if (usableWands.size() == 0) {
            sendMessage("You don't have permission to use any wands.", sender);
            return true;
        }

        if (usableWands.size() < page * pageSize) {
            page = 0;
        }

        WbsMenu menu = new WbsMenu(plugin, "Wands (Page " + (page + 1) + ")", 6, "wands:" + player.getName());

        MenuSlot borderSlot = new MenuSlot(plugin, getBorderItem());
        menu.setUnregisterOnClose(true);
        menu.setOutline(borderSlot);

        int index = 0;
        boolean hasPrevPage = false;
        boolean hasNextPage = false;
        for (MagicWand wand : usableWands) {
            boolean beforePage = index < page * pageSize;
            boolean afterPage = index >= (page+1) * pageSize;

            hasPrevPage |= beforePage;
            hasNextPage |= afterPage;

            boolean onPage = !beforePage && !afterPage;

            index++;
            if (onPage) {
                menu.setNextFreeSlot(getWandSlot(player, wand));
            }
        }

        if (hasPrevPage) {
            MenuSlot prevPageButton = new MenuSlot(plugin, getPrevItem());
            // Decrement has already been done to convert to internal index
            int prevPage = page;
            prevPageButton.setClickAction((
                    (event) -> {
                        Player clickingPlayer = (Player) event.getWhoClicked();
                        clickingPlayer.performCommand(label + " " + args[0] + " " + prevPage);
                    }
            ));

            menu.setSlot(18, prevPageButton);
            menu.setSlot(27, prevPageButton);
        }

        if (hasNextPage) {
            MenuSlot prevPageButton = new MenuSlot(plugin, getNextItem());
            // Decrement reduced by 1, so increase by 2 since command accepts human readable index
            int nextPage = page + 2;
            prevPageButton.setClickAction((
                    (event) -> {
                        Player clickingPlayer = (Player) event.getWhoClicked();
                        clickingPlayer.performCommand(label + " " + args[0] + " " + nextPage);
                    }
            ));

            menu.setSlot(26, prevPageButton);
            menu.setSlot(35, prevPageButton);
        }

        menu.showTo(player);

        return true;
    }

    private MenuSlot getWandSlot(Player player, final MagicWand wand) {
        MenuSlot slot = new MenuSlot(plugin, wand.getItem());

        // Checking here instead of in the command,
        if (player.hasPermission("wbsmagic.command.admin.wand")) {
            slot.setClickAction(
                    (event) -> {
                        Player clickingPlayer = (Player) event.getWhoClicked();
                        clickingPlayer.getInventory().addItem(wand.getItem());
                    }
            );
        }

        return slot;
    }

    private ItemStack getBorderItem() {
        Material borderMaterial = Material.PURPLE_STAINED_GLASS_PANE;
        ItemStack menuBorder = new ItemStack(borderMaterial);
        ItemMeta borderMeta =
                Objects.requireNonNull(
                        Bukkit.getItemFactory().getItemMeta(borderMaterial)
                );

        borderMeta.setDisplayName(ChatColor.RESET + "");

        menuBorder.setItemMeta(borderMeta);
        return menuBorder;
    }

    private ItemStack getPrevItem() {
        Material borderMaterial = Material.RED_STAINED_GLASS;
        ItemStack menuBorder = new ItemStack(borderMaterial);
        ItemMeta borderMeta =
                Objects.requireNonNull(
                        Bukkit.getItemFactory().getItemMeta(borderMaterial)
                );

        borderMeta.setDisplayName(plugin.dynamicColourise("&c&lPrevious page"));

        menuBorder.setItemMeta(borderMeta);
        return menuBorder;
    }

    private ItemStack getNextItem() {
        Material borderMaterial = Material.LIME_STAINED_GLASS;
        ItemStack menuBorder = new ItemStack(borderMaterial);
        ItemMeta borderMeta =
                Objects.requireNonNull(
                        Bukkit.getItemFactory().getItemMeta(borderMaterial)
                );

        borderMeta.setDisplayName(plugin.dynamicColourise("&a&lNext page"));

        menuBorder.setItemMeta(borderMeta);
        return menuBorder;
    }
}
