package wbs.magic.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import wbs.magic.SpellCaster;
import wbs.magic.events.SpellCastEvent;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spells.Hallucination;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spells.ThrowBlock;
import wbs.magic.spells.ranged.targeted.*;
import wbs.magic.wand.MagicWand;
import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.pluginhooks.WbsRegionUtils;

import java.util.Collection;

@SuppressWarnings("unused")
public class SpellListener extends WbsMessenger implements Listener {

	public SpellListener(WbsPlugin plugin) {
		super(plugin);
	}

	@EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
	public void onMonsterTargetPlayer(EntityTargetEvent event) {
		if (event.getEntity() instanceof Monster) {
			Monster monster = (Monster) event.getEntity();

			if (event.getTarget() instanceof Player) {
				Player target = (Player) event.getTarget();
				String dominated = monster.getPersistentDataContainer().get(DominateMonster.DOMINATE_KEY, PersistentDataType.STRING);
				if (dominated != null) {
					if (target.getName().equalsIgnoreCase(dominated)) {
						event.setCancelled(true);
						event.setTarget(null);
					}
				}

				if (!event.isCancelled()) {
					Collection<MagicEntityEffect> effects = MagicEntityEffect.getEffects(monster);

					for (MagicEntityEffect effect : effects) {
						if (effect.getSpell() instanceof SummonAlly) {
							event.setCancelled(true);
							// Don't need to clear target - entities summoned by SummonAlly are prevented
							// from targeting the caster from spawn time
							break;
						}
					}
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
	public void damageDuringHallucination(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getDamager();

		if (SpellCaster.isRegistered(player)) {
			SpellCaster caster = SpellCaster.getCaster(player);

			SpellInstance conc = caster.getConcentration();
			if (conc != null && conc.getRegisteredSpell().getSpellClass() == Hallucination.class) {
				caster.stopConcentration();
				caster.sendActionBar(conc.getName() + " broken by dealing damage!");
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onFallingBlockSolidify(EntityChangeBlockEvent event) {
		if (event.getEntity() instanceof FallingBlock) {
			FallingBlock block = (FallingBlock) event.getEntity();

			PersistentDataContainer container = block.getPersistentDataContainer();

			String thrownByName = container.get(ThrowBlock.THROWN_BY_KEY, PersistentDataType.STRING);
			if (thrownByName != null) {
				Player player = Bukkit.getPlayer(thrownByName);
				if (player != null) {
					String stickToWallsBool = container.get(ThrowBlock.STICK_TO_WALLS_KEY, PersistentDataType.STRING);
					if (stickToWallsBool != null && stickToWallsBool.equalsIgnoreCase("true")) {
						block.remove();
						event.setCancelled(true);
						return;
					}

					if (!WbsRegionUtils.canBuildAt(event.getBlock().getLocation(), player)) {
						block.remove();
						event.setCancelled(true);
					}
				} else {
					// The player that threw this logged off, cancel.
					block.remove();
					event.setCancelled(true);
				}
			}
		}
	}
}
