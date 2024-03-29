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

public class FullInfoSubcommand extends WbsSubcommand {
    public FullInfoSubcommand(WbsPlugin plugin) {
        super(plugin, "fullinfo");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        int infoTier = 1;
        MagicWand wand = null;

        if (args.length == 1) {
            if (sender instanceof Player) {
                ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
                wand = MagicWand.getWand(item);

                if (wand != null) {
                    if (wand.getMaxTier() == 1) {
                        fullInfo(wand, sender, 1);
                        return true;
                    }
                }
            }

            sendMessage("Usage: &h/" + label + " " + args[0] + " <tier> [wand name]", sender);
            return true;
        }

        try {
            infoTier = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            wand = MagicWand.getWand(args[1]);
            if (wand == null) {
                sendMessage("Invalid tier: " + args[1] + ".", sender);
                return true;
            }
        }

        if (wand == null && args.length >= 3) {
            wand = MagicWand.getWand(args[2]);
        } else {
            if (sender instanceof Player) {
                ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
                wand = MagicWand.getWand(item);
            }
        }

        if (wand == null) {
            sendMessage("Usage: &h/" + label + " " + args[0] + " <tier> [wand name]", sender);
            return true;
        }

        if (infoTier <= 0) {
            sendMessage("That tier is too low. Tiers must be greater than 0.", sender);
            return true;
        }

        if (infoTier > wand.getMaxTier()) {
            sendMessage("That tier is too high. This wand's maximum tier is " + wand.getMaxTier(), sender);
            return true;
        }

        fullInfo(wand, sender, infoTier);
        return true;
    }

    private void fullInfo(MagicWand wand, CommandSender sender, int infoTier) {
        Map<Integer, List<SpellBinding>> bindings = wand.bindingMap();
        sendMessage("&m   &r== " + wand.getDisplay() + "&r ==&m   ", sender);

        List<SpellBinding> tiersBindings = bindings.get(infoTier);
        if (tiersBindings != null) {
            sendMessage("&5&m  &r Tier " + infoTier + " &5&m  ", sender);

            for (SpellBinding binding : tiersBindings) {
                sendMessageNoPrefix(binding.getTrigger() + ": &h"
                                + binding.getSpell().toString().replaceAll("\n", "\n    "),
                        sender);
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
                        sendMessageNoPrefix(passive.toString().replaceAll("\n", "\n&r  - &h"), sender);
                    }
                }
            }
        }
    }

    @Override
    public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        List<String> choices = new LinkedList<>();


        switch (args.length) {
            case 2:
                int maxTier = 3;
                if (sender instanceof Player) {
                    MagicWand wand = MagicWand.getHeldWand((Player) sender);
                    if (wand != null) {
                        maxTier = wand.getMaxTier();
                    }
                }

                for (int i = 1; i <= maxTier; i++) choices.add(i + "");
                break;
            case 3:
                choices.addAll(MagicWand.getWandNames());
                break;
        }

        return choices;
    }
}
