package wbs.magic.controllers;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import wbs.magic.events.SpellCastEvent;
import wbs.magic.statuseffects.generics.StatusEffect;
import wbs.magic.statuseffects.generics.StatusEffect.StatusEffectType;
import wbs.magic.wrappers.MagicWand;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.plugin.WbsPlugin;

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
	public void damageDivineShield(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		Player victim = (Player) event.getEntity();
		if (SpellCaster.isRegistered(victim)) {
			SpellCaster victimCaster = SpellCaster.getCaster(victim);

			List<StatusEffect> effects = victimCaster.getStatusEffect(StatusEffectType.DIVINE_SHIELD);

			if (!effects.isEmpty()) {
				victimCaster.removeStatusEffect(effects.get(0));
				
				event.setCancelled(true);
			}
		}
	}
}
