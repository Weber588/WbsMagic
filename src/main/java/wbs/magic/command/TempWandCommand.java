package wbs.magic.command;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.magic.SpellCaster;
import wbs.magic.WbsMagic;
import wbs.magic.spells.SpellInstance;
import wbs.magic.wand.MagicWand;
import wbs.magic.wand.SimpleWandControl;
import wbs.magic.wand.SpellBinding;

import java.time.LocalDate;
import java.util.Arrays;

public class TempWandCommand extends SpellSubcommand {
    public TempWandCommand(@NotNull WbsMagic plugin) {
        super(plugin, "tempwand");

        ignoreOptions.clear();
    }

    @Override
    protected void useSpell(SpellCaster caster, SpellInstance instance) {
        Player player = caster.getPlayer();

        Material material = player.getInventory().getItemInMainHand().getType();

        if (material.isAir()) material = Material.STICK;

        MagicWand tempWand =
                new MagicWand("temp:" + caster.getName() + ":" + instance.getName(),
                        plugin.dynamicColourise("&5&lTemp: &e" + instance.getName()),
                        material);

        tempWand.setPermission(getPermission());
        tempWand.setShiny(true);

        tempWand.setLore(plugin.colouriseAll(
                Arrays.asList(
                        "&7Temporary wand.",
                        "&6Spell: &b" + instance.getName(),
                        "&6Created by: &b" + caster.getName(),
                        "&6Created on: &b" + LocalDate.now()
                )
        ));

        tempWand.addSpell(1, new SpellBinding(SimpleWandControl.RIGHT_CLICK.toTrigger("Internal"), instance));

        ItemStack item = tempWand.buildNewWand();
        if (instance.consumeWand()) {
            item.setAmount(item.getType().getMaxStackSize());
        }

        caster.getPlayer().getInventory().addItem(item);
        sendMessage("Created temp wand for spell &h" + instance.getName() +
                "&r. This wand will expire on the next reload.", caster.getPlayer());
    }
}
