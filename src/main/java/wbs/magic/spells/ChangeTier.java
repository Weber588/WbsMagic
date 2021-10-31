package wbs.magic.spells;

import org.bukkit.event.Cancellable;
import wbs.magic.SpellCaster;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.wand.MagicWand;

@Spell(name = "Change Tier",
        cost = 0,
        cooldown = 0,
        description = "A spell that increases the player's casting tier, if the wand supports higher tiers."
)
@SpellOption(optionName = "cancel-event", type = SpellOptionType.BOOLEAN, defaultBool = true)
public class ChangeTier extends SpellInstance {
    public ChangeTier(SpellConfig config, String directory) {
        super(config, directory);

        cancelEvent = config.getBoolean("cancel-event");
    }

    private final boolean cancelEvent;

    @Override
    public boolean cast(CastingContext context) {
        SpellCaster caster = context.caster;
        MagicWand wand = context.getWand();

        if (wand == null) {
            caster.sendActionBar("A wand is required when changing tiers.");
            return false;
        }

        caster.nextTier(wand);

        if (cancelEvent && context.eventDetails.event instanceof Cancellable) {
            Cancellable event = (Cancellable) context.eventDetails.event;
            event.setCancelled(true);
        }

        return true;
    }
}
