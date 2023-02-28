package wbs.magic.listeners;

import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import wbs.magic.events.SpellCastEvent;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.objects.MagicEntityMarker;
import wbs.magic.spells.Hallucination;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spells.ThrowBlock;
import wbs.magic.spells.ranged.targeted.DivineShield;
import wbs.magic.spells.ranged.targeted.DominateMonster;
import wbs.magic.spells.ranged.targeted.PlanarBinding;
import wbs.magic.statuseffects.generics.StatusEffect;
import wbs.magic.statuseffects.generics.StatusEffect.StatusEffectType;
import wbs.magic.wand.MagicWand;
import wbs.magic.SpellCaster;

import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.pluginhooks.WbsRegionUtils;

@SuppressWarnings("unused")
public class SpellListener extends WbsMessenger implements Listener {

	public SpellListener(WbsPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void castSpellEvent(SpellCastEvent event) {
		SpellCaster caster = event.getCaster();
		List<StatusEffect> effects = caster.getStatusEffect(StatusEffectType.COUNTERED);
		if (!effects.isEmpty()) {
			MagicWand wand = event.getWand();
			if (wand != null) {
				StatusEffect status = effects.get(0);
				event.setCancelled(true);

				int cost = event.getSpell().getCost();
				caster.spendMana(cost);
				caster.sendActionBar("&h" + status.getCaster().getName() + " &rcountered your spell! " + caster.manaDisplay(cost));
				caster.setCooldownNow(event.getSpell(), wand);

				caster.removeStatusEffect(status);
			}
		}
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
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
	public void damageDivineShield(EntityDamageByEntityEvent event) {
		PersistentDataContainer container = event.getEntity().getPersistentDataContainer();

		if (container.get(DivineShield.DIVINE_SHIELD_KEY, PersistentDataType.STRING) != null) {
			event.setCancelled(true);
			container.remove(DivineShield.DIVINE_SHIELD_KEY);
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
	public void teleportPlanarBinding(EntityTeleportEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof LivingEntity)) {
			return;
		}

		Collection<MagicEntityEffect> effects = MagicEntityEffect.getEffects(entity);

		for (MagicEntityEffect effect : effects) {
			if (effect.getSpell() instanceof PlanarBinding) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
	public void teleportPlanarBinding(PlayerTeleportEvent event) {
		Player player = event.getPlayer();

		SpellCaster eventCaster = null;

		TeleportCause cause = event.getCause();
		switch (cause) {
			case ENDER_PEARL:
			case CHORUS_FRUIT:
			case PLUGIN:
				break;
			default:
				return;
		}

		if (SpellCaster.isRegistered(player)) {
			eventCaster = SpellCaster.getCaster(player);

			if (cause == TeleportCause.PLUGIN && !eventCaster.isMagicTeleporting()) {
				// Some other non-magical teleport - don't try to cancel it
				return;
			}
		}

		Collection<MagicEntityEffect> effects = MagicEntityEffect.getEffects(player);

		for (MagicEntityEffect effect : effects) {
			if (effect.getSpell() instanceof PlanarBinding) {
				event.setCancelled(true);

				if (eventCaster != null) {
					eventCaster.sendActionBar(effect.caster.getName() + "'s &h" + effect.getSpell().getName()
							+ "&r prevents you from teleporting!");
				}
				break;
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
