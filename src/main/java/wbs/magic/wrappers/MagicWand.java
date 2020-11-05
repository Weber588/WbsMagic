package wbs.magic.wrappers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import org.bukkit.persistence.PersistentDataType;
import wbs.magic.WbsMagic;
import wbs.magic.enums.WandControl;
import wbs.magic.passives.PassiveEffect;
import wbs.magic.passives.PassiveEffectType;
import wbs.magic.spellinstances.SpellInstance;

public class MagicWand {

	public static final NamespacedKey WAND_NAME_KEY = new NamespacedKey(WbsMagic.getInstance(), "wand_name");
	private static final PersistentDataType<String, String> WAND_NAME_TYPE = PersistentDataType.STRING;

	public static Map<String, MagicWand> allWands = new HashMap<>();

	public static boolean wandExists(String name) {
		return allWands.containsKey(name);
	}
	
	public static Set<String> getWandNames() {
		return allWands.keySet();
	}
	
	public static MagicWand getWand(String name) {
		return allWands.get(name);
	}
	
	public static MagicWand getWand(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return null;
		}

		String wandName = meta.getPersistentDataContainer().get(WAND_NAME_KEY, WAND_NAME_TYPE);
		if (wandName != null) {
			return allWands.get(wandName);
		}

		return null;
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
	private int requiresLevel = 0;
	private Material material = Material.STICK;
	private boolean sendErrors = true;
	private boolean cancelDrops = false;
	
	private final Map<Integer, Map<WandControl, SpellInstance>> bindings = new HashMap<>();
	
	private final Map<PassiveEffectType, PassiveEffect> passives = new HashMap<>();
	
	public void addPassive(PassiveEffect passive) {
		if (!passives.containsKey(passive.type)) {
			passives.put(passive.type, passive);
		}
	}
	
	public MagicWand(String wandName, String display) {
		this.display = display;
		this.wandName = wandName;
		allWands.put(wandName, this);
	}
			
	public MagicWand(String wandName, String display, Material item, int requiresLevel) {
		this.display = display;
		this.wandName = wandName;
		this.requiresLevel = requiresLevel;
		material = item;
		allWands.put(wandName, this);
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
	
	public SpellInstance getBinding(int tier, WandControl control) {
		if (!bindings.containsKey(tier)) {
			bindings.put(tier, new HashMap<>());
		}
		return bindings.get(tier).get(control);
	}
	
	public void setMaxTier(int maxTier) {
		this.maxTier = maxTier;
	}

	public int getLevel() {
		return requiresLevel;
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

		item.setItemMeta(meta);

		if (shiny) {
 			item.addUnsafeEnchantment(Enchantment.LOYALTY, 1);
		}
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
}









