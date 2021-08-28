package wbs.magic.spells;

import java.util.Collection;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.SpellCaster;

import wbs.magic.targeters.RadiusTargeter;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.particles.ElectricParticleEffect;

@Spell(name = "Void Step",
		cost = 30,
		cooldown = 15,
		description = "The caster teleports a short distance in the direction they were facing, and stuns all nearby creatures that were left behind. Stunned creatures move slowly, cannot see the caster, and may be affected by blindness, nausea, or (in the case of other spellcasters) losing concentration."
)
@SpellSettings(isContinuousCast = true)
@FailableSpell("If the spell is unable to find a safe place to teleport to, the spell will not take effect. Mana will not be consumed.")
@SpellSound(sound = Sound.ENTITY_WITHER_DEATH, pitch = 2, volume = 0.5F)
@DamageSpell(defaultDamage = 2)
@RestrictWandControls(dontRestrictLineOfSight = true)
@SpellOption(optionName = "distance", type = SpellOptionType.DOUBLE, defaultDouble = 10)
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 1.5)
public class VoidStep extends SpellInstance {

	public VoidStep(SpellConfig config, String directory) {
		super(config, directory);

		distance = config.getDouble("distance");
		speed = config.getDouble("speed");
		damage = config.getDouble("damage");
		
		effect.setTicks(40);
		effect.setAmount(3);
		effect.setRadius(0.6);
		Particle.DustOptions data = new Particle.DustOptions(Color.fromRGB(160, 120, 255), 0.4F);
		effect.setOptions(data);
	}

	private final double distance;
	private final double speed;
	private final double damage;

	private final ElectricParticleEffect effect = new ElectricParticleEffect();
	private final RadiusTargeter radiusTargeter = new RadiusTargeter(6);
	
	@Override
	public boolean cast(SpellCaster caster) {
		Player player = caster.getPlayer();
		
		// TODO Update to new particle syntax
		Location oldPos = player.getLocation();
	//	ParticleEffect.spiral(oldPos, Particle.SMOKE_NORMAL, 15, 1, 2, 0.3);
	//	ParticleEffect.spiral(oldPos.add(0, 2, 0), Particle.CRIT_MAGIC, 15, 1, 2, 0.3, false, new Vector(0, -1, 0));
		
		if (caster.blink(distance)) {
			caster.push(speed);
			getCastSound().play(player.getLocation());
			
			effect.play(Particle.REDSTONE, WbsEntities.getMiddleLocation(player));

			Collection<LivingEntity> entities = radiusTargeter.getTargets(caster, oldPos);
			
			for (LivingEntity target : entities) {
				PotionEffect effect = new PotionEffect(PotionEffectType.BLINDNESS, (int) (Math.random() * 200), 0);
            	target.addPotionEffect(effect);
            	
            	effect = new PotionEffect(PotionEffectType.SLOW, (int) (Math.random() * 200), 0);
            	target.addPotionEffect(effect);
			}
			
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDistance: &7" + distance;
		asString += "\n7rDamage: &7" + damage;
		
		return asString;
	}
}
