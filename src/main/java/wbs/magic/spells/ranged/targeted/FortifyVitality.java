package wbs.magic.spells.ranged.targeted;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import wbs.magic.SpellCaster;
import wbs.magic.objects.AlignmentType;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.options.DoubleOptions.DoubleOption;
import wbs.magic.spellmanagement.configuration.options.EnumOptions;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spellmanagement.configuration.options.StringOptions.StringOption;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.spells.framework.LivingEntitySpell;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.SelfTargeter;
import wbs.utils.util.string.WbsStrings;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Spell(name = "Fortify Vitality",
        description = "For a short duration, affected creatures are immune to all negative potion effects")
@StringOption(optionName = "negative-potions", defaultValue = "BAD_OMEN",
        listDefaults =
                {
                        "BLINDNESS", "CONFUSION", "DARKNESS", "HUNGER", "LEVITATION",
                        "POISON", "SLOW", "SLOW_DIGGING", "UNLUCK",
                        "WEAKNESS", "WITHER",
                },
        suggestions =
                "BAD_OMEN, BLINDNESS, CONFUSION, HUNGER, LEVITATION, POISON, SLOW, SLOW_DIGGING," +
                        " UNLUCK, WEAKNESS, WITHER"
        )
@StringOption(optionName = "prevention-message", defaultValue = "%spell% protected you from %effect%!")
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.GOOD, enumType = AlignmentType.class)
@TargeterOption(optionName = "targeter", defaultType = SelfTargeter.class)
@DoubleOption(optionName = "duration", defaultValue = 10)
public class FortifyVitality extends StatusSpell {
    private static final FortifyVitalityListener LISTENER = new FortifyVitalityListener();

    public FortifyVitality(SpellConfig config, String directory) {
        super(config, directory);

        preventionMessage = config.getString("prevention-message");

        List<String> negativePotionStrings = config.getStringList("negative-potions");
        for (String potString : negativePotionStrings) {
            PotionEffectType type = PotionEffectType.getByName(potString);
            if (type == null) {
                logError("Invalid potion effect type: " + potString, directory + "/negative-potions");
            } else {
                negativeTypes.add(type);
            }
        }

        if (negativeTypes.isEmpty()) {
            negativeTypes.addAll(plugin.settings.negativePotions);
        }

        tryRegisterListener(LISTENER);
    }

    private final String preventionMessage;
    private final List<PotionEffectType> negativeTypes = new LinkedList<>();

    @Override
    protected @NotNull MagicEntityEffect generateEffect(LivingEntity target, @NotNull SpellCaster caster) {
        MagicEntityEffect marker = new MagicEntityEffect(target, caster, this);

        marker.setExpireMessage("&h" + getName() + "&r fades away...");
        marker.setExpireOnDeath(true);
        marker.setRemoveOnExpire(false);

        return marker;
    }

    private static class FortifyVitalityListener implements Listener {
        @SuppressWarnings("unused")
        @EventHandler
        public void onPotionApply(EntityPotionEffectEvent event) {
            Entity entity = event.getEntity();
            FortifyVitality spell = MagicEntityEffect.getAffectingSpell(entity, FortifyVitality.class);
            if (spell != null) {
                PotionEffectType type = event.getModifiedType();
                if (spell.negativeTypes.contains(type)) {
                    event.setCancelled(true);
                    if (entity instanceof Player) {
                        String message = spell.preventionMessage
                                .replaceAll("%spell%", "&h" + spell.getName() + "&r")
                                .replaceAll("%effect%", "&h" + potionToString(type) + "&r");
                        plugin.sendActionBar(message, (Player) entity);
                    }
                }
            }
        }
    }

    private static String potionToString(PotionEffectType type) {
        return WbsStrings.capitalizeAll(type.getName().replaceAll("_", " "));
    }

    @Override
    public String toString() {
        String asString = super.toString();

        String listJoin = "\n\t&r- &7";

        asString += "\n&rProtects against: &7";
        asString += listJoin + negativeTypes.stream()
                .map(FortifyVitality::potionToString)
                .collect(Collectors.joining(listJoin));

        return asString;
    }
}
