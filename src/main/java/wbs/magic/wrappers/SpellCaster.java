package wbs.magic.wrappers;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.time.Duration;
import java.time.LocalDateTime;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import wbs.magic.WbsMagic;
import wbs.magic.enums.SpellType;
import wbs.magic.enums.WandControl;
import wbs.magic.events.SpellCastEvent;
import wbs.magic.events.SpellPrepareEvent;
import wbs.magic.spellinstances.SpellInstance;
import wbs.magic.spellinstances.ranged.targeted.TargetedSpell;
import wbs.magic.statuseffects.generics.StatusEffect;
import wbs.magic.statuseffects.generics.StatusEffect.StatusEffectType;

import wbs.utils.util.WbsEntities;
import wbs.utils.util.WbsRunnable;
import wbs.utils.util.string.WbsStringify;

public class SpellCaster implements Serializable {
	private static final long serialVersionUID = 3509120592222799198L;
	private final static int initialMana = 500;
	private static WbsMagic plugin;
	private static Logger logger;
	
	/*************************/
	/*       Data store      */
	/*************************/
	
	private static Map<UUID, SpellCaster> playerMap = new HashMap<>();
	
	/**
	 * Check if the player has a SpellCaster object already
	 * @param p The player to check
	 * @return true if the player has a SpellCaster object
	 */
	public static boolean isRegistered(Player p) {
		return (playerMap.containsKey(p.getUniqueId()));
	}

	/**
	 * Get the player's SpellCaster object.
	 * Will create one if one does not exist.
	 * @param p The player
	 * @return The corresponding SpellCaster object
	 */
	public static SpellCaster getCaster(Player p) {
		SpellCaster returnCaster;
		UUID uuid = p.getUniqueId();
		if (isRegistered(p)) {
			returnCaster = playerMap.get(uuid);
		} else {
			returnCaster = new SpellCaster(uuid);
		}
		return returnCaster;
	}
	
	/**
	 * Get all caster objects that exist
	 * @return A Map of UUID to SpellCasters
	 */
	public static Map<UUID, SpellCaster> allCasters() {
		return playerMap;
	}
	
