package wbs.magic.spellinstances;

import java.time.Duration;
import java.util.function.Predicate;
import java.util.logging.Logger;

import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import org.jetbrains.annotations.NotNull;
import wbs.magic.MagicSettings;
import wbs.magic.annotations.SpellSettings;
import wbs.magic.spells.RegisteredSpell;
import wbs.magic.spells.SpellConfig;
import wbs.magic.WbsMagic;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.string.WbsStringify;
import wbs.utils.util.WbsSoundGroup;

// Cost and cooldown are added from the @Spell annotation
@SpellOption(optionName = "consume", type = SpellOptionType.BOOLEAN, defaultBool = false)
@SpellOption(optionName = "durability", type = SpellOptionType.INT, defaultInt = 0)
// No concentration; this is added if the SpellSettings option canBeConcentration is set
public abstract class SpellInstance extends WbsMessenger {

	public static Predicate<Entity> VALID_TARGETS_PREDICATE = entity -> {
		boolean returnVal = false;
		if (entity instanceof LivingEntity) {
			returnVal = true;
			if (entity instanceof Player) {
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
	protected final int durability;

	public SpellInstance(SpellConfig config, String directory) {
		super(plugin);
		registeredSpell = config.getSpellClass();
		cooldown = config.getDouble("cooldown");

		cost = config.getInt("cost");

		consume = config.getBoolean("consume");
		durability = config.getInt("durability");

		customName = config.getString("custom-name", registeredSpell.getName());

		SpellSettings settings = registeredSpell.getSettings();
		boolean concentration = false;
		if (settings != null) {
			if (settings.canBeConcentration()) {
				boolean defaultConcentration = settings.concentrationByDefault();
				concentration = config.getBoolean("concentration", defaultConcentration);
			}
		}
		isConcentration = concentration;
	}
	
	/**
	 * Cast the spell with a given caster.
	 * @param caster The caster to make cast the spell
	 * @return true if the spell was successful, false if the spell failed
	 */
	public abstract boolean cast(SpellCaster caster);

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

		if (!customName.equalsIgnoreCase(registeredSpell.getName())) {
			asString += "\n&rCustom name: &h" + customName;
		}

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
		
		return asString;
	}

	@NotNull
	public final RegisteredSpell getRegisteredSpell() {
		return registeredSpell;
	}

	public final String getName() {
		return customName;
	}
}
