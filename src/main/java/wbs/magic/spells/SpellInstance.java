package wbs.magic.spells;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.MagicSettings;
import wbs.magic.SpellCaster;
import wbs.magic.WbsMagic;
import wbs.magic.DamageSource;
import wbs.magic.DamageType;
import wbs.magic.exceptions.UncastableSpellException;
import wbs.magic.objects.AlignmentType;
import wbs.magic.spellmanagement.RegisteredSpell;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.SpellManager;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.spellmanagement.configuration.options.BoolOptions;
import wbs.magic.spellmanagement.configuration.options.BoolOptions.BoolOption;
import wbs.magic.spells.framework.*;
import wbs.magic.wand.SimpleWandControl;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsSoundGroup;
import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.string.WbsStringify;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;

@BoolOption(optionName = "consume", defaultValue = false, saveToDefaults = false)
@SpellOption(optionName = "send-messages", type = SpellOptionType.BOOLEAN, defaultBool = true, saveToDefaults = false)
@SpellOption(optionName = "send-errors", type = SpellOptionType.BOOLEAN, defaultBool = true, saveToDefaults = false)
@SpellOption(optionName = "durability", type = SpellOptionType.INT, defaultInt = 0, saveToDefaults = false)
// No concentration; this is added if the SpellSettings option canBeConcentration is set
public abstract class SpellInstance extends WbsMessenger {

	public static Predicate<Entity> VALID_TARGETS_PREDICATE = entity -> {
		if (!(entity instanceof LivingEntity)) return false;
		if (entity instanceof ArmorStand) return false;

		if (entity instanceof Player) {
			Player player = (Player) entity;

			// Filter out offline players & NPCs
			if (!player.isOnline()) return false;

			if (player.getGameMode() == GameMode.SPECTATOR) return false;
		}

		return !entity.isDead();
	};

	protected static void logError(String error, String directory) {
		MagicSettings settings = MagicSettings.getInstance();
		settings.logError(error, directory);
	}

	protected static Logger logger;
	protected static WbsMagic plugin;
	public static void setPlugin(WbsMagic plugin) {
		SpellInstance.plugin = plugin;
		logger = plugin.getLogger();
	}
	
	// Defaults
	protected final RegisteredSpell registeredSpell;
	private final String customName;
	protected final int cost; // in mana
	protected final double cooldown; // cooldown in seconds
	protected final boolean isConcentration;
	protected final boolean consume; // Whether or not to take the wand item when cast
	protected final boolean sendMessages;
	protected final boolean sendErrors;
	protected final int durability;

	@NotNull
	private final DamageSource damageSource;

	@NotNull
	protected AlignmentType alignmentType;
	protected ItemCost itemCost;

	public SpellInstance(SpellConfig config, String directory) {
		super(plugin);
		registeredSpell = config.getRegistration();
		itemCost = config.getItemCost();
		if (itemCost == null) itemCost = new ItemCost();

		alignmentType = config.getEnum("alignment", AlignmentType.class);

		cooldown = config.getDouble("cooldown");
		cost = config.getInt("cost");

		consume = config.getBoolean("consume");
		sendMessages = config.getBoolean("send-messages");
		sendErrors = config.getBoolean("send-errors");
		durability = config.getInt("durability");

		customName = config.getString("custom-name", registeredSpell.getName());
		if (!customName.equalsIgnoreCase(registeredSpell.getName()) && !customName.isEmpty()) {
			SpellManager.setAlias(registeredSpell, customName, directory + "/custom-name");
		}

		SpellSettings settings = registeredSpell.getSettings();
		boolean concentration = false;
		if (settings != null) {
			if (settings.canBeConcentration()) {
				boolean defaultConcentration = settings.concentrationByDefault();
				concentration = config.getBoolean("concentration", defaultConcentration);
			}
		}

		damageSource = new DamageSource(this);

		DamageSpell damageSpell = registeredSpell.getDamageSpell();
		if (damageSpell != null) {
			for (String damageTypeString : damageSpell.damageTypes()) {
				DamageType type = WbsEnums.getEnumFromString(DamageType.class, damageTypeString);
				damageSource.addType(type);
			}
		}

		isConcentration = concentration;
	}
	
