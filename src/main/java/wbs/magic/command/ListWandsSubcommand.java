package wbs.magic.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.magic.wand.MagicWand;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.Set;

public class ListWandsSubcommand extends WbsSubcommand {
    public ListWandsSubcommand(WbsPlugin plugin) {
        super(plugin, "listwands");
        addAlias("wands");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Set<String> nameSet = MagicWand.getWandNames();

        String list = String.join(", ", nameSet);
        sendMessage("All wand types: &h" + list, sender);

        return true;
    }
}
