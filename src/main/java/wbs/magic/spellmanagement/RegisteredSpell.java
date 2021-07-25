package wbs.magic.spellmanagement;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.spells.SpellInstance;
import wbs.utils.util.WbsSound;
import wbs.utils.util.WbsSoundGroup;

import java.util.*;

public class RegisteredSpell {

    private final String name;
    private final Class<? extends SpellInstance> spellClass;

    private final Spell spell;
    private SpellSettings settings;
    private final Map<String, SpellOption> options = new HashMap<>();
    private final FailableSpell failableSpell;
    private final DamageSpell damageSpell;
    private final ControlRestrictions controlRestrictions;
    @NotNull
    private final WbsSoundGroup castSound;

    private SpellConfig defaultConfig;

    public RegisteredSpell(String name, Class<? extends SpellInstance> spellClass) {
        this.name = name;
        this.spellClass = spellClass;

        SpellSettings settings = spellClass.getAnnotation(SpellSettings.class);
        if (settings != null) {
            this.settings = settings;
        }

        spell = spellClass.getAnnotation(Spell.class);

        failableSpell = spellClass.getAnnotation(FailableSpell.class);
        damageSpell = spellClass.getAnnotation(DamageSpell.class);
        controlRestrictions = new ControlRestrictions(
                spellClass.getAnnotation(RestrictWandControls.class));

        castSound = new WbsSoundGroup();
        SpellSounds sounds = spellClass.getAnnotation(SpellSounds.class);
        if (sounds != null) {
            for (SpellSound sound : sounds.value()) {
                WbsSound newSound = new WbsSound(sound.sound(), sound.pitch(), sound.volume());
                castSound.addSound(newSound);
            }
        } else {
            SpellSound sound = spellClass.getAnnotation(SpellSound.class);
            if (sound != null) {
                WbsSound newSound = new WbsSound(sound.sound(), sound.pitch(), sound.volume());
                castSound.addSound(newSound);
            }
        }

        SpellOptions options = spellClass.getAnnotation(SpellOptions.class);
        if (options != null) {
            addOptionsNoOverrides(options);
        } else { // Try for single SpellOption
            SpellOption option = spellClass.getAnnotation(SpellOption.class);
            if (option != null) {
                addOptionNoOverride(option);
            }
        }

        // Iterate up through classes until SpellInstance is reached.
        // Uses NoOverrides methods, as lower classes override higher ones.
        Class<?> superClass = spellClass.getSuperclass();
        while (SpellInstance.class.isAssignableFrom(superClass)) {

            options = superClass.getAnnotation(SpellOptions.class);
            if (options != null) {
                addOptionsNoOverrides(options);
            } else { // Try for single SpellOption
                SpellOption option = superClass.getAnnotation(SpellOption.class);
                if (option != null) {
                    addOptionNoOverride(option);
                }
            }

            superClass = superClass.getSuperclass();
        }
    }

    public @NotNull Spell getSpell() {
        return spell;
    }
    public @Nullable DamageSpell getDamageSpell() {
        return damageSpell;
    }
    public @Nullable FailableSpell getFailableSpell() {
        return failableSpell;
    }
    public @NotNull ControlRestrictions getControlRestrictions() {
        return controlRestrictions;
    }


    public @Nullable SpellConfig buildDefaultConfig(@NotNull ConfigurationSection config, String directory, boolean logMissing) {
        defaultConfig = SpellConfig.fromConfigSection(config, directory, true, logMissing);
        return defaultConfig;
    }

    public SpellConfig getDefaultConfig() {
        return defaultConfig;
    }

    /**
     * Add a set of SpellOptions without overriding existing keys if already set
     * @param options The SpellOptions containing the array of options to set
     */
    private void addOptionsNoOverrides(SpellOptions options) {
        for (SpellOption option : options.value()) {
            addOptionNoOverride(option);
        }
    }

    private void addOptionNoOverride(SpellOption option) {
        if (!this.options.containsKey(option.optionName())) {
            this.options.put(option.optionName(), option);
        }
    }

    public String getName() {
        return name;
    }
    public Class<? extends SpellInstance> getSpellClass() {
        return spellClass;
    }

    public SpellSettings getSettings() {
        return settings;
    }

    public Map<String, SpellOption> getOptions() {
        return Collections.unmodifiableMap(options);
    }

    /**
     * Take a {@link ConfigurationSection} and fill out the options on it
     * @param config The config to fill out
     * @return The same configuration section, with the option fields configured
     */
    public ConfigurationSection toConfigSection(ConfigurationSection config) {
        return defaultConfig.writeToConfig(config);
    }

    public WbsSoundGroup getCastSound() {
        return castSound;
    }
}
