package wbs.magic.spells.ranged.targeted;

import org.bukkit.entity.Animals;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.spells.framework.EntityTargetedSpell;
import wbs.magic.spells.ranged.RangedSpell;
import wbs.magic.targeters.GenericTargeter;

@Spell(name = "Mass Breed",
        cost = 15,
        cooldown = 120,
        description = "Breed all targeted animals, as if you fed them."
)
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 200)
// Overrides
@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 25)
@SpellOption(optionName = "targeter", type = SpellOptionType.STRING, defaultString = "RADIUS", aliases = {"target", "targetter"})
public class MassBreed extends RangedSpell implements EntityTargetedSpell<Animals> {

    private GenericTargeter targeter;
    private final int duration;

    public MassBreed(SpellConfig config, String directory) {
        super(config, directory);
        configureTargeter(config, directory);

        duration = (int) (config.getDouble("duration") * 20);
    }

    @Override
    public void castOn(CastingContext context, Animals target) {
        target.setLoveModeTicks(duration);
    }

    @Override
    public Class<Animals> getEntityClass() {
        return Animals.class;
    }

    @Override
    public void setTargeter(GenericTargeter targeter) {
        this.targeter = targeter;
    }

    @Override
    public GenericTargeter getTargeter() {
        return targeter;
    }
}
