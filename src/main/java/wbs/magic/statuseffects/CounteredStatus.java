package wbs.magic.statuseffects;

import wbs.magic.statuseffects.generics.StatusEffect;
import wbs.magic.wrappers.SpellCaster;

public class CounteredStatus extends StatusEffect {

    public CounteredStatus(SpellCaster caster, int duration) {
        super(caster, duration);
    }

    @Override
    public StatusEffectType getType() {
        return StatusEffectType.COUNTERED;
    }
}
