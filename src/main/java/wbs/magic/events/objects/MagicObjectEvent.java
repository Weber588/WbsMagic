package wbs.magic.events.objects;

import wbs.magic.events.SpellEvent;
import wbs.magic.objects.generics.MagicObject;

public abstract class MagicObjectEvent extends SpellEvent {
    protected final MagicObject magicObject;

    public MagicObjectEvent(MagicObject magicObject) {
        super(magicObject.caster, magicObject.castingSpell);
        this.magicObject = magicObject;
    }

    public MagicObject getMagicObject() {
        return magicObject;
    }
}
