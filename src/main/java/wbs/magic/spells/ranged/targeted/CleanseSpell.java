package wbs.magic.spells.ranged.targeted;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.spells.framework.LivingEntitySpell;
import wbs.magic.targeters.GenericTargeter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Spell(name = "Cleanse",
        cost = 15,
        cooldown = 120,
        description = "Removes negative potion effects, as well as certain other magic effects!"
)
// Overrides
@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 25)
@SpellOption(optionName = "targeter", type = SpellOptionType.STRING, defaultString = "SELF", aliases = {"target", "targetter"})
public class CleanseSpell extends SpellInstance implements LivingEntitySpell {
    private GenericTargeter targeter;

    // TODO: Make this configurable?
    private final Set<PotionEffectType> NEGATIVE_EFFECTS = new HashSet<>(Arrays.asList(
            PotionEffectType.BAD_OMEN,
            PotionEffectType.BLINDNESS,
            PotionEffectType.CONFUSION,
            PotionEffectType.HUNGER,
            PotionEffectType.LEVITATION,
            PotionEffectType.POISON,
            PotionEffectType.SLOW,
            PotionEffectType.SLOW_DIGGING,
            PotionEffectType.UNLUCK,
            PotionEffectType.WEAKNESS,
            PotionEffectType.WITHER
    ));

    public CleanseSpell(SpellConfig config, String directory) {
        super(config, directory);

        configureTargeter(config, directory);

        // TODO: Something with magic effect/status levels?
    }

    @Override
    public void castOn(CastingContext context, LivingEntity target) {
        for (PotionEffect effect : target.getActivePotionEffects()) {
            if (NEGATIVE_EFFECTS.contains(effect.getType())) {
                target.removePotionEffect(effect.getType());
            }
        }
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
