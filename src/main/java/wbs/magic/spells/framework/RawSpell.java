package wbs.magic.spells.framework;

import wbs.magic.spells.SpellInstance;

/**
 * Represents a spell that may be cast without any additional parameters.
 * A SpellInstance may simply be cast by overriding the
 * {@link SpellInstance#cast(CastingContext)} method, but can use this interface
 * if it has multiple cast methods
 *
 */
public interface RawSpell extends Castable {

    /**
     * Cast the spell with a given caster.
     * @param context The context around the cast spell
     * @return true if the spell was successful, false if the spell failed
     */
    boolean castRaw(CastingContext context);

}
