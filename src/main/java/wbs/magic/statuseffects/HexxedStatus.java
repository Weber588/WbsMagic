package wbs.magic.statuseffects;

import wbs.magic.statuseffects.generics.StatusEffect;
import wbs.magic.wrappers.SpellCaster;

public class HexxedStatus extends StatusEffect {

	public HexxedStatus(StatusEffectType type, SpellCaster caster, int duration) {
		super(type, caster, duration);
	}
	
	private double multiplier = 1.2;
	
	public void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
	}
	
	public double getMultiplier() {
		return multiplier;
	}

}