	/**
	 * Load all SpellCasters from the data file.
	 */
	@SuppressWarnings("unchecked")
	public static void loadSpellCasters() {
		plugin = WbsMagic.getInstance();
		final String path = plugin.getDataFolder() + File.separator +"player.data";
		logger = plugin.getLogger();

		logger.info("Attempting to load SpellCasters... ");
		
		try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(path))) {
			try {
				playerMap = (HashMap<UUID, SpellCaster>) input.readObject();
			} catch (ClassNotFoundException e) {
				logger.warning("A class definition was missing when reading data from the player Magic data file.");
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			logger.warning("The player Magic data file was missing!");
			e.printStackTrace();
		} catch (IOException e) {
			logger.warning("An unknown error occurred while attempting to read the player Magic data file.");
			e.printStackTrace();
		}
		
		for (SpellCaster caster : playerMap.values()) {
			caster.tier = 1;
		}
		
		logger.info("Loaded " + playerMap.size() + " SpellCasters from memory.");
	}
	
	/**
	 * Write all SpellCasters in active memory to the data file.
	 * Will do nothing if there are none loaded.
	 */
	public static void saveSpellCasters() {
		if (playerMap.isEmpty())  {
			logger.info("There was no player data loaded.");
			return;
		}
		
		final String path = plugin.getDataFolder() + File.separator + "player.data";
		for (SpellCaster caster : playerMap.values()) {
			caster.setConcentration(null);
		}
		try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(path))) {
			output.writeObject(playerMap);
		} catch (FileNotFoundException e) {
			logger.info("ERROR: The player Magic data file was missing!");
			e.printStackTrace();
		} catch (IOException e) {
			logger.info("ERROR: An unknown error occured while writing to player Magic data file.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Remove a player from the player map, thereby
	 * stopping them from being saved or loaded in future.
	 * @param p
	 */
	public static void unregisterCaster(Player p) {
		playerMap.remove(p.getUniqueId());
	}
	
	private SpellCaster(UUID uuid) {
		this.uuid = uuid;
		concentration = null;
		cooldown = new HashMap<>();
		jumpCount = 0;
		
		playerMap.put(uuid, this);
	}
	
	/**
	 * Get a new SpellCaster object for a player regardless
	 * of whether or not they already have a caster object.
	 * @param p
	 * @return
	 */
	public static SpellCaster resetCaster(Player p) {
		return (new SpellCaster(p.getUniqueId()));
	}
	
	/**
	 * Get a Predicate that takes an Entity and returns living entities
	 * that are not the specified player.
	 * @param player The player to ignore
	 * @return The Predicate
	 */
	public static final Predicate<Entity> getPredicate(Player player) {
		return new Predicate<Entity>() {
			@Override
			public boolean test(Entity entity) {
				boolean returnVal = false;
				if (entity instanceof LivingEntity) {
					returnVal = true;
					if (entity instanceof Player) {
						if (((Player) entity).equals(player)) {
							returnVal = false;
						}
						if (((Player) entity).getGameMode() == GameMode.SPECTATOR) {
							returnVal = false;
						}
					} else if (entity instanceof ArmorStand) {
						returnVal = false;
					}
					
					if (entity.isDead()) {
						returnVal = false;
					}
				}
				return returnVal;
			}
		};
	}

	
	/*************************/
	/*       Misc Static     */
	/*************************/
	
	/*************************/
	/*     END OF STATIC     */
	/*************************/
	
	private final UUID uuid;
	private final transient Player player = null;
	private int mana = initialMana;
	private int level = 1;
	private transient int tier = 1;
	public transient int jumpCount = 0;
	
	/*
	 * The caster can only have one active concentration spell at
	 * a time, but are able to cast other non-concentration spells
	 * while concentrating on it.
	 */
	private transient SpellType concentration = null;
	
	/*
	 * Like concentration, the caster may be considered to be "casting"
	 * one spell at a time, but cannot cast any other spells while casting.
	 * Casting spells cannot be configured, and cannot also be concentration.
	 * Casting spells must be bound to a shift control, otherwise the configuration
	 * will fail.
	 */
	private transient SpellType casting = null;
	
	// Casting runnable for the spell currently being cast
	private transient WbsRunnable castingRunnable = null;
	
	// Map of wand name to map of spell to localdatetime
	private Map<String, Map<SpellInstance, LocalDateTime>> cooldown = new HashMap<>();
	
	/**
	 * Set the player's level.
	 * @param level The desired level
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * Get the player object that this object represents
	 * @return
	 */
	public Player getPlayer() {
		if (player == null) {
			return Bukkit.getPlayer(uuid);
		} else {
			return player;
		}
	}
	
	/**
	 * Get the UUID of the caster
	 * @return
	 */
	public UUID getUUID() {
		return uuid;
	}

	/**
	 * Set the SpellType that the caster is concentrating on
	 * @param spell The SpellType
	 */
	public void setConcentration(SpellType spell) {
		concentration = spell;
	}
	
	/**
	 * Get the SpellType the caster is concentrating on
	 * @return The caster's concentration
	 */
	public SpellType getConcentration() {
		return concentration;
	}
	
	/**
	 * Get whether or not the player is concentrating on a spell
	 * @return true if they player is concentrating
	 */
	public boolean isConcentrating() {
		return (concentration != null);
	}

	/**
	 * Stop the caster concentrating at a later time
	 * @param ticks The amount of ticks to wait
	 */
	public void stopConcentrationLater(int ticks) {
		new BukkitRunnable() {
			public void run() {
				concentration = null;
			}
		}.runTaskLater(plugin, ticks);
	}

	/**
	 * Stop the caster from concentrating
	 */
	public void stopConcentration() {
		concentration = null;
	}
	
	/**
	 * Check if the caster is concentrating on a given SpellType
	 * @param type The SpellType to check
	 * @return true if the player is concentrating on type
	 */
	public boolean isConcentratingOn(SpellType type) {
		return (concentration == type);
	}
	
	/**
	 * Set the SpellType that the caster is now casting
	 * @param spell The SpellType
	 */
	public void setCasting(SpellType spell, WbsRunnable runnable) {
		if (castingRunnable != null) {
			castingRunnable.cancelSafely();
		}
		casting = spell;
		castingRunnable = runnable;
	}
	
	/**
	 * Get the SpellType the caster is casting
	 * @return The spell being cast.
	 */
	public SpellType getCasting() {
		return casting;
	}
	
	/**
	 * Get whether or not the player is casting a spell right now
	 * @return true if casting a continuous-cast spell
	 */
	public boolean isCasting() {
		return (casting != null);
	}

	/**
	 * Stop the caster concentrating at a later time
	 * @param ticks The amount of ticks to wait
	 */
	public void stopCastingLater(int ticks) {
		new BukkitRunnable() {
			public void run() {
				casting = null;
				if (castingRunnable != null) {
					castingRunnable.cancelSafely();
				}
			}
		}.runTaskLater(plugin, ticks);
	}

	/**
	 * Stop the caster from concentrating
	 */
	public void forceStopCasting() {
		casting = null;
		if (castingRunnable != null) {
			castingRunnable.cancelSafely();
		}
		castingRunnable = null;
	}
	
	/**
	 * Stop the caster from casting their current spell
	 */
	public void stopCasting() {
		casting = null;
		castingRunnable = null;
	}
	
	/**
	 * Check if the caster is casting a given SpellType
	 * @param type The SpellType to check
	 * @return true if the player is casting that spell type
	 */
	public boolean isCasting(SpellType type) {
		return (casting == type);
	}
	
	/**
	 * Add mana to the caster.
	 * @param add The amount of mana to add
	 * @return The amount of mana that was successfully added
	 */
	public int addMana(int add) {
		if (mana > getMaxMana()) {
			return 0;
		}
		int returnInt = 0;
		if (add + mana > getMaxMana()) {
			returnInt = getMaxMana() - mana;
			mana = getMaxMana();
		} else {
			returnInt = mana + add;
			mana = returnInt;
		}
		return returnInt;
	}

	/**
	 * Set the mana of the caster without checking level.
	 * @param newMana The caster's new mana
	 */
	public void setMana(int newMana) {
		mana = newMana;
	}

	/**
	 * Whether the player has at least a certain amount of mana
	 * @param checkMana The amount to check
	 * @return true if the player has checkMana or more mana
	 */
	public boolean hasMana(int checkMana) {
		if (!getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
			return (checkMana <= mana);
		} else {
			return true;
		}
	}

	// returns true if the caster had that much mana, and spends it. Returns false
	// if the caster did not, and leaves mana at current.
	/**
	 * Spend mana for this caster; returns whether or not the caster had enough
	 * @param remove The amount of mana to take
	 * @return true if the caster had enough mana.
	 */
	public boolean spendMana(int remove) {
		boolean success;
		boolean isCreative = false;
		if (!getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
			success = (mana - remove >= 0);
		} else {
			success = true;
			isCreative = true;
		}
		if (!isCreative) {
			if (success) {
				mana -= remove;
			}
		}
		return success;
	}

	/**
	 * Send an action bar to the caster with formatting handled
	 * @param message The action bar message to send
	 * @see wbs.utils.util.plugin.WbsPlugin#sendMessage(String, CommandSender) sendMessage for formatting information
	 */
	public void sendActionBar(String message) {
		plugin.sendActionBar(message, getPlayer());
	}
	
	/**
	 * Increment the caster's current tier, looped when exceeding the wands max level.
	 * @param wand The wand the player changed tiers on
	 * @return true if the tier was changed, false if the wand does not have tiers
	 */
	public boolean nextTier(MagicWand wand) {
		Player p = getPlayer();
		p.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, p.getLocation().add(0, 1.5, 0), 10, 0.3, 0.7, 0.3, 1);
		tier++;
		if (wand.getMaxTier() == 1) {
			if (wand.doErrorMessages()) {
				sendActionBar("This wand does not have tiers.");
			}
			tier = 1;
			return false;
		} else if (tier > wand.getMaxTier()) {
			tier = 1;
			sendActionBar("Tier reverted to 1!");
		} else {
			sendActionBar("Tier " + tier + " spell primed!");
		}
		return true;
	}

	/**
	 * Gets the caster's mana
	 * @return The caster's mana
	 */
	public int getMana() {
		return mana;
	}

	/**
	 * Gets the caster's maximum mana
	 * @return The max mana
	 */
	public int getMaxMana() {
		return (level+1)*(level+1)*500;
	}

	/**
	 * Gets the caster's current level
	 * @return The caster's level
	 */
	public int getLevel() {
		return level;
	}
	
	/**
	 * Gets what tier the caster is currently at
	 * @return The current tier
	 */
	public int getTier() {
		return tier;
	}
	
	/**
	 * Get the cooldowns for this caster on a given wand
	 * @param wand The wand to find cooldowns for
	 * @return A map of spells to the last time the spelltype was used.
	 */
	public Map<SpellInstance, LocalDateTime> getCooldownsFor(MagicWand wand) {
		if (cooldown == null) {
			cooldown = new HashMap<>();
		}
		Map<SpellInstance, LocalDateTime> wandCooldown = cooldown.get(wand.getWandName());
		if (wandCooldown == null) {
			wandCooldown = new HashMap<>();
			cooldown.put(wand.getWandName(), wandCooldown);
		}
		
		return wandCooldown;
	}

	/**
	 * Set the current cooldown of a given spell
	 * @param spell The spell to set the cooldown for
	 */
	public void setCooldownNow(SpellInstance spell, MagicWand wand) {
		if (cooldown == null) {
			cooldown = new HashMap<>();
		}
		Map<SpellInstance, LocalDateTime> wandCooldown = getCooldownsFor(wand);
		if (wandCooldown == null) {
			wandCooldown = new HashMap<>();
			cooldown.put(wand.getWandName(), wandCooldown);
		}	
		
		wandCooldown.put(spell, LocalDateTime.now());
	}
	
	/**
	 * Check if a spell may be cast on a certain wand by this caster, or
	 * if it is currently on cooldown.
	 * @param spell The spell to check
	 * @param wand The wand being used to cast
	 * @return true if the spell may be cast
	 */
	public boolean offCooldown(SpellInstance spell, MagicWand wand) {
		if (cooldown == null) {
			cooldown = new HashMap<>();
			return true;
		}
		
		Map<SpellInstance, LocalDateTime> wandCooldown = getCooldownsFor(wand);
		
		if (wandCooldown.isEmpty() || !wandCooldown.containsKey(spell)) {
			return true;
		} else {
			LocalDateTime lastUse = wandCooldown.get(spell);
			Duration between = Duration.between(lastUse, LocalDateTime.now());
			double timeAgo = between.toMillis();
			if (timeAgo <= spell.getCooldown()*1000) {
				LocalDateTime unlockTime = lastUse.plusNanos((long) (spell.getCooldown() * 1000000000.0));
				Duration timeLeft = Duration.between(LocalDateTime.now(), unlockTime);
				String timeLeftString = WbsStringify.toString(timeLeft, false);
				sendActionBar("You can use that again in " + timeLeftString);
				return false;
			} else {
				return true;
			}
		}
	}
	
	/**
	 * Send a formatted message with "&" colour codes,
	 * where "&w" becomes the configured error colour,
	 * "&h" becomes the configured highlight colour, and
	 * "&r" resets to the configured main colour.
	 * @param message The message to send
	 */
	public void sendMessage(String message) {
		plugin.sendMessage(message, getPlayer());
	}
	
	/**
	 * Get the player's username
	 * @return The player's username
	 */
	public String getName() {
		return (getPlayer().getName());
	}
	

	private void badWand() {
		Player p = getPlayer();
		ItemStack wandItem = p.getInventory().getItemInMainHand();
		p.getWorld().dropItemNaturally(p.getLocation(), wandItem);
		p.getInventory().removeItem(wandItem);
		p.getWorld().spawnParticle(Particle.SMOKE_LARGE, p.getLocation(), 40, 0.6, 1, 0.6, 0.2);
		final double force = 1.5;
		
		double x, y, z;
		double yaw = Math.toRadians(p.getLocation().getYaw());

		y = force/2;
		x = force * Math.cos(yaw + (Math.PI/2));
		z = force * Math.sin(yaw + (Math.PI/2));
		
		p.setVelocity(new Vector(-x, y, -z));
	}
	
	public void showManaChange(int cost) {
		if (cost != 0) {
			if (cost < 0) {
				sendActionBar("+" + manaDisplay(Math.abs(cost)));
			} else {
				sendActionBar("-" + manaDisplay(cost));
			}
		}
	}
	
	/**
	 * Get the mana cost formatted with the mana symbol
	 * @param cost The mana cost to have in the string
	 * @return The formatted string
	 */
	public String manaDisplay(int cost) {
		return cost + "&5✴";
	}
	
	/**
	 * Cast a spell based on a WandControl binding on the given wand
	 * @param combo The control the caster is using
	 * @param wand The MagicWand the caster is casting with
	 * @param interactionTarget The target to use, if the spell is a TargetedSpell.
	 * May be null to use the spells targeter.
	 * @return true if the spell was cast, false if:
	 * <ul>
	 * <li>There was no spell at that control on the given wand at the current tier,</li>
	 * <li>The wand's level was higher than the caster's level,</li>
	 * <li>The spell's level on that wand was higher than the caster's level,</li>
	 * <li>The spell was a concentration spell, and the caster was already concentrating,</li>
	 * <li>The spell was on cooldown for that wand,</li>
	 * <li>The caster was casting a spell,</li>
	 * <li>The caster did not have enough mana to cast the spell, or</li>
	 * <li>SpellCastEvent was cancelled externally</li>
	 */
	public boolean castSpellOn(WandControl combo, MagicWand wand, LivingEntity interactionTarget) {
		if (wand.getPermission() != null && !wand.getPermission().equals("")) {
			if (!getPlayer().hasPermission(wand.getPermission())) {
				sendActionBar("&wYou do not have permission to use this.");
				return false;
			}
		}
		
		if (tier > wand.getMaxTier()) {
			tier = 1;
		}
		SpellInstance spell = wand.getBinding(tier, combo);
		
		if (spell == null) {
			if (!combo.isCombined()) {
				tier = 1;
				if (wand.doErrorMessages()) {
					sendActionBar("That spell combination is not defined for this wand!");
				}
				return false;
			} else {
				if (combo.isDown()) {
					return castSpellOn(combo.directionless(), wand, interactionTarget);
				} else if (combo.isEntity()) {
					return castSpellOn(combo.nonEntity(), wand, interactionTarget);
				} else if (combo.isShift()) {
					return castSpellOn(combo.shiftless(), wand, interactionTarget);
				} else {
					return castSpellOn(combo.uncombined(), wand, interactionTarget); 
				}
			}
		}
		
		SpellPrepareEvent prepareEvent = new SpellPrepareEvent(this, spell);
		Bukkit.getPluginManager().callEvent(prepareEvent);
		
		if (prepareEvent.isCancelled()) {
			return false;
		}
		
		if (wand.getLevel() > level) {
			badWand();
			sendActionBar("&cThe wand was too powerful!");
			return false;
		}
		
		tier = 1;
		if (spell.isConcentration()) {
			if (concentration != null) {
				sendActionBar("&cYou are already concentrating on a " + concentration.getName() + " spell!");
				return false;
			}
		}
		
		if (isCasting()) {
			sendActionBar("&cYou are already casting " + casting.getName() + "!");
			return false;
		}
		
		if (wand.getLevel() > level) {
			sendActionBar("&cThis wand unlocks at level " + wand.getLevel());
			return false;
		}
		if (spell.getRequiredLevel() > level) {
			sendActionBar("&cThis spell unlocks at level " + spell.getRequiredLevel());
			return false;
		}
		
		if (offCooldown(spell, wand)) {
			
			int cost = spell.getCost();
			if (!hasMana(cost)) {
				sendActionBar("&cNot enough mana!");
			} else {
				
				SpellCastEvent castEvent = new SpellCastEvent(this, spell);
				if (castEvent.isCancelled()) {
					return true;
				}

				boolean success = false;
				if (spell instanceof TargetedSpell) {
					success = ((TargetedSpell) spell).cast(this, interactionTarget);
				} else {
					success = spell.cast(this);
				}
				
				if (success) {
					if (ignoreNextCooldown) {
						ignoreNextCooldown = false;
					} else {
						setCooldownNow(spell, wand);
					}

					if (ignoreNextCost) {
						ignoreNextCost = false;
					} else {
						spendMana(cost);
						showManaChange(cost);
					}
					
					if (spell.consumeWand()) {
						PlayerInventory inv = getPlayer().getInventory();
						ItemStack heldWandItem = inv.getItemInMainHand();
						heldWandItem.setAmount(heldWandItem.getAmount() - 1);
						inv.setItemInMainHand(heldWandItem);
					}
				}
				return true;
			}
			
		}
		return false;
	}

	private boolean ignoreNextCooldown;
	public void ignoreNextCooldown() {
		ignoreNextCooldown = true;
	}

	private boolean ignoreNextCost;
	public void ignoreNextCost() {
		ignoreNextCost = true;
	}
	
	/**
	 * Overload of {@link SpellCaster#castSpellOn(WandControl, MagicWand, LivingEntity)}
	 * with null value for interactionTarget.
	 * @param combo The control the caster is using
	 * @param wand The MagicWand the caster is casting with
	 * @return See {@link SpellCaster#castSpellOn(WandControl, MagicWand, LivingEntity)}
	 */
	public boolean castSpell(WandControl combo, MagicWand wand) {
		return castSpellOn(combo, wand, null);
		
	}
	
	private final transient Multimap<StatusEffectType, StatusEffect> statusEffects = LinkedListMultimap.create();

	public List<StatusEffect> getStatusEffect(StatusEffectType effectType) {
		List<StatusEffect> effects = new LinkedList<>();
		
		Collection<StatusEffect> allEffects = statusEffects.get(effectType);
		if (allEffects == null) {
			return effects;
		}
		for (StatusEffect effect : allEffects) {
			if (effect.getType() == effectType) {
				effects.add(effect);
			}
		}
		
		return effects;
	}
	
	public Multimap<StatusEffectType, StatusEffect> getStatusEffects() {
		return statusEffects;
	}
	
	/**
	 * @return true if the status effect existed on this caster
	 */
	public boolean removeStatusEffect(StatusEffect effect) {
		return statusEffects.remove(effect.getType(), effect);
	}
	
	public void addStatusEffect(StatusEffect effect) {
		statusEffects.put(effect.getType(), effect);
	}
	
	/****************/
	// Message util
	/****************/
	
	/**
	 * Send an actionbar to the caster telling them their concentration was broken.
	 */
	public void concentrationBroken() {
		sendActionBar("Concentration broken!");
	}
	
	/**
	 * Sends an action bar to the caster with a bar representing
	 * how much mana they have.
	 */
	public void checkMana() {
		if (mana >= getMaxMana()) {
			sendActionBar("&6▄▄▄▄▄▄▄▄▄▄");
			return;
		}
		
		char fill = '▄';
		char half = '▖';
		
		int amount = (int) (((double) mana)/getMaxMana()*20);
		
		int filledCount = amount / 2;
		int emptyCount;
		if (amount % 2 == 1) {
			emptyCount = 20 - amount - 1;
		} else {
			emptyCount = 20 - amount;
		}
		emptyCount /= 2;
		char[] filledArray = new char[filledCount];
		Arrays.fill(filledArray, fill);
		String filled = new String(filledArray);

		char[] emptyArray = new char[emptyCount];
		Arrays.fill(emptyArray, fill);
		String empty = new String(emptyArray);
		
		String display;
		if (amount % 2 == 1) {
			display = "&6" + filled + half + " &8" + empty;
		} else {
			display = "&6" + filled + "&8" + empty;
		}
		
		sendActionBar(display);
	}

	/*************************/
	/*    Targeting Utils    */
	/*************************/

	/**
	 * Gets a unit vector in the direction the caster is facing
	 * @return The facing vector
	 */
	public Vector getFacingVector() {
		return getFacingVector(1);
	}

	/**
	 * Gets a vector in the direction the caster is facing scaled to the
	 * given magnitude
	 * @param magnitude The scale of the resulting vector
	 * @return The facing vector
	 */
	public Vector getFacingVector(double magnitude) {
		return WbsEntities.getFacingVector(getPlayer(), magnitude);
	}
	
	/**
	 * Get living entities within a radius of a given location
	 * @param radius The radius around the location to check in
	 * @param includeSelf Whether or not to include the caster
	 * @return A Collection of LivingEntities.
	 */
	public Set<LivingEntity> getNearbyLiving(double radius, boolean includeSelf) {
		return WbsEntities.getNearbyLiving(getPlayer(), radius, includeSelf);
	}

	/**
	 * Get's the location at the block the player is looking at.
	 * @param range The maximum distance away from the player the block may be
	 * @return The target location. null if there was no block in range
	 */
	public Location getTargetPos(double range) {
		return WbsEntities.getTargetPos(getPlayer(), range);
	}

	/*************************/
	/*     Movement Utils    */
	/*************************/

	/**
	 * Pushes the caster in the direction they're facing at a given speed
	 * @param speed The new speed
	 */
	public void push(double speed) {
		WbsEntities.push(getPlayer(), speed);
	}

	/**
	 * Pushes the caster according to a vector
	 * @param direction The new velocity vector
	 */
	public void push(Vector direction) {
		WbsEntities.push(getPlayer(), direction);
	}
	

	/**
	 * Teleports the caster a given distance in the direction they're looking, ensuring
	 * a safe landing (cannot land in non-solid blocks)
	 * @param distance The distance to teleport
	 * @return true if the blink was successful, false if there was no safe landing spot
	 * found
	 */
	public boolean blink(double distance) {
		return WbsEntities.blink(getPlayer(), distance);
	}
	
	/*************************/
	/*      Damage Utils     */
	/*************************/

	private transient SpellInstance currentDamageSpell = null;
	public boolean isDealingSpellDamage() {
		return currentDamageSpell != null;
	}
	
	/**
	 * @return The spell being used for damage currently. May be null.
	 */
	public SpellInstance getCurrentDamageSpell() {
		return currentDamageSpell;
	}
	
	/**
	 * Make the caster deal damage to a living entity
	 * @param target The entity to damage
	 * @param damage The amount of damage to deal
	 * @param spell The spell causing the damage
	 */
	public void damage(LivingEntity target, double damage, SpellInstance spell) {
		damage(target, damage, getPlayer(), spell);
	}
	
	/**
	 * Deal damage with a spell. Important to use, as it
	 * makes the damage properly register spell damage events.
	 * It is also needed to get last spell damage for death messages.
	 * @param target The entity to damage
	 * @param damage The amount of damage to deal
	 * @param player The player to deal the damage
	 */
	public void damage(LivingEntity target, double damage, Player player, SpellInstance spell) {
		currentDamageSpell = spell;

		WbsEntities.damage(target, damage, player);
		
		currentDamageSpell = null;
	}
	
	/*************************/
	/*       Misc Utils      */
	/*************************/

	/**
	 * Get a Predicate that takes an Entity and returns living entities
	 * that are not the caster.
	 * @return The predicate
	 */
	public final Predicate<Entity> getPredicate() {
		Player player = getPlayer();
		return getPredicate(player);
	}

	/*************************/
	/*    Player Wrappers    */
	/*************************/

	public boolean isSneaking() {
		return getPlayer().isSneaking();
	}

	/**
	 * Gets the caster's current location
	 * @return The caster's location
	 */
	public Location getLocation() {
		return getPlayer().getLocation();
	}

	/**
	 * Gets the caster's current eye location
	 * @return The caster's eye location
	 */
	public Location getEyeLocation() {
		return getPlayer().getEyeLocation();
	}
}

