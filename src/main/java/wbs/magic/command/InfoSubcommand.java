package wbs.magic.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.magic.enums.WandControl;
import wbs.magic.passives.PassiveEffect;
import wbs.magic.passives.PassiveEffectType;
import wbs.magic.spellinstances.SpellInstance;
import wbs.magic.wrappers.MagicWand;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InfoSubcommand extends WbsSubcommand {
    public InfoSubcommand(WbsPlugin plugin) {
        super(plugin, "info");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        MagicWand wand;

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sendMessage("Usage: &h/" + label + " " + args[0] + " <wand name>", sender);
                return true;
            }

            Player player = (Player) sender;

            ItemStack item = player.getInventory().getItemInMainHand();
            wand = MagicWand.getWand(item);

            if (wand == null) {
                sendMessage("You are not holding a valid wand; if you wish to look up info about a specific wand, use &h/ " + label + " info <wand name>", sender);
                return true;
            }
        } else {
            wand = MagicWand.getWand(args[1]);
            if (wand == null) {
                sendMessage("Invalid wand name; do &h/" + label + " wands&r for a list. (Or hold a wand)", sender);
                return true;
            }
        }

        showInfo(wand, sender);

        return true;
    }

    private void showInfo(MagicWand wand, CommandSender sender) {
        Map<Integer, Map<WandControl, SpellInstance>> bindings = wand.bindingMap();
        sendMessage("&m   &r== " + wand.getDisplay() + "&r ==&m   ", sender);
        Set<Integer> tierSet = bindings.keySet();
        boolean showTierInfo = tierSet.size() > 1;
        if (showTierInfo) {
            sendMessage("To change tier, drop your wand &owithout&r shifting!", sender);
        }
        for (int tier : tierSet) {
            Map<WandControl, SpellInstance> tiersBindings = bindings.get(tier);
            if (showTierInfo) {
                sendMessage("&5&m  &r Tier " + tier + " &5&m  ", sender);
            }
            for (WandControl control : tiersBindings.keySet()) {
                sendMessageNoPrefix(WbsEnums.toPrettyString(control) + ": &h" + tiersBindings.get(control).simpleString(), sender);
            }
        }

        Map<PassiveEffectType, PassiveEffect> passives = wand.passivesMap();
        if (passives != null && !passives.isEmpty()) {
            sendMessage("&5&m  &r Passives &5&m  ", sender);

            for (PassiveEffect effect : passives.values()) {
                sendMessageNoPrefix(effect.toString().replaceAll("\n", "\n    "), sender);
            }
        }
    }

    @Override
    public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        List<String> choices = new LinkedList<>();

        if (args.length == 2) {
            choices.addAll(MagicWand.getWandNames());
        }

        return choices;
    }
}
