package wbs.magic.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import wbs.magic.spellmanagement.configuration.DamageSpell;
import wbs.magic.wand.WandControl;
import wbs.magic.passives.DamageImmunityPassive;
import wbs.magic.passives.DamageResistancePassive;
import wbs.magic.passives.PassiveEffect;
import wbs.magic.passives.PassiveEffectType;
import wbs.magic.spells.SpellInstance;
import wbs.magic.wand.MagicWand;
import wbs.magic.SpellCaster;

import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.plugin.WbsPlugin;

@SuppressWarnings("unused")
public class WandListener extends WbsMessenger implements Listener {

	public WandListener(WbsPlugin plugin) {
		super(plugin);
	}

	private boolean isLookingDown(Player player) {
		return (player.getLocation().getPitch() > 85);
	}

	@EventHandler
	public void PlayerDropItemEvent(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItemDrop().getItemStack();
		MagicWand wand = MagicWand.getWand(item);
		if (wand != null) {
			SpellCaster caster = SpellCaster.getCaster(player);
			if (!player.isSneaking()) {
				if (caster.nextTier(wand)) {
					event.setCancelled(true);
				}
			} else {
				if (isLookingDown(player)) {
					caster.castSpell(WandControl.SHIFT_DROP_DOWN, wand);
				} else {
					caster.castSpell(WandControl.SHIFT_DROP, wand);
				}

				event.setCancelled(true);
			}

			event.setCancelled(event.isCancelled() || wand.cancelDrops());
		}
	}

	@EventHandler(ignoreCancelled=true,priority=EventPriority.HIGHEST)
	public void onItemChange(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();

		int oldSlot = event.getPreviousSlot();

		ItemStack item = player.getInventory().getItem(oldSlot);
		if (item == null) {
			return;
		}
		MagicWand wand = MagicWand.getWand(item);

		if (wand != null) {
			if (SpellCaster.isRegistered(player)) {
				SpellCaster caster = SpellCaster.getCaster(player);

				if (caster.isCasting()) {
					caster.stopCasting();
					sendActionBar("You let go of your wand!", player);
				}

				if (caster.isConcentrating()) {
					caster.stopConcentration();
					sendActionBar("You let go of your wand!", player);
				}
			}
		}
	}

	/**
	 * Due to the addition of an arm swing animation in 1.15,
	 * the LEFT_CLICK_AIR event is no longer reliably *truly*
	 * a left click air event - it may be an item drop as well.
	 * See {@link #onInteract(PlayerInteractEvent)} for the drop item filter.
	 * It is worth noting that this event, while reliable, happens 1 tick after
	 * the original event is fired.
	 *
	 * @param event The original event, fired last tick (cannot be cancelled)
	 */
	private void playerLeftClickEventSafe(PlayerInteractEvent event) {
		// Guaranteed wand in hand
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		MagicWand wand = MagicWand.getWand(item);

		SpellCaster caster = SpellCaster.getCaster(player);

		if (!player.isSneaking()) {
			if (isLookingDown(player)) {
				caster.castSpell(WandControl.PUNCH_DOWN, wand);
			} else {
				caster.castSpell(WandControl.PUNCH, wand);
			}
		} else {
			if (isLookingDown(player)) {
				caster.castSpell(WandControl.SHIFT_PUNCH_DOWN, wand);
			} else {
				caster.castSpell(WandControl.SHIFT_PUNCH, wand);
			}
		}
	}

	// A table of a runnables, indexed by player creating them, and the timestamp of creation.
	private final Table<Player, Long, BukkitRunnable> tasks = HashBasedTable.create();

	/**
	 * Logic by geekles on spigotmc.org, edits by Weber588
	 *
	 * @param event The event to prepare
	 * @return false if the even is safe to fire right now, true if the event
	 * was left click air event.
	 */
	private boolean checkLeftClickEvent(PlayerInteractEvent event) {
		if (event.getAction() != Action.LEFT_CLICK_AIR) {
			return false;
		}

		long timestamp = System.currentTimeMillis();

		Player player = event.getPlayer();

		Listener listener = new Listener() {
			@EventHandler
			public void onDrop(PlayerDropItemEvent e) {
				if (!(e.getPlayer().getUniqueId().equals(player.getUniqueId())))
					return;
				BukkitRunnable task = tasks.get(player, timestamp);
				tasks.remove(player, timestamp);
				task.cancel();

				// Item drop handled elsewhere
			}
		};

		BukkitRunnable task = new BukkitRunnable() {
			@Override
			public void run() {
				HandlerList.unregisterAll(listener);

				playerLeftClickEventSafe(event);
				tasks.remove(player, timestamp);
			}
		};
		task.runTaskLater(plugin, 1);
		tasks.put(player, timestamp, task);

		Bukkit.getPluginManager().registerEvents(listener, plugin);

		return true;
	}

