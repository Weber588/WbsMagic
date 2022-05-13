package wbs.magic.command;

import com.google.common.collect.Table;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.magic.passives.PassiveEffect;
import wbs.magic.passives.PassiveEffectType;
import wbs.magic.wand.MagicWand;
import wbs.magic.wand.SpellBinding;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.*;

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
        Map<Integer, List<SpellBinding>> bindings = wand.bindingMap();
        sendMessage("&m   &r== " + wand.getDisplay() + "&r ==&m   ", sender);
        Set<Integer> tierSet = bindings.keySet();
        boolean showTierInfo = tierSet.size() > 1;
        if (showTierInfo) {
            sendMessage("To change tier, drop your wand &owithout&r shifting!", sender);
        }
        for (int tier : tierSet) {
            List<SpellBinding> tiersBindings = bindings.get(tier);
            if (showTierInfo) {
                sendMessage("&5&m  &r Tier " + tier + " &5&m  ", sender);
            }
            for (SpellBinding binding : tiersBindings) {
                sendMessageNoPrefix(binding.getTrigger() + ": &h" + binding.getSpell().simpleString(), sender);
            }
        }

        Table<EquipmentSlot, PassiveEffectType, PassiveEffect> passives = wand.passivesMap();
        if (passives != null && !passives.isEmpty()) {
            sendMessage("&5&m  &r Passives &5&m  ", sender);

            for (EquipmentSlot slot : passives.rowKeySet()) {
                sendMessage("&h" + WbsEnums.toPrettyString(slot) + ":", sender);
                for (PassiveEffectType effectType : passives.columnKeySet()) {
                    PassiveEffect passive = passives.get(slot, effectType);
                    if (passive != null) {
                        sendMessageNoPrefix(passive.toString().replaceAll("\n", "\n    "), sender);
                    }
                }
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
