package wbs.magic.controllers;

import java.util.List;

import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import wbs.magic.events.SpellCastEvent;
import wbs.magic.spellinstances.Hallucination;
import wbs.magic.spellinstances.SpellInstance;
import wbs.magic.spellinstances.ranged.targeted.DivineShield;
import wbs.magic.spellinstances.ranged.targeted.DominateMonster;
import wbs.magic.statuseffects.generics.StatusEffect;
import wbs.magic.statuseffects.generics.StatusEffect.StatusEffectType;
import wbs.magic.wrappers.MagicWand;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.plugin.WbsPlugin;

@SuppressWarnings("unused")
public class SpellController extends WbsMessenger implements Listener {

	public SpellController(WbsPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void castSpellEvent(SpellCastEvent event) {
		SpellCaster caster = event.getCaster();
		List<StatusEffect> effects = caster.getStatusEffect(StatusEffectType.COUNTERED);
		if (!effects.isEmpty()) {
			StatusEffect status = effects.get(0);
			event.setCancelled(true);

			int cost = event.getSpell().getCost();
			caster.spendMana(cost);
			caster.sendActionBar("&h" + status.getCaster().getName() + " &rcountered your spell! " + caster.manaDisplay(cost));
			caster.setCooldownNow(event.getSpell(), MagicWand.getWand(caster.getPlayer().getInventory().getItemInMainHand()));
			
			caster.removeStatusEffect(status);
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

}
