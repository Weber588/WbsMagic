package wbs.magic.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import wbs.magic.SpellCaster;
import wbs.magic.controls.EventDetails;
import wbs.magic.events.helper.PlayerPunchEvent;
import wbs.magic.events.helper.PlayerRightClickEvent;
import wbs.magic.spellmanagement.configuration.DamageSpell;
import wbs.magic.spells.SpellInstance;
import wbs.magic.wand.MagicWand;
import wbs.magic.wand.SpellBinding;
import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class WandListener extends WbsMessenger implements Listener {

	public WandListener(WbsPlugin plugin) {
		super(plugin);
	}

	private boolean isLookingDown(Player player) {
		return (player.getLocation().getPitch() > 85);
	}

	private void tryCasting(PlayerEvent event) {
		tryCasting(event.getPlayer(), event);
	}

	private void tryCasting(PlayerEvent event, Block block, Entity entity) {
		tryCasting(event.getPlayer(), event, block, entity);
	}

	private void tryCasting(Player player, Event event) {
		tryCasting(player, event, null, null);
	}

	private void tryCasting(Player player, Event event, Block block, Entity entity) {
		ItemStack item = player.getInventory().getItemInMainHand();
		MagicWand wand = MagicWand.getWand(item);
		if (wand != null) {
			SpellCaster caster = SpellCaster.getCaster(player);

			EventDetails details = new EventDetails(event, player);
			if (block != null) details.setBlock(block);
			if (entity != null) details.setOtherEntity(entity);
			SpellBinding binding = wand.tryCasting(caster, details);
		}
	}

	@EventHandler
	public void PunchEvent(PlayerPunchEvent event) {
		tryCasting(event, event.getBlock(), event.getOtherEntity());
	}

	@EventHandler
	public void RightClickEvent(PlayerRightClickEvent event) {
		tryCasting(event, event.getBlock(), event.getOtherEntity());
	}

	@EventHandler
	public void PlayerDropItemEvent(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItemDrop().getItemStack();
		MagicWand wand = MagicWand.getWand(item);
		if (wand != null) {
			SpellCaster caster = SpellCaster.getCaster(player);

			EventDetails details = new EventDetails(event, player);
			SpellBinding binding = wand.tryCasting(caster, details);

			event.setCancelled(event.isCancelled() || wand.preventDrops());
		}
	}

	@EventHandler
	public void BlockBreakEvent(BlockBreakEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		MagicWand wand = MagicWand.getWand(item);

		SpellCaster caster = SpellCaster.getCaster(player);

		if (!caster.isBreaking() && wand != null) {
			if (wand.preventBlockBreaking()) {
				event.setCancelled(true);
			} else {
				EventDetails details = new EventDetails(event, player);
				details.setBlock(event.getBlock());
				SpellBinding binding = wand.tryCasting(caster, details);
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
			if (wand.preventBlockPlacing()) {
				event.setCancelled(true);
			} else {
				SpellCaster caster = SpellCaster.getCaster(player);

				EventDetails details = new EventDetails(event, player);
				details.setBlock(event.getBlock());
				SpellBinding binding = wand.tryCasting(caster, details);
			}
		}
	}

	@EventHandler
	public void PlayerToggleSneakEvent(PlayerToggleSneakEvent event) {
		if (event.isSneaking()) {
			tryCasting(event);
		}
	}

	@EventHandler
	public void PlayerDeathEvent(PlayerDeathEvent event) {
		tryCasting(event.getEntity(), event);
	}

	@EventHandler
	public void PlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
		tryCasting(event);
	}

	@EventHandler
	public void PlayerFishEvent(PlayerFishEvent event) {
		if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
			tryCasting(event);
		}
	}

	@EventHandler
	public void PlayerRiptideEvent(PlayerRiptideEvent event) {
		tryCasting(event);
	}

	@EventHandler
	public void PlayerItemBreakEvent(PlayerItemBreakEvent event) {
		tryCasting(event);
	}

	@EventHandler
	public void PlayerToggleSprintEvent(PlayerToggleSprintEvent event) {
		tryCasting(event);
	}

	@EventHandler
	public void ProjectileLaunchEvent(ProjectileLaunchEvent event) {
		Projectile proj = event.getEntity();

		if (proj.getShooter() instanceof Player) {
			tryCasting((Player) proj.getShooter(), event, null, proj);
		}
	}

	//==============================//
	/*             Misc             */
	//==============================//

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

	//==============================//
	//     Death Message Handlers   //
	//==============================//

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
		String deathMessage;

		if (victim.equals(attacker)) {
			deathMessage = spell.suicideFormat().replaceAll("%player%", victim.getName());
		} else {
			deathMessage = spell.deathFormat().replaceAll("%victim%", victim.getName());
			deathMessage = deathMessage.replaceAll("%attacker%", attacker.getName());
		}

		return deathMessage;
	}
}