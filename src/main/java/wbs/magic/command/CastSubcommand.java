package wbs.magic.command;

import wbs.magic.WbsMagic;
import wbs.magic.controls.EventDetails;
import wbs.magic.events.SpellCastEvent;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.wand.SimpleWandControl;
import wbs.magic.wand.SpellBinding;

public class CastSubcommand extends SpellSubcommand {
    public CastSubcommand(WbsMagic plugin) {
        super(plugin, "cast");
    }

    @Override
    protected void useSpell(SpellCaster caster, SpellInstance instance) {
        EventDetails details = new EventDetails(new SpellCastEvent(caster, instance), caster.getPlayer());
        SpellBinding binding = new SpellBinding(SimpleWandControl.RIGHT_CLICK.toTrigger("Command"), instance);

        instance.cast(new CastingContext(details, binding, caster));
    }
}
