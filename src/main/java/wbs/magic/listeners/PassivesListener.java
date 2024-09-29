package wbs.magic.listeners;

import java.util.*;

import com.google.common.collect.Table;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import wbs.magic.MagicSettings;
import wbs.magic.passives.*;
import wbs.magic.wand.MagicWand;

import wbs.utils.util.WbsEntities;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.entities.WbsPlayerUtil;
import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.plugin.WbsPlugin;

public class PassivesListener extends WbsMessenger implements Listener {

	public PassivesListener(WbsPlugin plugin) {
		super(plugin);
	}
	
	/**
	 * Start timers for each type of passive given a wand and player,
	 * self-canceling when the wand is no longer in a valid slot
	 * @param wand The wand whose passives are to be used
	 * @param player The player to run the passives on
	 * @param item The wand item
	 */
	private void startPassiveTimers(MagicWand wand, Player player, ItemStack item, EquipmentSlot slot) {
		Map<PassiveEffectType, PassiveEffect> passives = wand.passivesMap().row(slot);

		MagicSettings settings = MagicSettings.getInstance();
		int passivesRefreshRate = settings.getPassiveRefreshRate();

		List<TimedPassiveEffect> timedEffects = new LinkedList<>();
		for (PassiveEffect effect : passives.values()) {
			if (effect instanceof TimedPassiveEffect) {
				timedEffects.add((TimedPassiveEffect) effect);
			}
		}

		if (!timedEffects.isEmpty()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					if (!Objects.equals(WbsEntityUtil.getItemInSlot(player, slot), item)) {
						timedEffects.forEach(effect -> effect.onStop(wand, player, item, slot));
						cancel();

					} else {
						timedEffects.forEach(effect -> effect.onTick(wand, player, item, slot));
					}
				}
			}.runTaskTimer(plugin, 0, passivesRefreshRate);
		}
	}

	@EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
	public void onItemChange(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		
		int newSlot = event.getNewSlot();
		
		ItemStack item = player.getInventory().getItem(newSlot);
		if (item == null) {
			return;
		}
		MagicWand wand = MagicWand.getWand(item);
		
		if (wand != null) {
			startPassiveTimers(wand, player, item, EquipmentSlot.HAND);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			
			ItemStack item;
			MagicWand wand;
			
			for (EquipmentSlot slot : EquipmentSlot.values()) {
				item = WbsEntityUtil.getItemInSlot(player, slot);
				if (item != null) {
					wand = MagicWand.getWand(item);

					if (wand != null) {
						startPassiveTimers(wand, player, item, slot);
						return;
					}
				}
			}
		}
	}

	@EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
	public void onSwapItems(PlayerSwapHandItemsEvent event) {
		Player player = event.getPlayer();
		
		ItemStack item = event.getOffHandItem();
		if (item != null) {
			MagicWand wand = MagicWand.getWand(item);

			if (wand != null) {
				startPassiveTimers(wand, player, item, EquipmentSlot.OFF_HAND);
			}
		}

		item = event.getMainHandItem();
		if (item != null) {
			MagicWand wand = MagicWand.getWand(item);

			if (wand != null) {
				startPassiveTimers(wand, player, item, EquipmentSlot.HAND);
			}
		}
	}

	@EventHandler(ignoreCancelled=true,priority=EventPriority.MONITOR)
	public void onItemPickup(EntityPickupItemEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getEntity();
		ItemStack item = event.getItem().getItemStack();
		MagicWand wand = MagicWand.getWand(item);

		if (wand != null) {
			new BukkitRunnable() {
				@Override
				public void run() {
					if (item.equals(player.getInventory().getItemInMainHand())) {
						startPassiveTimers(wand, player, item, EquipmentSlot.HAND);
					}
				}
			}.runTask(plugin);
		}
	}

	@EventHandler
	public void EntityDamageEvent(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();

			for (EquipmentSlot slot : EquipmentSlot.values()) {
				ItemStack item = WbsEntityUtil.getItemInSlot(player, slot);
				if (item == null) continue;
				MagicWand wand = MagicWand.getWand(item);
				if (wand != null) {
					checkPassiveOnDamage(event, wand, slot);
					if (event.isCancelled()) break;
				}
			}
		}
	}

	private void checkPassiveOnDamage(EntityDamageEvent event, MagicWand wand, EquipmentSlot slot) {
		if (event.getEntity() instanceof Player) {
			EntityDamageEvent.DamageCause cause = event.getCause();

			Table<EquipmentSlot, PassiveEffectType, PassiveEffect> passives = wand.passivesMap();

			if (passives.contains(slot, PassiveEffectType.DAMAGE_IMMUNITY)) {
				DamageImmunityPassive immunityPassive = (DamageImmunityPassive) passives.get(slot, PassiveEffectType.DAMAGE_IMMUNITY);

				Map<EntityDamageEvent.DamageCause, Double> immunityMap = immunityPassive.getImmunities();

				if (immunityMap.containsKey(cause)) {
					event.setCancelled(true);
				}
			}

			if (passives.contains(slot, PassiveEffectType.DAMAGE_RESISTANCE)) {
				DamageResistancePassive resistancePassive = (DamageResistancePassive) passives.get(slot, PassiveEffectType.DAMAGE_RESISTANCE);

				Map<EntityDamageEvent.DamageCause, Double> resistanceMap = resistancePassive.getResistances();

				if (resistanceMap.containsKey(cause)) {
					double reduction = resistanceMap.get(cause);
					if (reduction != 0) {
						event.setDamage(event.getDamage() * 100 / reduction);
					}
				}
			}
		}
	}
}
