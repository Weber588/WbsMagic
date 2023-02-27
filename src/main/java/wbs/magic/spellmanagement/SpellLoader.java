package wbs.magic.spellmanagement;

import wbs.magic.spells.SpellInstance;

import java.util.List;

public interface SpellLoader {

    /**
     * Get the classes to be loaded.
     * @return Returns the list of registration entries to register
     */
    List<SpellRegistrationEntry<?>> getSpells();

    int getSpellCount();

}
