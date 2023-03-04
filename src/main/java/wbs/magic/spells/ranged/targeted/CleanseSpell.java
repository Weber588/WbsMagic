package wbs.magic.spells.ranged.targeted;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.spells.framework.LivingEntitySpell;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.SelfTargeter;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;

@Spell(name = "Cleanse",
        cost = 15,
        cooldown = 120,
        description = "Removes negative potion effects, as well as certain other magic effects!"
)
@TargeterOption(optionName = "targeter", defaultType = SelfTargeter.class, defaultRange = 60)
public class CleanseSpell extends SpellInstance implements LivingEntitySpell {

    public CleanseSpell(SpellConfig config, String directory) {
        super(config, directory);

        targeter = config.getTargeter("targeter");

        effect = new NormalParticleEffect();
        effect.setAmount(15);

        // TODO: Something with magic effect/status levels?
    }

    private final GenericTargeter targeter;
    private final NormalParticleEffect effect;

    @Override
    public void castOn(CastingContext context, LivingEntity target) {
        int removed = 0;

        for (PotionEffect effect : target.getActivePotionEffects()) {
            if (plugin.settings.negativePotions.contains(effect.getType())) {
                target.removePotionEffect(effect.getType());
                removed++;
            }
        }

        if (removed > 0) {
            effect.setXYZ(target.getWidth());
            effect.setY(target.getHeight());

            effect.play(Particle.END_ROD, WbsEntityUtil.getMiddleLocation(target));
        }
    }

    @Override
    public GenericTargeter getTargeter() {
        return targeter;
    }
}
