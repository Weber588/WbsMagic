package wbs.magic.wand;

import java.util.*;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.SpellCaster;
import wbs.magic.WbsMagic;
import wbs.magic.controls.EventDetails;
import wbs.magic.passives.PassiveEffect;
import wbs.magic.passives.PassiveEffectType;

public class MagicWand {

	public static final NamespacedKey WAND_NAME_KEY = new NamespacedKey(WbsMagic.getInstance(), "wand_name");
	private static final PersistentDataType<String, String> WAND_NAME_TYPE = PersistentDataType.STRING;

	public static final Map<String, MagicWand> allWands = new HashMap<>();
	private static final Map<String, MagicWand> displayNames = new HashMap<>();

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

	public static boolean isExpiredWand(ItemStack item) {
		String wandName = getWandName(item);
		if (wandName == null) return false;

		return allWands.get(wandName) == null;
	}

	@Nullable
	public static MagicWand getWand(@NotNull ItemStack item) {
		String wandName = getWandName(item);

		if (wandName == null) return null;

		return allWands.get(wandName);
	}

	@Nullable
	public static String getWandName(@NotNull ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return null;

		String wandName = meta.getPersistentDataContainer().get(WAND_NAME_KEY, WAND_NAME_TYPE);
		if (wandName != null) {
			return wandName;
		}

		if (WbsMagic.getInstance().settings.retrieveByWandName()) {
			MagicWand found = displayNames.get(meta.getDisplayName());
			if (found != null) {
				return found.wandName;
			}
		}

		return null;
	}

	public static boolean isWand(ItemStack item) {
		return (getWand(item) != null);
	}
	
	public static void clear() {
		allWands.clear();
	}

	// ======================== //
	//         Instance         //
	// ======================== //
	
	private final String wandName;
	private String permission = "";

	private int uses = -1;
	
	public MagicWand(String wandName, String display) {
		this(wandName, display, Material.STICK);
	}
			
	public MagicWand(String wandName, String display, Material item) {
		this.display = display;
		wandName = wandName.replace(' ', '_');
		this.wandName = wandName;
		material = item;
		allWands.put(wandName, this);
		if (WbsMagic.getInstance().settings.retrieveByWandName()) {
			buildNewWand(); // to add display name according to meta, not exact string.
		}
	}

	// ================================ //
	//         Spells & Bindings        //
	// ================================ //

	private final Map<Integer, List<SpellBinding>> bindings = new HashMap<>();
	private final Table<EquipmentSlot, PassiveEffectType, PassiveEffect> passives = HashBasedTable.create();
	private int maxTier = 1;

	public Map<Integer, List<SpellBinding>> bindingMap() {
		return bindings;
	}
	
	public Table<EquipmentSlot, PassiveEffectType, PassiveEffect> passivesMap() {
		return passives;
	}

	public void addPassive(EquipmentSlot slot, PassiveEffect passive) {
		if (!passives.contains(slot, passive.type)) {
			passives.put(slot, passive.type, passive);
		}
	}

	/**
	 * Add a spell to this wand under a given tier and control, taking into
	 * account mutually exclusive wand controls
	 * @param tier The tier needed to cast the spell on the given control
	 * @param binding The binding to add at the given tier
	 */
	public void addSpell(int tier, SpellBinding binding) {
		List<SpellBinding> bindingList = bindings.get(tier);

		if (bindingList == null) {
			bindingList = new ArrayList<>();
		}

		bindingList.add(binding);
		bindingList.sort(Comparator.comparingInt(o -> o.getTrigger().getPriority()));

		bindings.put(tier, bindingList);
	}

