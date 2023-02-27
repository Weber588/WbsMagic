package wbs.magic;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.function.Predicate;
import java.time.Duration;
import java.time.LocalDateTime;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.controls.EventDetails;
import wbs.magic.exceptions.PlayerOfflineException;
import wbs.magic.spellmanagement.configuration.ItemCost;
import wbs.magic.spells.ChangeTier;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.events.SpellCastEvent;
import wbs.magic.events.SpellPrepareEvent;
import wbs.magic.spells.SpellInstance;
import wbs.magic.statuseffects.generics.StatusEffect;
import wbs.magic.statuseffects.generics.StatusEffect.StatusEffectType;

import wbs.magic.wand.MagicWand;
import wbs.magic.wand.SpellBinding;
import wbs.utils.util.*;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.string.WbsStringify;

public class SpellCaster implements Serializable {
	private static final long serialVersionUID = 3509120592222799198L;
	private final static int initialMana = 500;
	private static Logger logger;

	private static WbsMagic plugin;
	private static MagicSettings settings;
	public static void setPlugin(WbsMagic plugin) {
		SpellCaster.plugin = plugin;
		settings = plugin.settings;
	}
	
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
			logger.info("ERROR: An unknown error occurred while writing to player Magic data file.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Remove a player from the player map, thereby
	 * stopping them from being saved or loaded in future.
	 * @param p The player to unregister
	 */
	public static void unregisterCaster(Player p) {
		playerMap.remove(p.getUniqueId());
	}
	
	private SpellCaster(UUID uuid) {
		this.uuid = uuid;
		concentration = null;
		cooldown = new HashMap<>();
		jumpCount = 0;
		mana = settings.maxMana;
		
		playerMap.put(uuid, this);
	}
	
	/**
	 * Get a new SpellCaster object for a player regardless
	 * of whether or not they already have a caster object.
	 * @param p The player whose caster to reset
	 * @return The caster that got reset
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
	public static Predicate<Entity> getPredicate(Player player) {
		return entity -> {
			boolean returnVal = SpellInstance.VALID_TARGETS_PREDICATE.test(entity);
			if (entity.equals(player)) {
				returnVal = false;
			}

			return returnVal;
		};
	}

	
	/*************************/
	/*       Misc Static     */
	/*************************/
	
	/*************************/
	/*     END OF STATIC     */
	/*************************/
	
	private final UUID uuid;
	private Player player;
	private int mana = initialMana;
	private transient int tier = 1;
	public transient int jumpCount = 0;
	
	/*
	 * The caster can only have one active concentration spell at
	 * a time, but are able to cast other non-concentration spells
	 * while concentrating on it.
	 */
	private SpellInstance concentration;
	
	/*
	 * Like concentration, the caster may be considered to be "casting"
	 * one spell at a time, but cannot cast any other spells while casting.
	 * Casting spells cannot be configured, and cannot also be concentration.
	 * Casting spells must be bound to a shift control, otherwise the configuration
	 * will fail.
	 */
	private transient SpellInstance casting = null;
	
	// Casting runnable for the spell currently being cast
	private transient WbsRunnable castingRunnable = null;
	
	// Map of wand name to map of spell to localdatetime
	private transient Map<String, Map<SpellInstance, LocalDateTime>> cooldown;

	/**
	 * Reset this player, forcing the next call to use {@link Bukkit#getPlayer(UUID)}
	 */
	public void resetPlayer() {
		player = null;
	}

	/**
	 * @return The player that this object represents
	 */
	@NotNull
	public Player getPlayer() {
		if (player == null) {
			player = Bukkit.getPlayer(uuid);

			if (player == null) {
				throw new PlayerOfflineException();
			}
		}

		return player;
	}
	
	/**
	 * @return The UUID of the player this object represents
	 */
	public UUID getUUID() {
		return uuid;
	}

	/**
	 * Set the SpellType that the caster is concentrating on
	 * @param spell The SpellType
	 */
	public void setConcentration(SpellInstance spell) {
		concentration = spell;
	}
	
	/**
	 * Get the SpellType the caster is concentrating on
	 * @return The caster's concentration
	 */
	@Nullable
	public SpellInstance getConcentration() {
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
	public boolean isConcentratingOn(SpellInstance type) {
		return (concentration == type);
	}
	
	/**
	 * Set the SpellType that the caster is now casting
	 * @param spell The SpellType
	 */
	public void setCasting(SpellInstance spell, WbsRunnable runnable) {
		if (castingRunnable != null) {
			castingRunnable.cancel();
		}
		casting = spell;
		castingRunnable = runnable;
	}
	
	/**
	 * Get the SpellType the caster is casting
	 * @return The spell being cast.
	 */
	@Nullable
	public SpellInstance getCasting() {
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
					castingRunnable.cancel();
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
			castingRunnable.cancel();
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
	public boolean isCasting(SpellInstance type) {
		return (casting == type);
	}

	/**
	 * Gets the caster's mana
	 * @return The caster's mana
	 */
	public int getMana() {
		if (settings.useXPForCost()) {
			return WbsEntities.getExp(getPlayer());
		} else {
			return mana;
		}
	}

	/**
	 * Set the mana of the caster without checking level.
	 * @param newMana The caster's new mana
	 */
	public void setMana(int newMana) {
		if (settings.useXPForCost()) {
			WbsEntities.setExp(getPlayer(), newMana);
		} else {
			mana = newMana;
		}
	}

	/**
	 * Add mana to the caster.
	 * @param add The amount of mana to add
	 * @return The amount of mana that was successfully added
	 */
	public int addMana(int add) {
		if (getMana() > getMaxMana()) {
			return 0;
		}
		int returnInt;
		if (add + getMana() > getMaxMana()) {
			returnInt = getMaxMana() - getMana();
			setMana(getMaxMana());
		} else {
			returnInt = add;
			setMana(getMana() + add);
		}
		return returnInt;
	}

	/**
	 * Whether the player has at least a certain amount of mana
	 * @param checkMana The amount to check
	 * @return true if the player has checkMana or more mana
	 */
	public boolean hasMana(int checkMana) {
		if (!getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
			return (checkMana <= getMana());
		} else {
			return true;
		}
	}

	/**
	 * Spend mana for this caster; returns whether or not the caster had enough
	 * @param remove The amount of mana to take
	 * @return true if the caster had enough mana.
	 */
	public boolean spendMana(int remove) {
		boolean success;
		boolean isCreative = false;
		if (!getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
			success = (getMana() - remove >= 0);
		} else {
			success = true;
			isCreative = true;
		}
		if (!isCreative) {
			if (success) {
				setMana(getMana() - remove);
			}
		}
		return success;
	}

	public void showManaLoss(int cost) {
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
		if (settings.useXPForCost()) {
			return cost + "&a◉";
		} else {
			return cost + "&5✴";
		}
	}

	public String manaName() {
		if (settings.useXPForCost()) {
			return "exp";
		} else {
			return "mana";
		}
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
			if (wand.preventDrops()) {
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

	public void setTier(int tier) {
		this.tier = tier;
	}

	/**
	 * Gets the caster's maximum mana
	 * @return The max mana
	 */
	public int getMaxMana() {
		if (settings.useXPForCost()) {
			return Integer.MAX_VALUE;
		} else {
			// TODO: Make this configurable in config.yml
			return 500;
		}
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

		return cooldown.computeIfAbsent(wand.getWandName(), key -> new HashMap<>());
	}

	/**
	 * Set the current cooldown of a given spell
	 * @param spell The spell to set the cooldown for
	 */
	public void setCooldownNow(@NotNull SpellInstance spell, @NotNull MagicWand wand) {
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
	public boolean offCooldown(SpellInstance spell, MagicWand wand, boolean sendErrors) {
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
			if (timeAgo < spell.getCooldown()*1000) {
				if (sendErrors) {
					LocalDateTime unlockTime = lastUse.plusNanos((long) (spell.getCooldown() * 1000000000.0));
					Duration timeLeft = Duration.between(LocalDateTime.now(), unlockTime);
					String timeLeftString = WbsStringify.toString(timeLeft, false);

					// Don't display if it's rounded to actual cooldown
					double cooldownFromDisplay = WbsMath.roundTo(timeLeft.toMillis() / 1000.0, 2);
					if (cooldownFromDisplay != WbsMath.roundTo(spell.getCooldown(), 2)) {
						sendActionBar("You can use that again in " + timeLeftString);
					}
				}
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
	
	/**
	 * Cast a spell based on a WandControl binding on the given wand
	 * @param eventDetails The details of the event that caused the casting
	 * @param binding The binding that was found on the wand
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
	public boolean castSpell(EventDetails eventDetails, SpellBinding binding, MagicWand wand) {
		CastingContext context = new CastingContext(eventDetails, binding, this);
		context.setWand(wand);

		SpellInstance spell = binding.getSpell();
		SpellPrepareEvent prepareEvent = new SpellPrepareEvent(this, spell);
		Bukkit.getPluginManager().callEvent(prepareEvent);
		
		if (prepareEvent.isCancelled()) {
			return false;
		}

		if (spell.getClass() != ChangeTier.class) {
			tier = 1;
		}

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

		if (!offCooldown(binding.getSpell(), wand, true)) {
			return false;
		}

		if (!ignoreNextCost) {
			int cost = spell.getCost();
			if (!hasMana(cost)) {
				sendActionBar("&cNot enough " + manaName() + "!");
				return false;
			}

			ItemCost itemCost = spell.getItemCost();
			if (itemCost.isActive() && !itemCost.checkPlayer(getPlayer())) {
				sendActionBar("&wMissing components! &h"
						+ itemCost);
				return false;
			}
		}

		SpellCastEvent castEvent = new SpellCastEvent(this, spell, wand);
		if (castEvent.isCancelled()) {
			return false;
		}

		boolean success = spell.cast(context);

		if (success) afterCast(spell, wand);

		return true;
	}

	private void afterCast(SpellInstance spell, MagicWand wand) {
		spell.getCastSound().play(getLocation());

		if (ignoreNextCooldown) {
			ignoreNextCooldown = false;
		} else {
			setCooldownNow(spell, wand);
		}

		if (ignoreNextCost) {
			ignoreNextCost = false;
		} else {
			spendMana(spell.getCost());
			showManaLoss(spell.getCost());

			ItemCost itemCost = spell.getItemCost();
			if (itemCost.isActive()) {
				itemCost.takeFromPlayer(getPlayer());
				if (spell.getCost() > 0) {
					sendActionBar("Consumed " + itemCost + "!");
				}
			}
		}

		if (spell.consumeWand()) {
			if (ignoreNextConsume) {
				ignoreNextConsume = false;
			} else {
				PlayerInventory inv = getPlayer().getInventory();
				ItemStack heldWandItem = inv.getItemInMainHand();
				heldWandItem.setAmount(heldWandItem.getAmount() - 1);
				inv.setItemInMainHand(heldWandItem);
			}
		}

		if (spell.getDurability() >= 1) {
			PlayerInventory inv = getPlayer().getInventory();
			ItemStack heldWandItem = inv.getItemInMainHand();

			ItemMeta meta = heldWandItem.getItemMeta();
			if (meta instanceof Damageable) {
				Damageable damageable = (Damageable) meta;

				int damage = damageable.getDamage() + spell.getDurability();
				damageable.setDamage(damage);

				heldWandItem.setItemMeta(meta);

				int maxDamage = heldWandItem.getType().getMaxDurability();
				if (damage >= maxDamage) {
					heldWandItem.setAmount(heldWandItem.getAmount() - 1);
				}
			}
		}
	}

	private boolean ignoreNextCooldown;
	public void ignoreNextCooldown() {
		ignoreNextCooldown = true;
	}

	private boolean ignoreNextCost;
	public void ignoreNextCost() {
		ignoreNextCost = true;
	}

	private boolean ignoreNextConsume;
	public void ignoreNextConsume() {
		ignoreNextConsume = true;
	}

	private Multimap<StatusEffectType, StatusEffect> statusEffects = LinkedListMultimap.create();

	public List<StatusEffect> getStatusEffect(StatusEffectType effectType) {
		List<StatusEffect> effects = new LinkedList<>();
		if (statusEffects == null) {
			statusEffects = LinkedListMultimap.create();
		}
		
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

	private boolean isBreaking = false;
	public void setIsBreaking(boolean breaking) {
		isBreaking = breaking;
	}

	public boolean isBreaking() {
		return isBreaking;
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
		if (settings.useXPForCost()) {
			sendActionBar("&6Current exp: " + getMana());
			return;
		}

		if (getMana() >= getMaxMana()) {
			sendActionBar("&6▄▄▄▄▄▄▄▄▄▄");
			return;
		}
		
		char fill = '▄';
		char half = '▖';
		
		int amount = (int) (((double) getMana())/getMaxMana()*20);
		
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
		RadiusSelector<LivingEntity> selector = new RadiusSelector<>(LivingEntity.class)
				.setRange(radius)
				.setPredicateRaw(SpellInstance.VALID_TARGETS_PREDICATE);

		if (includeSelf) {
			return new HashSet<>(selector.select(getPlayer()));
		} else {
			return new HashSet<>(selector.selectExcluding(getPlayer()));
		}
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