	/**
	 * Cast the spell with a given caster.
	 * @deprecated cast(SpellCaster) is being deprecated in favour
	 * of {@link #cast(CastingContext)}, which allows spells to specify
	 * responses to different events and wand controls.<p/>
	 * For standard behaviour, {@link RawSpell#castRaw(CastingContext)}
	 * should be implemented.
	 * @param caster The caster to make cast the spell
	 * @return true if the spell was successful, false if the spell failed
	 */
	@SuppressWarnings("ConstantConditions")
	@Deprecated
	public boolean cast(SpellCaster caster) {
		if (this instanceof RawSpell) {
			// Shouldn't be using null as arguments for this, but it's not guaranteed to fail, and since this is deprecated
			// it shouldn't be used anyway.
			((RawSpell) this).castRaw(new CastingContext(null, null, caster));
		}
		return false;
	}

	/**
	 * Cast the spell with this context, prioritising based on the
	 * control used.<p/>
	 * For example, controls like {@link SimpleWandControl#PUNCH_ENTITY} are entity
	 * related, and will always run the entity version of the spell if the spell
	 * supports it.<p/>
	 * If the control didn't prioritise a given spell type, the method used will
	 * be chosen in this order based on what the spell supports:
	 * <l>
	 *     <li>Raw</li>
	 *     <li>Entity</li>
	 *     <li>Block</li>
	 *     <li>Location</li>
	 * </l>
	 * @return Whether or not the cast was successful
	 */
	public boolean cast(CastingContext context) {
		// Prioritize which version of the spell is run based on
		// the control, then
		if (context.eventDetails.getOtherEntity() != null) {
			if (this instanceof EntityTargetedSpell) {
				EntityTargetedSpell<?> entitySpell = (EntityTargetedSpell<?>) this;
				return entitySpell.castEntity(context);
			}
			if (this instanceof LocationTargetedSpell) {
				LocationTargetedSpell locationSpell = (LocationTargetedSpell) this;
				return locationSpell.castLocation(context);
			}
		}

		if (context.eventDetails.getBlock() != null) {
			if (this instanceof BlockSpell) {
				BlockSpell blockSpell = (BlockSpell) this;
				return blockSpell.castBlock(context);
			}
			if (this instanceof LocationTargetedSpell) {
				LocationTargetedSpell locationTargetedSpell = (LocationTargetedSpell) this;
				return locationTargetedSpell.castLocation(context);
			}
		}

		if (this instanceof RawSpell) {
			RawSpell rawSpell = (RawSpell) this;
			return rawSpell.castRaw(context);
		}

		if (this instanceof EntityTargetedSpell<?>) {
			EntityTargetedSpell<?> entitySpell = (EntityTargetedSpell<?>) this;
			return entitySpell.castEntity(context);
		}

		if (this instanceof BlockSpell) {
			BlockSpell blockSpell = (BlockSpell) this;
			return blockSpell.castBlock(context);
		}

		if (this instanceof LocationTargetedSpell) {
			LocationTargetedSpell locationSpell = (LocationTargetedSpell) this;
			return locationSpell.castLocation(context);
		}

		throw new UncastableSpellException("A castable spell lacked cast methods. " +
				"If the spell is implementing Castable directly, it should override " +
				"Castable#cast(CastingContext) to prevent this exception.");
	}

	@NotNull
	protected <T extends Enum<T>> T getEnumLogErrors(Class<T> clazz, SpellConfig config, String directory, String option, @NotNull T defaultVal) {
		String checkString = config.getString(option);
		T check = WbsEnums.getEnumFromString(clazz, checkString);

		if (check == null) {
			logError("Invalid " + option.replace('-', ' ') + ": " + checkString,
					directory + "/" + option);
			check = defaultVal;
		}

		return check;
	}

	/**
	 * Get the cost
	 * @return The cost of the spell
	 */
	public int getCost() {
		return cost;
	}

	/**
	 * Get the cooldown in seconds
	 * @return The cooldown in seconds
	 */
	public double getCooldown() {
		return cooldown;
	}

	/**
     * Whether or not a spell is concentration
     * @return True if the spell is concentration
     */
	public boolean isConcentration() {
		return isConcentration;
	}
	
