package wbs.magic.spells.ranged.targeted;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import wbs.magic.objects.AlignmentType;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.objects.PersistenceLevel;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.options.EnumOptions;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
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
@EnumOption(optionName = "dispel-alignment", defaultValue = AlignmentType.Name.BAD, enumType = AlignmentType.class)
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.GOOD, enumType = AlignmentType.class)
public class CleanseSpell extends SpellInstance implements LivingEntitySpell {

    public CleanseSpell(SpellConfig config, String directory) {
        super(config, directory);

        targeter = config.getTargeter("targeter");

        effect = new NormalParticleEffect();
        effect.setAmount(15);

        dispelAlignment = config.getEnum("dispel-alignment", AlignmentType.class);
    }

    private final AlignmentType dispelAlignment;
    private final GenericTargeter targeter;
    private final NormalParticleEffect effect;

    @Override
    public void castOn(CastingContext context, LivingEntity target) {
        int removedPotion = 0;

        for (PotionEffect effect : target.getActivePotionEffects()) {
            if (plugin.settings.negativePotions.contains(effect.getType())) {
                target.removePotionEffect(effect.getType());
                removedPotion++;
            }
        }

        int removedEffect = 0;

        for (MagicEntityEffect effect : MagicEntityEffect.getEffects(target)) {
            int level = effect.getAlignmentType().getLevel();
            if (level >= dispelAlignment.getLevel() && level < AlignmentType.NEUTRAL.getLevel()) {
                effect.dispel(PersistenceLevel.STRONG);
                removedEffect++;
            }
        }

        if (removedPotion + removedEffect > 0) {
            effect.setXYZ(target.getWidth());
            effect.setY(target.getHeight());

            effect.play(Particle.END_ROD, WbsEntityUtil.getMiddleLocation(target));

            if (target instanceof Player) {
                Player player = (Player) target;

                if (removedEffect == 0) {
                    sendActionBar("Cleansed of " + removedPotion + " negative potion effects!", player);
                } else if (removedPotion == 0) {
                    sendActionBar("Cleansed of " + removedEffect + " negative magic effects!", player);
                } else {
                    sendActionBar("Cleansed of " + (removedEffect + removedPotion) + " negative magic & potion effects!", player);
                }
            }

        }
    }

    @Override
    public GenericTargeter getTargeter() {
        return targeter;
    }
}
