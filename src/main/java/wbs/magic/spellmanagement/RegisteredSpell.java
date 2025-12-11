package wbs.magic.spellmanagement;

import net.kyori.adventure.sound.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.DamageSource;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.spellmanagement.configuration.options.SpellOptionManager;
import wbs.magic.spells.SpellInstance;
import wbs.utils.util.WbsSound;
import wbs.utils.util.WbsSoundGroup;

import java.lang.annotation.Annotation;
import java.util.*;

public class RegisteredSpell {

    private final String name;
    private final SpellRegistrationEntry<?> registrationEntry;

    @Nullable
    private SpellSettings settings;
    private final Map<String, Annotation> options = new HashMap<>();
    private final ControlRestrictions controlRestrictions;
    @NotNull
    private final WbsSoundGroup castSound;

    @Nullable
    private final DamageSpell damageSpell;
    @Nullable
    private DamageSource damageSource;

    private SpellConfig defaultConfig;

    public RegisteredSpell(String name, SpellRegistrationEntry<?> registrationEntry) {
        this.name = name;
        this.registrationEntry = registrationEntry;
        Class<? extends SpellInstance> spellClass = registrationEntry.getSpellClass();

        SpellSettings settings = registrationEntry.getSpellClass().getAnnotation(SpellSettings.class);
        if (settings != null) {
            this.settings = settings;
        }

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

        damageSpell = registrationEntry.getSpellClass().getAnnotation(DamageSpell.class);

        loadSpellOptions(spellClass);
    }

    private void loadSpellOptions(Class<?> clazz) {
        // End recursion
        if (clazz == null) return;
        if (clazz.getCanonicalName().startsWith("java")) return;

        // Load options

        for (Class<? extends Annotation> annotationClass : SpellOptionManager.getRegisteredAnnotations()) {
            Annotation[] options = clazz.getAnnotationsByType(annotationClass);
            for (Annotation option : options) {
                addOptionNoOverride(option);
            }
        }

        // Start recursion
        for (Class<?> superClass : clazz.getInterfaces()) {
            loadSpellOptions(superClass);
        }

        Class<?> superClass = clazz.getSuperclass();
        loadSpellOptions(superClass);
    }

    public SpellInstance buildSpell(SpellConfig config, String directory) {
        return registrationEntry.buildSpell(config, directory);
    }

    public @NotNull Spell getSpell() {
        return registrationEntry.getSpellClass().getAnnotation(Spell.class);
    }

    public @Nullable DamageSpell getDamageSpell() {
        return damageSpell;
    }

    public @Nullable FailableSpell getFailableSpell() {
        return registrationEntry.getSpellClass().getAnnotation(FailableSpell.class);
    }

    public @NotNull ControlRestrictions getControlRestrictions() {
        return controlRestrictions;
    }


    public void buildDefaultConfig(@NotNull ConfigurationSection config, String directory) {
        defaultConfig = SpellConfig.fromConfigSection(config, directory, true);
    }

    public SpellConfig getDefaultConfig() {
        return defaultConfig;
    }

    private void addOptionNoOverride(Annotation option) {
        String key = SpellOptionManager.getOptionName(option);
        Objects.requireNonNull(key);
        if (!this.options.containsKey(key)) {
            this.options.put(key, option);
        }
    }

    public String getName() {
        return name;
    }
    public Class<? extends SpellInstance> getSpellClass() {
        return registrationEntry.getSpellClass();
    }

    @Nullable
    public SpellSettings getSettings() {
        return settings;
    }

    public Map<String, Annotation> getOptions() {
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

    @NotNull
    public WbsSoundGroup getCastSound() {
        return castSound;
    }
}
