package wbs.magic.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.magic.wand.MagicWand;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class WandSubcommand extends WbsSubcommand {
    public WandSubcommand(WbsPlugin plugin) {
        super(plugin, "wand");
        addAliases("givewand", "give", "get", "getwand");

    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player target = null;
        MagicWand wand;

        if (args.length == 1) {
            sendUsage("<wand name> [player]", sender, label, args);
            return true;
        }

        wand = MagicWand.getWand(args[1]);
        if (wand == null) {
            sendMessage("Invalid wand name: &h" + args[1] + ".&r Do &h/magic wands&r for a list.", sender);
            return true;
        }

        if (args.length >= 3) {
            if (MagicWand.wandExists(args[1])) {
                wand = MagicWand.getWand(args[1]);
            } else {
                sendMessage("Invalid wand name; do &h/magic wands&r for a list.", sender);
                return true;
            }

            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sendMessage("Player not found.", sender);
                return true;
            }
        }

        if (target == null) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sendUsage("<player>", sender, label, args);
                return true;
            }
        }

        sendMessage("Giving " + target.getName() + " &h" + wand.getDisplay(), sender);
        target.getInventory().addItem(wand.buildNewWand());

        return true;
    }

    @Override
    public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        List<String> choices = new LinkedList<>();

        switch (args.length) {
            case 2:
                choices.addAll(MagicWand.getWandNames());
                break;
            case 3:
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }

        return choices;
    }
}
