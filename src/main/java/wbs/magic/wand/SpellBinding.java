package wbs.magic.wand;

import org.jetbrains.annotations.NotNull;
import wbs.magic.controls.CastTrigger;
import wbs.magic.spells.SpellInstance;

public class SpellBinding {
    @NotNull
    private final CastTrigger trigger;
    @NotNull
    private final SpellInstance spell;

    public SpellBinding(@NotNull CastTrigger trigger, @NotNull SpellInstance spell) {
        this.trigger = trigger;
        this.spell = spell;

        if (trigger == null) {
            throw new IllegalArgumentException();
        }
    }

    public @NotNull CastTrigger getTrigger() {
        return trigger;
    }

    public @NotNull SpellInstance getSpell() {
        return spell;
    }
}
