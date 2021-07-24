package wbs.magic.statuseffects;

import wbs.magic.statuseffects.generics.StatusEffect;
import wbs.magic.SpellCaster;

public class HexxedStatus extends StatusEffect {

	public HexxedStatus(SpellCaster caster, int duration) {
		super(caster, duration);
	}

	@Override
	public StatusEffectType getType() {
		return null;
	}

	private double multiplier = 1.2;
	
	public void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
	}
	
	public double getMultiplier() {
		return multiplier;
	}

}
