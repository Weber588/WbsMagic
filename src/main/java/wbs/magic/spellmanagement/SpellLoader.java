package wbs.magic.spellmanagement;

import wbs.magic.spells.SpellInstance;

import java.util.List;

public interface SpellLoader {

    /**
     * Get the classes to be loaded.
     * @return
     */
    List<Class<? extends SpellInstance>> getSpells();

    int getSpellCount();

}
