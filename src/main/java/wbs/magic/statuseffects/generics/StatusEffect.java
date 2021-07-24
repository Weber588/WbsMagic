package wbs.magic.statuseffects.generics;

import org.bukkit.scheduler.BukkitRunnable;

import wbs.magic.WbsMagic;
import wbs.magic.SpellCaster;

public abstract class StatusEffect {
	
	private static WbsMagic plugin;
	public void setPlugin(WbsMagic plugin) {
		StatusEffect.plugin = plugin;
	}
	
	public enum StatusEffectType {
		COUNTERED, CURSED, BLESSED;
		
		private String description;
		
		static {
			COUNTERED.description = "The next spell this caster uses will be countered "
					+ "and not take effect, but will still trigger cooldown and cost mana.";
		}
		
		public String getDescription() {
			return description;
		}
	}

	private int duration = 20; // in ticks
	private SpellCaster caster = null;
	
	public StatusEffect(SpellCaster caster, int duration) {
		this.caster = caster;
		this.duration = duration;
	}
	
	private StatusEffect thisEffect = this;
	
	public void applyTo(SpellCaster inflicted) {
		inflicted.addStatusEffect(this);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				inflicted.removeStatusEffect(thisEffect);
			}
		}.runTaskLater(plugin, duration);
	}

	public abstract StatusEffectType getType();
	
	public SpellCaster getCaster() {
		return caster;
	}
}
