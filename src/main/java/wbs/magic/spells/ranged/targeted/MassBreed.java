package wbs.magic.spells.ranged.targeted;

import org.bukkit.entity.Animals;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.spells.framework.EntityTargetedSpell;
import wbs.magic.spells.ranged.RangedSpell;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.RadiusTargeter;

@Spell(name = "Mass Breed",
        cost = 15,
        cooldown = 120,
        description = "Breed all targeted animals, as if you fed them."
)
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 200)
@TargeterOption(optionName = "targeter", defaultType = RadiusTargeter.class, defaultRange = 100)
public class MassBreed extends SpellInstance implements EntityTargetedSpell<Animals> {

    private final GenericTargeter targeter;
    private final int duration;

    public MassBreed(SpellConfig config, String directory) {
        super(config, directory);

        duration = (int) (config.getDouble("duration") * 20);
        targeter = config.getTargeter("targeter");
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
    public GenericTargeter getTargeter() {
        return targeter;
    }
}
