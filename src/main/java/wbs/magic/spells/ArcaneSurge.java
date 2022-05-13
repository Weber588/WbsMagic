package wbs.magic.spells;

import java.util.Collection;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.SpellCaster;

import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.WbsSound;
import wbs.utils.util.WbsSoundGroup;
import wbs.utils.util.particles.RingParticleEffect;

// TODO: Add continuous nature
@Spell(name = "Arcane Surge",
		cost = 25,
		cooldown = 5,
		description = "The caster moves forward for a set distance, dealing damage to nearby creatures, and the caster is immune to all damage while moving."
)
@SpellSound(sound = Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED)
@SpellSettings(isContinuousCast = true)
@DamageSpell(defaultDamage = 1)
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 1.5)
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 0.5)
public class ArcaneSurge extends SpellInstance {
	
	private final double duration;
	private final double speed;
	private final double damage;

	private final DustOptions data = new DustOptions(Color.fromRGB(200, 140, 200), 0.6F);
	private final DustOptions dataCore = new DustOptions(Color.fromRGB(120, 0, 144), 1F);

	private final PotionEffect effect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20, 7);

	private final RingParticleEffect particleEffect = (RingParticleEffect) new RingParticleEffect()
															.setAmount(75)
															.setOptions(data);
	private final RingParticleEffect coreEffect = (RingParticleEffect) new RingParticleEffect()
															.setAmount(75)
															.setOptions(dataCore);
	
	private final Particle particle = Particle.REDSTONE;
	
	public ArcaneSurge(SpellConfig config, String directory) {
		super(config, directory);
		
		duration = config.getDouble("duration") * 20;
		speed = config.getDouble("speed");
		damage = config.getDouble("damage");
	}
	
	@Override
	public boolean cast(CastingContext context) {
		SpellCaster caster = context.caster;
		Player player = caster.getPlayer();
		Vector direction = caster.getFacingVector(speed);
		
		player.addPotionEffect(effect);
		
		WbsSound sound = new WbsSound(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED);

		double hitbox = 2;

		RingParticleEffect particleEffect = (RingParticleEffect) this.particleEffect.clone()
											.setRadius(hitbox/2)
											.setAbout(direction);
		RingParticleEffect coreEffect = (RingParticleEffect) this.coreEffect.clone()
											.setRadius(hitbox/4)
											.setAbout(direction);
		
		ArcaneSurge castingSpell = this;
		
		new BukkitRunnable() {
			int i = 0;
			Collection<LivingEntity> entities;
			
			Location playerLoc = player.getEyeLocation();
			
	        public void run() {
	        	playerLoc = player.getEyeLocation();
	        	i++;
	        	if (i > duration) {
	        		cancel();
	        	}
	        	entities = caster.getNearbyLiving(hitbox, false);
				for (LivingEntity e : entities) {
					caster.damage(e, damage, player, castingSpell);
				}
	        	
				sound.play(playerLoc);

				particleEffect.play(particle, playerLoc);
				coreEffect.play(particle, playerLoc);
	        	
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
		asString += "\n&rDamage: &7" + damage;
		
		return asString;
	}
}
