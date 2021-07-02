package wbs.magic.spells;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import wbs.magic.MagicSettings;
import wbs.magic.WbsMagic;
import wbs.magic.annotations.RequiresPlugin;
import wbs.magic.annotations.Spell;
import wbs.magic.spellinstances.SpellInstance;
import wbs.utils.util.string.WbsStrings;

import java.util.*;
import java.util.logging.Logger;

public final class SpellManager {
    private SpellManager() {}

    private static final Map<String, RegisteredSpell> registeredSpells = new HashMap<>();
    private static final Map<String, RegisteredSpell> spellAliases = new HashMap<>();

    public static void clear() {
        registeredSpells.clear();
    }

    /**
     * Register the given spell classes
     * @param loader The spell loader to load spells from
     * @return The number of classes that were successfully registered
     */
    public static int registerClasses(SpellLoader loader) {
        Collection<Class<? extends SpellInstance>> classes = loader.getSpells();
        int loadedClasses = 0;
        PluginManager manager = Bukkit.getPluginManager();

        Logger logger = WbsMagic.getInstance().getLogger();

        Multimap<String, Class<? extends SpellInstance>> requiredPlugins = LinkedHashMultimap.create();
        for (Class<? extends SpellInstance> clazz : classes) {
            Spell spellAnnotation = clazz.getAnnotation(Spell.class);

            if (spellAnnotation == null) {
                logger.warning("Class " + clazz.getCanonicalName() + " lacked the Spell annotation.");
                continue;
            }

            RequiresPlugin requiresPlugin = clazz.getAnnotation(RequiresPlugin.class);
            if (requiresPlugin != null) {
                if (!manager.isPluginEnabled(requiresPlugin.value())) {
                    requiredPlugins.put(requiresPlugin.value(), clazz);
                    continue;
                }
            }

            String name = spellAnnotation.name();

            RegisteredSpell spell = registerSpell(name, clazz);

            if (spell != null) loadedClasses++;
        }

        for (String key : requiredPlugins.keySet()) {
            logger.info("The following spells require the " + key + " plugin:");
            for (Class<? extends SpellInstance> clazz : requiredPlugins.get(key)) {
                logger.info("\t- " + clazz.getCanonicalName());
            }
        }

        WbsMagic.getInstance().getLogger().info("Loaded " +
                loadedClasses + " out of " + classes.size() + " spells from " + loader.getClass().getSimpleName() + ".");

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

        RegisteredSpell newSpell = new RegisteredSpell(name, spell);

        setAlias(newSpell, newSpell.getName(), "Internal");

        registeredSpells.put(name, newSpell);

        return newSpell;
    }

    public static void unregisterSpell(Class<? extends SpellInstance> clazz) {
        RegisteredSpell removed = registeredSpells.remove(clazz.getCanonicalName());
        if (removed != null) {
            WbsMagic.getInstance().getLogger().info(clazz.getCanonicalName() + " was unregistered.");
        }
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

    public static void setAlias(RegisteredSpell spell, String alias, String directory) {
        RegisteredSpell aliasedSpell = spellAliases.get(alias);
        if (aliasedSpell != null && aliasedSpell.getSpellClass() != spell.getSpellClass()) {
            MagicSettings.getInstance().logError("The alias \"" + alias + "\" is already registered to the spell " + aliasedSpell.getName(), directory);
            return;
        }

        spellAliases.put(alias, spell);
    }

    public static RegisteredSpell getAliasedSpell(String alias) {
        return spellAliases.get(alias);
    }

    public static List<String> getAliasesFor(RegisteredSpell spell) {
        List<String> aliases = new LinkedList<>();

        for (String alias : spellAliases.keySet()) {
            if (spellAliases.get(alias) == spell) {
                aliases.add(alias);
            }
        }

        return aliases;
    }
}