	public SpellBinding tryCasting(SpellCaster caster, EventDetails event) {
		if (!(permission == null || permission.equals(""))) {
			if (!caster.getPlayer().hasPermission(permission)) {
				caster.sendActionBar("&wYou do not have permission to use this.");
				return null;
			}
		}

		Collection<SpellBinding> tierBindings = bindings.get(caster.getTier());
		if (tierBindings == null || tierBindings.isEmpty()) {
			return null;
		}

		for (SpellBinding binding : tierBindings) {
		//	caster.sendMessage("Checking " + binding.getSpell().simpleString() + " with priority "
		//			+ binding.getTrigger().getPriority() + " and control "
		//			+ binding.getTrigger().getControl());
			if (binding.getTrigger().runFor(event.event)) {
				if (binding.getTrigger().checkConditions(event)) {
					if (!caster.offCooldown(binding.getSpell(), this, binding.getSpell().sendErrors())) {
						caster.setTier(1);
						return null;
					}
					if (caster.castSpell(event, binding, this)) {
						return binding;
					}
				}
			}
		}

		return null;
	}

	// ================================ //
	//        Getters & Setters         //
	// ================================ //

	private boolean preventDrops = false;

	private boolean preventCombat = false;
	private boolean preventBlockBreaking = false;
	private boolean preventBlockPlacing = false;

	private boolean disarmImmune = false;

	public boolean preventDrops() {
		return preventDrops;
	}

	public MagicWand preventDrops(boolean cancelDrops) {
		this.preventDrops = cancelDrops;
		return this;
	}

	public MagicWand setPermission(String permission) {
		this.permission = permission;
		return this;
	}

	public String getPermission() {
		return permission;
	}

	public boolean preventCombat() {
		return preventCombat;
	}

	public void setPreventCombat(boolean preventCombat) {
		this.preventCombat = preventCombat;
	}

	public boolean preventBlockBreaking() {
		return preventBlockBreaking;
	}

	public void setPreventBlockBreaking(boolean preventBlockBreaking) {
		this.preventBlockBreaking = preventBlockBreaking;
	}

	public boolean preventBlockPlacing() {
		return preventBlockPlacing;
	}

	public void setPreventBlockPlacing(boolean preventBlockPlacing) {
		this.preventBlockPlacing = preventBlockPlacing;
	}

	public void setDisarmImmune(boolean disarmImmune) {
		this.disarmImmune = disarmImmune;
	}

	public boolean isDisarmImmune() {
		return disarmImmune;
	}
	
	public void setMaxTier(int maxTier) {
		this.maxTier = maxTier;
	}

	// ================================ //
	//            Item stuff            //
	// ================================ //

	private final String display;
	private final Material material;

	private List<String> lore;
	private boolean shiny;
	private ItemStack wandItem;

	private final Map<Enchantment, Integer> enchantments = new HashMap<>();
	private final Set<ConfiguredAttribute> attributes = new HashSet<>();
	private final Set<ItemFlag> itemFlags = new HashSet<>();

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
		
		if (shiny && enchantments.isEmpty()) {
 			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}

		// Hides some extra unneeded text in lore section
		meta.addItemFlags(itemFlags.toArray(new ItemFlag[0]));

		for (ConfiguredAttribute attribute : attributes) {
			meta.addAttributeModifier(attribute.attribute, attribute.modifier);
		}

		if (shiny) {
			meta.setEnchantmentGlintOverride(true);
		}

		item.setItemMeta(meta);

		for (Enchantment enchant : enchantments.keySet()) {
			item.addUnsafeEnchantment(enchant, enchantments.get(enchant));
		}

		// Display name changes when added to meta.
		// This guarantees that the display name will match on retrieval.
		displayNames.put(meta.getDisplayName(), this);

		return item;
	}

	public MagicWand addEnchantment(Enchantment enchant, int level) {
		enchantments.put(enchant, level);
		return this;
	}

	public MagicWand addAttribute(ConfiguredAttribute attribute) {
		attributes.add(attribute);
		return this;
	}

	public MagicWand addItemFlags(Collection<ItemFlag> flags) {
		itemFlags.addAll(flags);
		return this;
	}

	public String getWandName() {
		return wandName;
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

	public void setLore(List<String> lore) {
		this.lore = lore;
	}

	public void setShiny(boolean shiny) {
		this.shiny = shiny;
	}
}









