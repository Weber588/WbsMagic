package wbs.magic.spells;

import wbs.magic.spellinstances.SpellInstance;

import java.util.Collection;

public interface SpellLoader {

    /**
     * Get the classes to be loaded.
     * @return
     */
    Collection<Class<? extends SpellInstance>> getSpells();

    int getSpellCount();

}
