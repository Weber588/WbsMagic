package wbs.magic.spellinstances;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.*;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.WbsEntities;
import wbs.utils.util.particles.RingParticleEffect;

@Spell(name = "Water Walk",
		cost = 10,
		cooldown = 5,
		description = "The caster is pulled into a current, moving in the direction the player is looking for a short time."
)
@SpellSettings(canBeConcentration = true)
@RestrictWandControls(dontRestrictLineOfSight = true)
@FailableSpell("If the caster is not in the water at any point for the duration of the spell, the spell will fail.")
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 2)
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 1)
public class WaterWalkSpell extends SpellInstance {

	public WaterWalkSpell(SpellConfig config, String directory) {
		super(config, directory);

		duration = config.getDouble("duration", duration/20) * 20;
		speed = config.getDouble("speed", speed);
	}

	private double duration = 40; // In ticks
	private double speed = 1;
	
	private final Particle particle = Particle.WATER_BUBBLE;
	private final RingParticleEffect effect = (RingParticleEffect) new RingParticleEffect()
										.setRadius(1)
										.setAmount(10);
	
	@Override
	public boolean cast(SpellCaster caster) {
		Player player = caster.getPlayer();
		
		if (!WbsEntities.isInWater(player)) {
			caster.sendActionBar("You are not in water!");
			return false;
		}
		
		RingParticleEffect effectClone = effect.clone();
		
		new BukkitRunnable() {
			int i = 0;
			Vector direction = caster.getFacingVector(speed);
			
	        public void run() {
	        	i++;
	        	if (i > duration || !WbsEntities.isInMaterial(player, Material.WATER)) {
	        		cancel();
	        	}

	    		effectClone.setAbout(caster.getFacingVector());
	    		effectClone.setRotation(i*2);
	        	effectClone.play(particle, WbsEntities.getMiddleLocation(player));
	        	
	        	direction = caster.getFacingVector(speed);
	        	
	        	caster.push(direction);
	        }
	    }.runTaskTimer(plugin, 0L, 1L);
		return true;
	}
}
