package wbs.magic.spells.ranged.targeted;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import wbs.magic.SpellCaster;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.options.DoubleOptions.DoubleOption;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.spells.framework.LivingEntitySpell;
import wbs.magic.targeters.GenericTargeter;

@DoubleOption(optionName = "duration", defaultValue = 30)
@TargeterOption(optionName = "targeter")
public abstract class StatusSpell extends SpellInstance implements LivingEntitySpell {

    private final GenericTargeter targeter;
    private final int duration;

    public StatusSpell(SpellConfig config, String directory) {
        super(config, directory);

        targeter = config.getTargeter("targeter");
        duration = config.getDurationFromDouble("duration");
    }

    @Override
    public final void castOn(CastingContext context, LivingEntity target) {
        MagicEntityEffect status = generateEffect(target, context.caster);

        status.setMaxAge(duration);
        status.setExpireOnDeath(true);

        status.run();
    }

    @NotNull
    protected MagicEntityEffect generateEffect(LivingEntity target, @NotNull SpellCaster caster) {
        return new MagicEntityEffect(target, caster, this);
    }

    @Override
    public GenericTargeter getTargeter() {
        return targeter;
    }

    @Override
    public String toString() {
        String asString = super.toString();

        asString += "\n&rDuration: &7" + (duration / 20.0) + " seconds";
        asString += "\n&rTarget: &7" + targeter.toString();

        return asString;
    }
}
