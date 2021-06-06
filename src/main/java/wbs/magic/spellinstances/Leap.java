package wbs.magic.spellinstances;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import wbs.magic.annotations.SpellSound;
import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.FailableSpell;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.wrappers.SpellCaster;

@Spell(name = "Leap",
		cost = 10,
		cooldown = 0, // Cooldown is 0, but the player must touch the ground before using it again
		description = "The caster is thrown in the direction they're facing, and takes no fall damage."
)
@SpellSound(sound = Sound.ENTITY_ILLUSIONER_CAST_SPELL)
@SpellSound(sound = Sound.ENTITY_PHANTOM_FLAP, volume = 0.5F)
@FailableSpell("If the player is in the air and has exceeded their maximum number of jumps, they will be unable to use the spell until they touch the ground again.")
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 1.5)
@SpellOption(optionName = "max-jumps", type = SpellOptionType.INT, defaultInt = 0, aliases = {"maxjumps", "jumps"})
@SpellOption(optionName = "infinite-jumps", type = SpellOptionType.BOOLEAN, defaultBool = false)
public class Leap extends SpellInstance {

	public Leap(SpellConfig config, String directory) {
		super(config, directory);

		maxJumps = config.getInt("max-jumps", maxJumps);
		
		speed = config.getDouble("speed", speed);

		infiniteJumps = config.getBoolean("infinite-jumps", infiniteJumps);
	}

	private boolean infiniteJumps = false;
	private int maxJumps = 0;
	private double speed = 1.5;

	public boolean cast(SpellCaster caster) {
		Player player = caster.getPlayer();
		World world = player.getWorld();
		boolean onGround = player.isOnGround();
		if (infiniteJumps) {
			world.spawnParticle(Particle.SPELL_INSTANT, player.getLocation(), 160, 0, 0, 0, 0.5);

			caster.push(speed);
			return true;
		} else {
			if (!onGround) {
				if (caster.jumpCount >= maxJumps) {
					return false;
				} else {
					caster.jumpCount++;
				}
			} else {
				caster.jumpCount++;
			}
		}
		world.spawnParticle(Particle.SPELL_INSTANT, player.getLocation(), 160, 0, 0, 0, 0.5);

		if (onGround) {
			caster.jumpCount = 0;
			new BukkitRunnable() {
				int escape = 0;
				@Override
	            public void run() {
					if (!player.isOnGround()) {
						world.spawnParticle(Particle.SPELL_INSTANT, player.getLocation().add(0, 1, 0), 10, 0.4, 1, 0.4, 0);
						escape++;
						if (escape > 1000) {
							caster.jumpCount = 0;
							cancel();
						}
					} else {
						caster.jumpCount = 0;
						cancel();
					}
	            }
	        }.runTaskTimer(plugin, 2L, 2L);
		}

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
