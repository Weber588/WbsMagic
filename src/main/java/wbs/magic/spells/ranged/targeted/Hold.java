package wbs.magic.spells.ranged.targeted;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellSettings;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.SpellCaster;

import wbs.utils.util.particles.RingParticleEffect;

@Spell(name = "Hold",
		cost = 15,
		cooldown = 15,
		description = "The creature that the caster is looking at is held in place, unable to move for the duration of the spell. If the target is in the air, they will be pulled down to the ground and prevented from taking off again."
)
@SpellSettings(canBeConcentration = true)
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 4)
@SpellOption(optionName = "glow", type = SpellOptionType.BOOLEAN, defaultBool = true, aliases = {"glowing"})
public class Hold extends TargetedSpell {
	public Hold(SpellConfig config, String directory) {
		super(config, directory);

		duration = config.getDouble("duration", duration);
		glowing = config.getBoolean("glow", glowing);
		
		effect = new RingParticleEffect();
		effect.setRadius(0.5);
		effect.setOptions(Material.GRAVEL.createBlockData());
	}

	private double duration = 4; // in seconds
	private boolean glowing = true;
	
	private final PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 2, 100, false, false);
	private final PotionEffect glow = new PotionEffect(PotionEffectType.GLOWING, 2, 0, false, false);
	private final Particle display = Particle.FALLING_DUST;
	
	private RingParticleEffect effect; // Is mutable, but will be cloned
	
	@Override
	public <T extends LivingEntity> boolean preCast(SpellCaster caster, Set<T> targets) {
		if (isConcentration) {
			caster.setConcentration(this);
		}
		return false;
	}
	
	@Override
	public void castOn(SpellCaster caster, LivingEntity target) {
			
		if (target instanceof Player) {
			SpellCaster.getCaster((Player) target).sendActionBar("&h" + caster.getName() + "&r cast &hHold&r on you!");
		}
		
		RingParticleEffect localEffect = effect.clone(); // Don't change the orientation for other 

		localEffect.setAmount((int) (target.getWidth()*10));
		localEffect.setRadius(target.getWidth());
		
		new BukkitRunnable() {
			double i = 0;
			final Vector holdVec = new Vector(0, -10, 0);
			
			Location currentLoc = target.getLocation();
			
			final double height = target.getHeight();
			@Override
            public void run() {
				if (i < duration*20) {
					
					if (target.isDead()) {
						caster.stopConcentration();
						cancel();
					} else if (isConcentration && !caster.isConcentratingOn(Hold.this)) {
						caster.concentrationBroken();
						cancel();
						return;
					}

					target.removePotionEffect(PotionEffectType.SLOW);
					target.addPotionEffect(slow);
					
					if (glowing) {
						target.removePotionEffect(PotionEffectType.GLOWING);
						target.addPotionEffect(glow);
					}

					currentLoc = target.getLocation();
					currentLoc.setY(height + currentLoc.getY() + 0.5);
					
					localEffect.setRotation(i);
					localEffect.buildAndPlay(display, currentLoc);
					
					target.setVelocity(holdVec);
				} else {
					if (isConcentration) {
						caster.stopConcentration();
					}
					cancel();
				}
				i++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDuration: &7" + duration + " seconds";
		asString += "\n&rGlow: &7" + glow;

		return asString;
	}
}
