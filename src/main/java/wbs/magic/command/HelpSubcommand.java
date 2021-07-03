package wbs.magic.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class HelpSubcommand extends WbsSubcommand {
    public HelpSubcommand(WbsPlugin plugin) {
        super(plugin, "help");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            help(sender, 1);
        } else {
            try {
                help(sender, Integer.parseInt(args[1]));
            } catch (NumberFormatException e) {
                sendMessage("Usage: &b/mag help [page number]", sender);
                return true;
            }
        }

        return true;
    }

    private void help(CommandSender sender, int page) {
        sendMessage("&m     &0 (&5Magic&0) &r&m     ", sender);
        switch (page) {
            case 1:
                sendMessage("&h/magic&r:", sender);
                sendMessage("View your mana and the command for this menu.", sender);

                sendMessage("&h/magic help [page]&r:", sender);
                sendMessage("Display this and other help screens.", sender);

                sendMessage("&h/magic givewand <wand name> [player]&r:", sender);
                sendMessage("Give yourself or another player a specific wand.", sender);

                sendMessage("&h/magic guide [spell|controls]&r:", sender);
                sendMessage("Display this and other help screens.", sender);

                sendMessage("&h/magic info [wand name]&r:", sender);
                sendMessage("View what spells a wand can cast, and how to cast them", sender);
                break;
            default:
                help(sender, 1);
        }
    }
}
