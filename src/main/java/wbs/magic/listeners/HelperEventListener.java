package wbs.magic.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.magic.SpellCaster;
import wbs.magic.WbsMagic;
import wbs.magic.events.helper.PlayerPunchEvent;
import wbs.magic.events.helper.PlayerRightClickEvent;
import wbs.magic.spells.SpellInstance;
import wbs.magic.wand.MagicWand;

import java.util.HashSet;
import java.util.Set;

/**
 * Calls helper events to contain similar events
 */
public class HelperEventListener implements Listener {

    @EventHandler
    public void onRightClickOnEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();

        boolean targetableEntity = SpellInstance.VALID_TARGETS_PREDICATE.test(event.getRightClicked());
        Entity rightClicked = targetableEntity ? event.getRightClicked() : null;

        PlayerRightClickEvent rightClickEvent = new PlayerRightClickEvent(event, player, null, rightClicked);
        rightClickEvent.autoCall();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeftClickOnEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();

            ItemStack item = player.getInventory().getItemInMainHand();
            MagicWand wand = MagicWand.getWand(item);

            if (wand != null) {
                if (SpellCaster.isRegistered(player)) {
                    SpellCaster caster = SpellCaster.getCaster(player);

                    if (!caster.isDealingSpellDamage()) {
                        boolean targetableEntity = SpellInstance.VALID_TARGETS_PREDICATE.test(event.getEntity());
                        Entity leftClicked = targetableEntity ? event.getEntity() : null;

                        PlayerPunchEvent punchEvent = new PlayerPunchEvent(event, player, null, leftClicked);
                        punchEvent.autoCall();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        droppedItemsThisTick.add(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                droppedItemsThisTick.remove(player);
            }
        }.runTaskLater(WbsMagic.getInstance(), 1);
    }

    private final Set<Player> droppedItemsThisTick = new HashSet<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        MagicWand wand = MagicWand.getWand(item);

        if (MagicWand.isExpiredWand(item)) {
            player.getInventory().setItemInMainHand(null);
            WbsMagic.getInstance().sendMessage("&wWand has expired.", player);
            return;
        }

        if (event.getHand() == EquipmentSlot.HAND) {
            if (wand != null) {
                if (droppedItemsThisTick.contains(player)) {
                    return;
                }

                Action action = event.getAction();

                switch (action) {
                    case RIGHT_CLICK_AIR:
                    case RIGHT_CLICK_BLOCK:
                        PlayerRightClickEvent rightClickEvent = new PlayerRightClickEvent(event, player, event.getClickedBlock(), null);

                        rightClickEvent.autoCall();
                        break;
                    case LEFT_CLICK_AIR:
                    case LEFT_CLICK_BLOCK:
                        PlayerPunchEvent punchEvent = new PlayerPunchEvent(event, player, event.getClickedBlock(), null);

                        punchEvent.autoCall();
                        break;
                }
            }
        }
    }
}
