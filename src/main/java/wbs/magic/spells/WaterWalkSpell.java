package wbs.magic.spells;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import wbs.magic.objects.AlignmentType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.SpellCaster;

import wbs.magic.spellmanagement.configuration.options.EnumOptions;
import wbs.magic.spells.framework.CastingContext;
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
@EnumOptions.EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.POSITIVE, enumType = AlignmentType.class)
public class WaterWalkSpell extends SpellInstance {

	public WaterWalkSpell(SpellConfig config, String directory) {
		super(config, directory);

		duration = config.getDouble("duration") * 20;
		speed = config.getDouble("speed");
	}

	private final double duration;
	private final double speed;
	
	private final Particle particle = Particle.BUBBLE;
	private final RingParticleEffect effect = (RingParticleEffect) new RingParticleEffect()
										.setRadius(1)
										.setAmount(10);
	
	@Override
	public boolean cast(CastingContext context) {
		SpellCaster caster = context.caster;
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

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDuration: &7" + duration / 20 + " seconds";
		asString += "\n&rSpeed: &7" + speed;

		return asString;
	}
}
