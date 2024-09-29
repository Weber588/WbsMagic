package wbs.magic.passives;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import wbs.magic.MagicSettings;
import wbs.magic.wand.MagicWand;
import wbs.utils.util.string.WbsStrings;

public class PotionPassive extends PassiveEffect implements TimedPassiveEffect {
	private static final PotionEffectType[] EXTENDED_POTIONS =
			{
					PotionEffectType.NIGHT_VISION,
					PotionEffectType.NAUSEA,
					PotionEffectType.BLINDNESS
			};


		private final List<PotionEffect> effects = new LinkedList<>();

	public PotionPassive(ConfigurationSection config, String directory) {
		super(PassiveEffectType.POTION, config, directory);

		Map<PotionEffectType, Integer> potionEffects = new HashMap<>();

		// PotionEffectType isn't an enum for some reason, so WbsEnums won't be used here.
		
		for (String keyName : config.getKeys(false)) {
			if (PotionEffectType.getByName(keyName) != null) {
				PotionEffectType potionType = PotionEffectType.getByName(keyName);
				
				int level = config.getInt(keyName, 1);
				level = Math.max(1, level);
				level = Math.min(256, level);
				
				level--;
				potionEffects.put(potionType, level);
			} else {
				logError("Invalid potion type: " + keyName, directory);
			}
		}

		int passivesRefreshRate = MagicSettings.getInstance().getPassiveRefreshRate();

		for (PotionEffectType potionType : potionEffects.keySet()) {
			int ticks = passivesRefreshRate * 2;
			for (PotionEffectType extendedType : EXTENDED_POTIONS) {
				if (potionType.equals(extendedType)) {
					ticks = Math.max(250 + passivesRefreshRate, passivesRefreshRate * 2);
					break;
				}
			}
			PotionEffect effect = new PotionEffect(potionType, ticks, potionEffects.get(potionType), true, false);
			effects.add(effect);
		}
	}

	@Override
	public boolean isEnabled() {
		return !effects.isEmpty();
	}

	@Override
	public String toString() {
		StringBuilder asString = new StringBuilder(super.toString());
		
		for (PotionEffect effect : effects) {
			int level = effect.getAmplifier();
			asString.append("\n&r")
					.append(WbsStrings.capitalizeAll(effect.getType().getName().replace('_', ' ')))
					.append(": &7")
					.append(level + 1);
		}
		
		return asString.toString();
	}

	@Override
	public void onStart(MagicWand wand, Player player, ItemStack item, EquipmentSlot slot) {

	}

	@Override
	public void onTick(MagicWand wand, Player player, ItemStack item, EquipmentSlot slot) {
		for (PotionEffect effect : effects) {
			player.addPotionEffect(effect, true);
		}
	}

	@Override
	public void onStop(MagicWand wand, Player player, ItemStack item, EquipmentSlot slot) {
		for (PotionEffect effect : effects) {
			player.removePotionEffect(effect.getType());
		}
	}
}
