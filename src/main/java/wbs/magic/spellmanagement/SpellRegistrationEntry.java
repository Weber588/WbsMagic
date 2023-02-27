package wbs.magic.spellmanagement;

import wbs.magic.spells.SpellInstance;

import java.util.function.BiFunction;

public class SpellRegistrationEntry<T extends SpellInstance> {

    private final Class<T> spellClass;
    private final BiFunction<SpellConfig, String, T> producer;

    public SpellRegistrationEntry(Class<T> spellClass, BiFunction<SpellConfig, String, T> producer) {
        this.spellClass = spellClass;
        this.producer = producer;
    }

    public Class<T> getSpellClass() {
        return this.spellClass;
    }

    public T buildSpell(SpellConfig config, String directory) {
        return producer.apply(config, directory);
    }
}