	/**
	 * Whether or not to consume the wand item when successfully cast.
	 * @return true if the wand should be taken on successful cast
	 */
	public boolean consumeWand() {
		return consume;
	}

	/**
	 * If the wand item supports it, durability is how much damage
	 * to do to it.
	 * @return The amount of durability to take from the wand.
	 */
	public int getDurability() {
		return durability;
	}

	/**
	 * Get the default sound to play when casting
	 * @return The sound to play when cast. Returns null if there is no sound to play
	 */
	@NotNull
	public WbsSoundGroup getCastSound() {
		return registeredSpell.getCastSound();
	}

	//************
	// Math methods
	protected Vector scaleVector(Vector original, double magnitude) {
		return (original.clone().normalize().multiply(magnitude));
	}

	protected boolean chance(double percent) {
		if (percent < 0) {
			return false;
		} else if (percent > 100) {
			return true;
		} else {
			return (Math.random() < percent/100);
		}
	}
	
	public String simpleString() {
		return getName();
	}
	
	@Override
	public String toString() {
		String asString = simpleString();

		asString += "\n&rCost: &7" + cost;
		asString += "\n&rCooldown: &7" + WbsStringify.toString(Duration.ofMillis((long)(cooldown * 1000)), false);
		if (isConcentration) {
			asString += "\n&rConcentration: &7true";
		}
		if (consume) {
			asString += "\n&rConsume: &7true";
		}
		if (durability > 0) {
			asString += "\n&rDurability: &7" + durability;
		}

		if (itemCost.isActive()) {
			asString += "\n&rItem cost:";
			asString += "\n    &rMaterial: &7" + WbsEnums.toPrettyString(itemCost.getMaterial());
			asString += "\n    &rAmount: &7" + itemCost.getAmount();
		}
		
		return asString;
	}

	/**
	 * Register a listener if it isn't already registered.
	 * The given listener should be a singleton, not a new
	 * instance, or multiple listeners will be registered.
	 * @param singletonListener The singleton listener (one per class, not per
	 *                          instance)
	 */
	protected void tryRegisterListener(@NotNull Listener singletonListener) {
		Method[] methods = singletonListener.getClass().getMethods();

		Set<HandlerList> registeredIn = new HashSet<>();

		boolean wereAllRegistered = true;
		for (Method method : methods) {
			for (Annotation annotation : method.getDeclaredAnnotations()) {
				if (annotation.annotationType() == EventHandler.class) {
					HandlerList handlers = getHandlerList(method);

					if (handlers != null) {
						boolean isRegistered = Arrays.stream(handlers.getRegisteredListeners())
								.anyMatch(check -> check.getListener().equals(singletonListener));

						if (isRegistered) {
							registeredIn.add(handlers);
						} else {
							wereAllRegistered = false;
						}
					}
				}
			}
		}

		if (!wereAllRegistered) {
			for (HandlerList handlers : registeredIn) {
				handlers.unregister(singletonListener);
			}
			Bukkit.getPluginManager().registerEvents(singletonListener, plugin);
		}
	}

	@Nullable
	private HandlerList getHandlerList(Method method) {
		Parameter[] parameters = method.getParameters();

		if (parameters.length > 0) {
			Parameter eventParam = parameters[0];
			Class<?> eventClass = eventParam.getType();

			if (Event.class.isAssignableFrom(eventClass)) {
				HandlerList handlerList = null;
				try {
					Method handlersMethod = eventClass.getMethod("getHandlerList");

					handlerList = (HandlerList) handlersMethod.invoke(null);
				} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
					e.printStackTrace();
				}

				return handlerList;
			}
		}

		return null;
	}

	@NotNull
	public final RegisteredSpell getRegisteredSpell() {
		return registeredSpell;
	}

	public final String getName() {
		return customName;
	}

	public ItemCost getItemCost() {
		return itemCost;
	}

	public boolean sendMessages() {
		return sendMessages;
	}

	public boolean sendErrors() {
		return sendErrors;
	}

	@NotNull
	public AlignmentType getAlignmentType() {
		return alignmentType;
	}

	@NotNull
	public DamageSource getDamageSource() {
		return damageSource;
	}
}