	/**
	 * Catch the drop event to fix LEFT_CLICK_AIR, incase this packet
	 * comes in first
	 *
	 * @param event
	 */
	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		droppedItemsThisTick.add(player);
		new BukkitRunnable() {
			@Override
			public void run() {
				droppedItemsThisTick.remove(player);
			}
		}.runTaskLater(plugin, 1);
	}

	private final Set<Player> droppedItemsThisTick = new HashSet<>();

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		MagicWand wand = MagicWand.getWand(item);
		if (event.getHand() == EquipmentSlot.HAND) {
			if (wand != null) {
				if (droppedItemsThisTick.contains(player)) {
					return;
				}/* else {
					if (checkLeftClickEvent(event)) {
						return;
					}
				} */

				SpellCaster caster = SpellCaster.getCaster(player);

				WandControl control = null;
				Action action = event.getAction();

				// Allow right clicking chests, doors, levers etc
				if ((action == Action.RIGHT_CLICK_BLOCK ||
						action == Action.LEFT_CLICK_BLOCK) &&
						!player.isSneaking())
				{
					Block block = event.getClickedBlock();
					assert block != null;
					if (block.getType().isInteractable()) {
						// Stairs are in this list for some reason
						Tag<Material> stairTag = Tag.STAIRS;
						if (!stairTag.isTagged(block.getType())) {
							return;
						}
					}
				}

				switch (action) {
					case RIGHT_CLICK_AIR:
					case RIGHT_CLICK_BLOCK:
						if (!player.isSneaking()) {
							if (isLookingDown(player)) {
								control = WandControl.RIGHT_CLICK_DOWN;
							} else {
								control = WandControl.RIGHT_CLICK;
							}
						} else {
							if (isLookingDown(player)) {
								control = WandControl.SHIFT_RIGHT_CLICK_DOWN;
							} else {
								control = WandControl.SHIFT_RIGHT_CLICK;
							}
						}
						break;
					case LEFT_CLICK_AIR:
					case LEFT_CLICK_BLOCK:
						if (!player.isSneaking()) {
							if (isLookingDown(player)) {
								control = WandControl.PUNCH_DOWN;
							} else {
								control = WandControl.PUNCH;
							}
						} else {
							if (isLookingDown(player)) {
								control = WandControl.SHIFT_PUNCH_DOWN;
							} else {
								control = WandControl.SHIFT_PUNCH;
							}
						}
						break;
					default:
						break;
				}

				boolean tryToCast = wand.hasSimplifiedBinding(caster.getTier(), control);

				if (tryToCast) {
					caster.castSpell(control, wand);

					event.setCancelled(true);
				}
			}
		}
	}

	/********************************/
	/*           Passives           */

	/********************************/

	@EventHandler
	public void EntityDamageEvent(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			ItemStack item = player.getInventory().getItemInMainHand();
			MagicWand wand = MagicWand.getWand(item);

			if (wand != null) {
				if (event.getEntity() instanceof Player) {
					DamageCause cause = event.getCause();

					Map<PassiveEffectType, PassiveEffect> passives = wand.passivesMap();

					PassiveEffectType type = PassiveEffectType.DAMAGE_IMMUNITY;
					if (passives.containsKey(type)) {
						DamageImmunityPassive immunityPassive = (DamageImmunityPassive) passives.get(type);

						Map<DamageCause, Double> immunityMap = immunityPassive.getImmunities();

						if (immunityMap.containsKey(cause)) {
							event.setCancelled(true);
						}
					}

					type = PassiveEffectType.DAMAGE_RESISTANCE;
					if (passives.containsKey(type)) {
						DamageResistancePassive resistancePassive = (DamageResistancePassive) passives.get(type);

						Map<DamageCause, Double> resistanceMap = resistancePassive.getResistances();

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

					boolean targetableEntity = (event.getEntity() instanceof LivingEntity);

					if (!caster.isDealingSpellDamage()) {
						boolean cancel = false;

						WandControl control;
						if (!player.isSneaking()) {
							if (targetableEntity) {
								control = WandControl.PUNCH_ENTITY;
							} else {
								control = WandControl.PUNCH;
							}
						} else {
							if (targetableEntity) {
								control = WandControl.SHIFT_PUNCH_ENTITY;
							} else {
								control = WandControl.SHIFT_PUNCH;
							}
						}

						boolean tryToCast = wand.hasSimplifiedBinding(caster.getTier(), control);

						if (tryToCast) {
							caster.castSpell(control, wand);

							if (!wand.allowCombat()) {
								event.setCancelled(true);
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlaceWand(BlockPlaceEvent event) {
		Player player = event.getPlayer();

		ItemStack item = null;

		switch (event.getHand()) {
			case HAND:
				item = player.getInventory().getItemInMainHand();
				break;
			case OFF_HAND:
				item = player.getInventory().getItemInOffHand();
				break;
		}
		
		if (item == null) return;

		MagicWand wand = MagicWand.getWand(item);
		if (wand != null) {
			if (!wand.allowBlockPlacing()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onRightClickOnEntity(PlayerInteractEntityEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) {
			return;
		}

		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		MagicWand wand = MagicWand.getWand(item);
		if (wand != null) {
			SpellCaster caster = SpellCaster.getCaster(player);

			if (!caster.isDealingSpellDamage()) {
				event.setCancelled(true);

				boolean targetableEntity = (event.getRightClicked() instanceof LivingEntity);

				WandControl control;
				if (!player.isSneaking()) {
					if (targetableEntity) {
						control = WandControl.RIGHT_CLICK_ENTITY;
					} else {
						control = WandControl.RIGHT_CLICK;
					}
				} else {
					if (targetableEntity) {
						control = WandControl.SHIFT_RIGHT_CLICK_ENTITY;
					} else {
						control = WandControl.SHIFT_RIGHT_CLICK;
					}
				}
				if (control.isEntity()) {
					LivingEntity clicked = (LivingEntity) event.getRightClicked();

					caster.castSpellOn(control, wand, clicked);
				} else {
					caster.castSpell(control, wand);
				}
			}
		}
	}

	/*********************************/
	/***** Death Message Handlers ****/
	/*********************************/

	private final Map<Player, SpellInstance> lastSpellDamage = new HashMap<>();
	// Victim to Attacker
	private final Map<Player, Player> lastSpellDamager = new HashMap<>();

	@EventHandler
	public void onNonEntityDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		Player victim = (Player) event.getEntity();

		switch (event.getCause()) {
			case ENTITY_ATTACK:
			case ENTITY_SWEEP_ATTACK:
				return;
			default:
				lastSpellDamage.remove(victim);
				lastSpellDamager.remove(victim);
				return;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDamageWithSpell(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		if (!(event.getDamager() instanceof Player)) {
			return;
		}

		Player attacker = (Player) event.getDamager();
		if (!SpellCaster.isRegistered(attacker)) {
			return;
		}

		Player victim = (Player) event.getEntity();

		SpellCaster attackingCaster = SpellCaster.getCaster(attacker);

		if (attackingCaster.isDealingSpellDamage()) {
			lastSpellDamage.put(victim, attackingCaster.getCurrentDamageSpell());
			lastSpellDamager.put(victim, attacker);
		} else {
			lastSpellDamage.remove(victim);
			lastSpellDamager.remove(victim);
		}
	}


	@EventHandler(ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player victim = event.getEntity();
		if (lastSpellDamage.containsKey(victim)) {
			SpellInstance spell = lastSpellDamage.get(victim);
			Player spellDamager = lastSpellDamager.get(victim);
			// Only damage spells can deal damage, so getDamageSpell is always nonnull
			DamageSpell damageSpell = spell.getRegisteredSpell().getDamageSpell();
			event.setDeathMessage(getDeathMessage(damageSpell, victim, spellDamager));
		}
	}

	private String getDeathMessage(DamageSpell spell, Player victim, Player attacker) {
		String deathMessage = null;

		if (victim.equals(attacker)) {
			deathMessage = spell.suicideFormat().replaceAll("%player%", victim.getName());
		} else {
			deathMessage = spell.deathFormat().replaceAll("%victim%", victim.getName());
			deathMessage = deathMessage.replaceAll("%attacker%", attacker.getName());
		}

		return deathMessage;
	}

	@EventHandler
	public void BlockBreakEvent(BlockBreakEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		MagicWand wand = MagicWand.getWand(item);
		if (wand != null) {
			if (!wand.allowBlockBreaking()) {
				event.setCancelled(true);
			}
		}
	}
}