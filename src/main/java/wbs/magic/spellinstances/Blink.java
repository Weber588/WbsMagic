package wbs.magic.spellinstances;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.*;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.wrappers.SpellCaster;
import wbs.magic.enums.SpellType;

@Spell(name = "Blink",
		cost = 30,
		cooldown = 5,
		description = "The caster is teleported a short distance in the direction they're facing."
)
@FailableSpell("If the spell is unable to find a safe place to teleport to, the spell will not take effect. Mana will not be consumed.")
@RestrictWandControls(dontRestrictLineOfSight = true)
@SpellOption(optionName = "distance", type = SpellOptionType.DOUBLE, defaultDouble = 10)
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 1.5)
public class Blink extends SpellInstance {
	
	public Blink(SpellConfig config, String directory) {
		super(config, directory);
		
		distance = config.getDouble("distance", distance);
		speed = config.getDouble("speed", speed);
	}

	private double distance = 10;
	private double speed = 1.5;
	
	@Override
	public boolean cast(SpellCaster caster) {
		Location loc = caster.getLocation();
		World world = loc.getWorld();
		
		world.spawnParticle(Particle.DRAGON_BREATH, loc.add(0, 1, 0), 25, 0.15, 0.15, 0.15, 0);
		world.spawnParticle(Particle.SPELL_WITCH, loc, 400, 0.6, 1, 0.6, 0);
		
		boolean success = caster.blink(distance);
		
		if (success) {
			loc = caster.getLocation();
			
			world.spawnParticle(Particle.DRAGON_BREATH, loc.add(0, 1, 0), 25, 0.15, 0.15, 0.15, 0);
			world.spawnParticle(Particle.SPELL_WITCH, loc, 400, 0.6, 1, 0.6, 0);

			// Need to do it after teleporting or it gets cut off for the user
			getCastSound().play(loc);
			caster.push(speed);
		}
		return success;
	}

	@Override
	public SpellType getType() {
		return SpellType.BLINK;
	}
	
	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDistance: &7" + distance;
		
		return asString;
	}
}
