package wbs.magic.wand;

import java.util.*;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import wbs.magic.WbsMagic;
import wbs.magic.passives.PassiveEffect;
import wbs.magic.passives.PassiveEffectType;
import wbs.magic.spells.SpellInstance;

public class MagicWand {

	public static final NamespacedKey WAND_NAME_KEY = new NamespacedKey(WbsMagic.getInstance(), "wand_name");
	private static final PersistentDataType<String, String> WAND_NAME_TYPE = PersistentDataType.STRING;

	public static Map<String, MagicWand> allWands = new HashMap<>();
	private static Map<String, MagicWand> displayNames = new HashMap<>();

	public static boolean wandExists(String name) {
		return allWands.containsKey(name);
	}
	
	public static List<String> getWandNames() {
		List<String> orderedWandNames = new LinkedList<>(allWands.keySet());
		orderedWandNames.sort(Comparator.naturalOrder());
		return orderedWandNames;
	}

	public static List<MagicWand> allWands() {
		List<MagicWand> orderedWands = new LinkedList<>(allWands.values());
		orderedWands.sort(Comparator.comparing(MagicWand::getWandName));
		return orderedWands;
	}

	@Nullable
	public static MagicWand getWand(String name) {
		return allWands.get(name);
	}

	@Nullable
	public static MagicWand getHeldWand(Player player) {
		return MagicWand.getWand(player.getInventory().getItemInMainHand());
	}

	@Nullable
	public static MagicWand getWand(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return null;
		}

		MagicWand wand = null;

		String wandName = meta.getPersistentDataContainer().get(WAND_NAME_KEY, WAND_NAME_TYPE);
		if (wandName != null) {
			wand = allWands.get(wandName);
		}

		if (wand == null && WbsMagic.getInstance().settings.retrieveByWandName()) {
			wand = displayNames.get(meta.getDisplayName());
		}

		return wand;
	}

	public static boolean isWand(ItemStack item) {
		return (getWand(item) != null);
	}
	
	public static void clear() {
		allWands.clear();
	}
	
	private final String wandName;
	private int maxTier = 1;
	private final String display;
	private String permission = "";
	private final Material material;
	private boolean sendErrors = true;
	private boolean cancelDrops = false;
	private boolean allowCombat = false;
	private boolean allowBlockBreaking = false;
	private boolean allowBlockPlacing = false;
	private boolean disarmImmune = false;

	private final Map<Integer, Map<WandControl, SpellInstance>> bindings = new HashMap<>();
	
	private final Map<PassiveEffectType, PassiveEffect> passives = new HashMap<>();

	public void addPassive(PassiveEffect passive) {
		if (!passives.containsKey(passive.type)) {
			passives.put(passive.type, passive);
		}
	}
	
	public MagicWand(String wandName, String display) {
		this(wandName, display, Material.STICK);
	}
			
	public MagicWand(String wandName, String display, Material item) {
		this.display = display;
		this.wandName = wandName;
		material = item;
		allWands.put(wandName, this);
		if (WbsMagic.getInstance().settings.retrieveByWandName()) {
			buildNewWand(); // to add display name according to meta, not exact string.
		}
	}
	
	public String getWandName() {
		return wandName;
	}

	public Map<Integer, Map<WandControl, SpellInstance>> bindingMap() {
		return bindings;
	}
	
	public Map<PassiveEffectType, PassiveEffect> passivesMap() {
		return passives;
	}
	
	public Set<SpellInstance> allBindings() {
		Set<SpellInstance> returnSet = new HashSet<>();
		for (int tier : bindings.keySet()) {
			Map<WandControl, SpellInstance> tiersBindings = bindings.get(tier);
			for (WandControl control : tiersBindings.keySet()) {
				returnSet.add(tiersBindings.get(control));
			}
		}
		
		return returnSet;
	}
	
	public void addSpell(int tier, WandControl control, SpellInstance spell) {
		if (!bindings.containsKey(tier)) {
			bindings.put(tier, new HashMap<>());
		}
		bindings.get(tier).put(control,  spell);
	}

	@Nullable
	public SpellInstance getBinding(int tier, WandControl control) {
		if (bindings.size() < tier) return null;

		return bindings.get(tier).get(control);
	}

	public boolean hasBinding(int tier, WandControl control) {
		if (bindings.size() == 0) return false;
		if (tier > getMaxTier()) tier = 1;

		return bindings.get(tier).containsKey(control);
	}

	public boolean hasSimplifiedBinding(int tier, WandControl control) {
		if (bindings.size() == 0) return false;
		if (tier > getMaxTier()) tier = 1;

		if (bindings.get(tier).containsKey(control)) return true;

		while (control.isCombined()) {
			control = control.getSimplified();
			if (bindings.get(tier).containsKey(control)) return true;
		}

		return false;
	}
	
	public void setMaxTier(int maxTier) {
		this.maxTier = maxTier;
	}

	public String getDisplay() {
		return display;
	}
	public int getMaxTier() {
		return maxTier;
	}
	public Material getMaterial() {
		return material;
	}
	
	private List<String> lore;
	public void setLore(List<String> lore) {
		this.lore = lore;
	}
	
	private boolean shiny;
	public void setShiny(boolean shiny) {
		this.shiny = shiny;
	}
	
	private ItemStack wandItem;
	public ItemStack getItem() {
		if (wandItem == null) {
			wandItem = buildNewWand();
		}
		return wandItem;
	}

	public ItemStack buildNewWand() {
		ItemStack item = new ItemStack(material);
		
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(material);
		assert meta != null;
		meta.setDisplayName(display);
		meta.setLore(lore);

		meta.getPersistentDataContainer().set(WAND_NAME_KEY, WAND_NAME_TYPE, wandName);
		
		if (shiny) {
 			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}

		// Hides some extra unneeded text in lore section
		meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

		item.setItemMeta(meta);

		if (shiny) {
 			item.addUnsafeEnchantment(Enchantment.LOYALTY, 1);
		}

		// Display name changes when added to meta.
		// This guarantees that the display name will match on retrieval.
		displayNames.put(meta.getDisplayName(), this);

		return item;
	}

	public void doErrorMessages(boolean sendErrors) {
		this.sendErrors = sendErrors;
	}
	
	public boolean doErrorMessages() {
		return sendErrors;
	}
	
	public boolean cancelDrops() {
		return cancelDrops;
	}
	
	public MagicWand cancelDrops(boolean cancelDrops) {
		this.cancelDrops = cancelDrops;
		return this;
	}
	
	public MagicWand setPermission(String permission) {
		this.permission = permission;
		return this;
	}
	public String getPermission() {
		return permission;
	}

	public boolean allowCombat() {
		return allowCombat;
	}

	public void setAllowCombat(boolean allowCombat) {
		this.allowCombat = allowCombat;
	}

	public boolean allowBlockBreaking() {
		return allowBlockBreaking;
	}

	public void setAllowBlockBreaking(boolean allowBlockBreaking) {
		this.allowBlockBreaking = allowBlockBreaking;
	}

	public boolean allowBlockPlacing() {
		return allowBlockPlacing;
	}

	public void setAllowBlockPlacing(boolean allowBlockPlacing) {
		this.allowBlockPlacing = allowBlockPlacing;
	}

	public void setDisarmImmune(boolean disarmImmune) {
		this.disarmImmune = disarmImmune;
	}

	public boolean isDisarmImmune() {
		return disarmImmune;
	}
}









