package wbs.magic.passives;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import wbs.magic.MagicSettings;
import wbs.magic.wand.MagicWand;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.WbsParticleEffect;

import java.util.HashMap;
import java.util.Map;

public class ParticlePassive extends PassiveEffect implements TimedPassiveEffect {

    private final Map<WbsParticleEffect, Particle> effects = new HashMap<>();

    public ParticlePassive(ConfigurationSection config, String directory) {
        super(PassiveEffectType.PARTICLES, config, directory);

        MagicSettings settings = MagicSettings.getInstance();

        for (String keyName : config.getKeys(false)) {
            ConfigurationSection effectConfig = config.getConfigurationSection(keyName);
            if (effectConfig == null) {
                settings.logError("Must be a section: " + keyName,
                        directory + "/" + keyName);
                continue;
            }

            try {
                WbsParticleEffect effect = WbsParticleEffect.buildParticleEffect(effectConfig,
                        settings,
                        directory + "/" + keyName);

                String particleString = effectConfig.getString("particle", Particle.WITCH.name());
                Particle particle = WbsEnums.getEnumFromString(Particle.class, particleString);

                if (particle == null) {
                    settings.logError("Invalid particle: " + particleString,
                            directory + "/" + keyName + "/particle");
                    continue;
                }

                effects.put(effect, particle);
            } catch (InvalidConfigurationException ignored) {}
        }
    }

    @Override
    public boolean isEnabled() {
        return !effects.isEmpty();
    }

    @Override
    public String toString() {
        return "Particle Effects";
    }

    private final Table<Player, WbsParticleEffect, Particle> runningEffects = HashBasedTable.create();

    @Override
    public void onStart(MagicWand wand, Player player, ItemStack item, EquipmentSlot slot) {
        for (WbsParticleEffect effect : effects.keySet()) {
            runningEffects.put(player, effect.clone(), effects.get(effect));
        }
    }

    @Override
    public void onTick(MagicWand wand, Player player, ItemStack item, EquipmentSlot slot) {
        runningEffects.row(player).forEach((effect, particle) ->
                effect.buildAndPlay(particle, WbsEntityUtil.getMiddleLocation(player)));
    }

    @Override
    public void onStop(MagicWand wand, Player player, ItemStack item, EquipmentSlot slot) {
        runningEffects.row(player).clear();
    }
}
