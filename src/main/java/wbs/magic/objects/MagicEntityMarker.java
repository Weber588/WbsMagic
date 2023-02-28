package wbs.magic.objects;

import org.bukkit.entity.Entity;
import wbs.magic.SpellCaster;
import wbs.magic.spells.SpellInstance;

public class MagicEntityMarker extends MagicEntityEffect {
    public MagicEntityMarker(Entity entity, SpellCaster caster, SpellInstance castingSpell) {
        super(entity, caster, castingSpell);
    }

    @Override
    protected final boolean tick() {
        boolean toReturn = super.tick();

        playParticles();

        return toReturn;
    }

    /**
     * Overrideable method to play particles.
     */
    protected void playParticles() {
        if (effects != null) {
            effects.play(location);
        }
    }
}
