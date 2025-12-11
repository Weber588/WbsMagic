package wbs.magic.spells.ranged.targeted;

import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import wbs.magic.SpellCaster;
import wbs.magic.events.SpellCastEvent;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;
import wbs.magic.wand.MagicWand;

import java.util.Collection;

@Spell(name = "Counter Spell",
		description = "The targeted players next spell within a certain amount of time is 'countered', meaning the spell will not take effect, but will still start its cooldown and take mana from the user")
@FailableSpell("If the targeted player does not cast a spell within the duration of counter spell, the effect will fade and no spell will be countered.")
@SpellSound(sound ="entity.vex.charge", pitch = 2, volume = 2)
// Overrides
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 15)
@TargeterOption(optionName = "targeter", defaultRange = 30, entityType = "PLAYER")
public class CounterSpell extends StatusSpell {
	private static final CounterSpellListener LISTENER = new CounterSpellListener();

	public CounterSpell(SpellConfig config, String directory) {
		super(config, directory);

		tryRegisterListener(LISTENER);
	}

	@SuppressWarnings("unused")
	private static class CounterSpellListener implements Listener {
		@EventHandler
		public void castSpellEvent(SpellCastEvent event) {
			SpellCaster caster = event.getCaster();
			Collection<MagicEntityEffect> effects = MagicEntityEffect.getEffects(caster.getPlayer(), CounterSpell.class);
			// Getting a single instance for now, in case more specific counters are added in future
			MagicEntityEffect effect = effects.stream().findAny().orElse(null);
			if (effect != null) {
				MagicWand wand = event.getWand();
				if (wand != null) {
					event.setCancelled(true);

					int cost = event.getSpell().getCost();
					caster.spendMana(cost);
					caster.sendActionBar("&h" + effect.getCaster().getName() + "&r countered your spell! " + caster.manaDisplay(cost));
					caster.setCooldownNow(event.getSpell(), wand);

					effect.remove(true);
				}
			}
		}
	}
}
