package wbs.magic.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.magic.SpellCaster;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;

public class ManaSubcommand extends WbsSubcommand {
    public ManaSubcommand(WbsPlugin plugin) {
        super(plugin, "mana");
        addAlias("setmana");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        int amount;
        Player target = null;

        if (args.length == 1) {
            sendMessage("Usage: &h/" + label + " " + args[0] + " <integer> [player]", sender);
            return true;
        }

        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sendMessage("Usage: &h/" + label + " " + args[0] + " <integer> [player]", sender);
            return true;
        }

        if (args.length >= 3) {
            target = Bukkit.getPlayer(args[2]);

            if (target == null) {
                sendMessage("The player &h\"" + args[2] + "\"&r was not found. Please use the player's full username.", sender);
                return true;
            }
        }

        if (target == null) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sendMessage("Usage: &h/" + label + " " + args[0] + " " + args[1] + " <player>", sender);
                return true;
            }
        }

        SpellCaster targetCaster = SpellCaster.getCaster(target);
        targetCaster.setMana(amount);
        sendMessage("Set &h" + target.getName() + "&r's mana to &h" + amount + "&r.", sender);

        return true;
    }

    @Override
    public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        List<String> choices = new LinkedList<>();

        if (args.length == 3) {
            Bukkit.getOnlinePlayers().stream().map(Player::getName).forEach(choices::add);
        }

        return choices;
    }
}
