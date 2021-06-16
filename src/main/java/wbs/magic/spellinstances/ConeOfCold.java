package wbs.magic.spellinstances;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.*;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.WbsEntities;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.utils.util.WbsRunnable;

@Spell(name = "Cone Of Cold",
		cost = 5,
		cooldown = 30,
		description = "The caster sprays frost in a cone in the direction they're facing until it times out, or concentration is broken. The frost deals damage and slows affected creatures."
)
@SpellSettings(isContinuousCast = true)
@RestrictWandControls(dontRestrictLineOfSight = true)
@DamageSpell(defaultDamage = 1.5, deathFormat = "%victim% was frozen to death by %attacker%!")
public class ConeOfCold extends SpellInstance {

	public ConeOfCold(SpellConfig config, String directory) {
		super(config, directory);

		damage = config.getDouble("damage", damage);
		
		effect.setSpeed(0.1);
		effect.setVariation(2);
		effect.setAmount(15);
		effect.setRadius(0);
	}

	private double damage = 1.5; // per second
	
	private final RingParticleEffect effect = new RingParticleEffect();
	private final Particle particle = Particle.EXPLOSION_NORMAL;

	private final PotionEffect potionEffect = new PotionEffect(PotionEffectType.SLOW, 100, 0);

	@Override
	public boolean cast(SpellCaster caster) {
		Player player = caster.getPlayer();

		double damagePerTick = damage / 2; // Divide by 2 because mobs can take damage once every 10 ticks
		RingParticleEffect localEffect = effect.clone();
		
		WbsRunnable runnable = new WbsRunnable() {
			Location damageCenter;
			Collection<LivingEntity> entities;
			
			final int sustain = (int) (cost / 20);
			
			final double hitbox = 3.5;
			int spent = 0;
            public void run() {
            	if (!player.isSneaking()) {
        			cancel();
        		}

    			if (caster.spendMana(sustain)) {
    				spent += sustain;
    				caster.sendActionBar("-" + caster.manaDisplay(spent));
    			} else {
    				cancel();
    			}

            	if (!caster.isCasting(ConeOfCold.this)) {
            		cancel();
            	}

            	if (isCancelled()) {
            		caster.stopCasting();
					//	caster.setCooldownNow(thisSpell, wand);
					// TODO: Fix cooldown by passing wand into casts
            		return;
            	}

				Vector facing = caster.getFacingVector(hitbox);

            	damageCenter = player.getLocation().add(facing);

            	entities = WbsEntities.getNearbyLiving(damageCenter, hitbox, caster.getPlayer());
				for (LivingEntity hit : entities) {
					caster.damage(hit, damagePerTick, ConeOfCold.this);
					hit.addPotionEffect(potionEffect);
				}

				localEffect.setDirection(facing.clone());
				localEffect.buildAndPlay(particle, player.getEyeLocation().add(facing.normalize()));
			}
            
			@Override
			protected void finish() {
				caster.sendActionBar("Spell interrupted!");
			}
        };
        
		caster.setCasting(this, runnable);
		
		runnable.runTaskTimer(plugin, 0L, 1L);
		return true;
	}
	
	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDamage: &7" + damage;
		
		return asString;
	}
}
