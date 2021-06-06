package wbs.magic.controllers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import wbs.magic.annotations.DamageSpell;
import wbs.magic.enums.WandControl;
import wbs.magic.passives.DamageImmunityPassive;
import wbs.magic.passives.DamageResistancePassive;
import wbs.magic.passives.PassiveEffect;
import wbs.magic.passives.PassiveEffectType;
import wbs.magic.spellinstances.SpellInstance;
import wbs.magic.wrappers.MagicWand;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.plugin.WbsPlugin;

public class WandController extends WbsMessenger implements Listener {
	
	public WandController(WbsPlugin plugin) {
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
	
	/**
	 * Due to the addition of an arm swing animation in 1.15,
	 * the LEFT_CLICK_AIR event is no longer reliably *truly*
	 * a left click air event - it may be an item drop as well.
	 * See {@link #onInteract(PlayerInteractEvent)} for the drop item filter.
	 * It is worth noting that this event, while reliable, happens 1 tick after
	 * the original event is fired.
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
	 * @param event The event to prepare
	 * @return false if the even is safe to fire right now, true if the event
	 * was left click air event.
	 */
	private boolean checkLeftClickEvent(PlayerInteractEvent event) {
		if(event.getAction() != Action.LEFT_CLICK_AIR) {
			return false;
		}
		
		long timestamp = System.currentTimeMillis();
		
		Player player = event.getPlayer();
		
		Listener listener = new Listener() {
				@EventHandler
				public void onDrop(PlayerDropItemEvent e) {
					if(!(e.getPlayer().getUniqueId().equals(player.getUniqueId())))
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

				boolean cancel = false;
				
				Action action = event.getAction();
				switch (action) {
				case RIGHT_CLICK_AIR:
				case RIGHT_CLICK_BLOCK:
					if (!player.isSneaking()) {
						if (isLookingDown(player)) {
							cancel = caster.castSpell(WandControl.RIGHT_CLICK_DOWN, wand);
						} else {
							cancel = caster.castSpell(WandControl.RIGHT_CLICK, wand);
						}
					} else {
						if (isLookingDown(player)) {
							cancel = caster.castSpell(WandControl.SHIFT_RIGHT_CLICK_DOWN, wand);
						} else {
							cancel = caster.castSpell(WandControl.SHIFT_RIGHT_CLICK, wand);
						}
					}
					break;
				case LEFT_CLICK_AIR:
				case LEFT_CLICK_BLOCK:
					if (!player.isSneaking()) {
						if (isLookingDown(player)) {
							cancel = caster.castSpell(WandControl.PUNCH_DOWN, wand);
						} else {
							cancel = caster.castSpell(WandControl.PUNCH, wand);
						}
					} else {
						if (isLookingDown(player)) {
							cancel = caster.castSpell(WandControl.SHIFT_PUNCH_DOWN, wand);
						} else {
							cancel = caster.castSpell(WandControl.SHIFT_PUNCH, wand);
						}
					}
					break;
				default: break;
				}
				
				event.setCancelled(cancel);
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

	@EventHandler(priority=EventPriority.LOWEST)
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
						
						if (!player.isSneaking()) {
							if (targetableEntity) {
								cancel = caster.castSpell(WandControl.PUNCH_ENTITY, wand);
							} else {
								cancel = caster.castSpell(WandControl.PUNCH, wand);
							}
						} else {
							if (targetableEntity) {
								cancel = caster.castSpell(WandControl.SHIFT_PUNCH_ENTITY, wand);
							} else {
								cancel = caster.castSpell(WandControl.SHIFT_PUNCH, wand);
							}
						}
						
						event.setCancelled(cancel);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlaceWand(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		MagicWand wand = MagicWand.getWand(item);
		if (wand != null) {
			event.setCancelled(true);
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
	
	@EventHandler(priority=EventPriority.LOWEST)
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
	
	
	@EventHandler(ignoreCancelled=true)
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
			event.setCancelled(true);
		}
	}
}
