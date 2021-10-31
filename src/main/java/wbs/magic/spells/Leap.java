package wbs.magic.spells;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import wbs.magic.spellmanagement.configuration.SpellSound;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.FailableSpell;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.SpellCaster;
import wbs.magic.spells.framework.CastingContext;

@Spell(name = "Leap",
		cost = 10,
		cooldown = 0, // Cooldown is 0, but the player must touch the ground before using it again
		description = "The caster is thrown in the direction they're facing, and takes no fall damage."
)
@SpellSound(sound = Sound.ENTITY_ILLUSIONER_CAST_SPELL, volume = 0.5f)
@SpellSound(sound = Sound.ENTITY_PHANTOM_FLAP, volume = 1)
@FailableSpell("If the player is in the air and has exceeded their maximum number of jumps, they will be unable to use the spell until they touch the ground again.")
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 1.5)
@SpellOption(optionName = "max-jumps", type = SpellOptionType.INT, defaultInt = 0, aliases = {"maxjumps", "jumps"})
@SpellOption(optionName = "infinite-jumps", type = SpellOptionType.BOOLEAN, defaultBool = false)
public class Leap extends SpellInstance {

	public Leap(SpellConfig config, String directory) {
		super(config, directory);

		maxJumps = config.getInt("max-jumps");
		
		speed = config.getDouble("speed");

		infiniteJumps = config.getBoolean("infinite-jumps");
	}

	private final boolean infiniteJumps;
	private final int maxJumps;
	private final double speed;

	public boolean cast(CastingContext context) {
		SpellCaster caster = context.caster;
		Player player = caster.getPlayer();
		World world = player.getWorld();

		if (caster.jumpCount > maxJumps && !infiniteJumps) {
			return false;
		}

		world.spawnParticle(Particle.SPELL_INSTANT, player.getLocation(), 160, 0, 0, 0, 0.5);

		if (caster.jumpCount == 0) {
			new BukkitRunnable() {
				int escape = 0;
				@Override
				public void run() {
					world.spawnParticle(Particle.SPELL_INSTANT, player.getLocation().add(0, 1, 0), 10, 0.4, 1, 0.4, 0);

					escape++;

					if (escape > 1000 || (player.isOnGround() && escape >= 5)) {
						caster.jumpCount = 0;
						cancel();
					}
				}
			}.runTaskTimer(plugin, 2L, 2L);
		}

		caster.jumpCount++;

		caster.push(speed);

		return true;
	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rMax jumps: &7" + maxJumps;
		asString += "\n&rSpeed: &7" + speed;
		
		return asString;
	}
}
