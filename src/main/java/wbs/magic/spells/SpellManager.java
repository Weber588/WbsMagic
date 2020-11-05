package wbs.magic.spells;

import wbs.magic.WbsMagic;
import wbs.magic.annotations.Spell;
import wbs.magic.spellinstances.SpellInstance;
import wbs.utils.util.string.WbsStrings;

import java.util.*;

public final class SpellManager {
    private SpellManager() {}

    private static final Map<String, RegisteredSpell> registeredSpells = new HashMap<>();

    public static void clear() {
        registeredSpells.clear();
    }

    public static int registerClasses(SpellLoader loader) {
        return registerClasses(loader.getSpells());
    }

    /**
     * Register the given spell classes
     * @param classes A collection of the classes to be loaded as spells.
     * @return The number of classes that were successfully registered
     */
    private static int registerClasses(Collection<Class<? extends SpellInstance>> classes) {
        int loadedClasses = 0;
        for (Class<? extends SpellInstance> clazz : classes) {
            Spell spellAnnotation = clazz.getAnnotation(Spell.class);

            if (spellAnnotation == null) {
                WbsMagic.getInstance().getLogger().warning("Class " + clazz.getCanonicalName() + " lacked the Spell annotation.");
                continue;
            }

            String name = spellAnnotation.name();

            registerSpell(name, clazz);

            loadedClasses++;
        }

        return loadedClasses;
    }

    private static RegisteredSpell registerSpell(String name, Class<? extends SpellInstance> spell) {
        name = WbsStrings.capitalizeAll(name.replace('_', ' '));

        if (registeredSpells.containsKey(name)) {
            String otherClasspath = registeredSpells.get(name).getSpellClass().getCanonicalName();
            if (!spell.getCanonicalName().equals(otherClasspath)) {
                WbsMagic.getInstance().getLogger().severe(
                        "Duplicate spells were registered under the name '" + name + "': " +
                                spell.getCanonicalName() + " and " +otherClasspath
                );
            }
            return null;
        }

        return registeredSpells.put(name, new RegisteredSpell(name, spell));
    }

    public static RegisteredSpell getSpell(String name) throws IllegalArgumentException {
        name = WbsStrings.capitalizeAll(name.replace('_', ' '));

        RegisteredSpell spell = registeredSpells.get(name);
        if (spell == null) {
            throw new IllegalArgumentException("Spell name was invalid or not registered");
        }
        return spell;
    }

    public static RegisteredSpell getSpell(Class<? extends SpellInstance> spellClass) throws IllegalArgumentException {
        for (RegisteredSpell checkSpell : registeredSpells.values()) {
            if (checkSpell.getSpellClass() == spellClass) {
                return checkSpell;
            }
        }
        throw new IllegalArgumentException("Spell class was not registered");
    }

    public static Set<String> getSpellNames() {
        return registeredSpells.keySet();
    }
    public static Map<String, RegisteredSpell> getSpells() {
        return Collections.unmodifiableMap(registeredSpells);
    }
}
