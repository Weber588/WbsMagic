package wbs.magic.spells;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.DamageSpell;
import wbs.magic.spellmanagement.configuration.RestrictWandControls;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellSettings;
import wbs.magic.SpellCaster;

import wbs.magic.targeters.RadiusTargeter;
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

		damage = config.getDouble("damage");
		
		effect.setSpeed(0.1);
		effect.setVariation(2);
		effect.setAmount(15);
		effect.setRadius(0);
	}

	private final double damage; // per second
	
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
			
			final int sustain = cost / 20;

			final double hitbox = 3.5;
			final RadiusTargeter targeter = new RadiusTargeter(hitbox);

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
            		return;
            	}

				Vector facing = caster.getFacingVector(hitbox);

            	damageCenter = player.getLocation().add(facing);

            	entities = targeter.getTargets(caster, damageCenter);
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
