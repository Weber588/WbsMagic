package wbs.magic.spells.framework;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.SpellCaster;
import wbs.magic.wand.MagicWand;
import wbs.magic.wand.SpellBinding;
import wbs.magic.controls.EventDetails;

public class CastingContext {

    @NotNull
    public final SpellCaster caster;
    @NotNull
    public final EventDetails eventDetails;
    @NotNull
    public final SpellBinding binding;

    @Nullable
    private MagicWand wand;

    @SuppressWarnings("NullableProblems")
    public CastingContext(@NotNull EventDetails eventDetails, @NotNull SpellBinding binding, @NotNull SpellCaster caster) {
        this.eventDetails = eventDetails;
        this.binding = binding;
        this.caster = caster;
    }

    @Nullable
    public MagicWand getWand() {
        return wand;
    }

    public void setWand(@Nullable MagicWand wand) {
        this.wand = wand;
    }
}
