package wbs.magic.spells.ranged.projectile;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import wbs.magic.objects.AlignmentType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.objects.projectiles.FrostShardProjectile;
import wbs.magic.spellmanagement.configuration.DamageSpell;
import wbs.magic.spellmanagement.configuration.RestrictWandControls;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.SpellCaster;

import wbs.magic.spellmanagement.configuration.options.EnumOptions;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.WbsMath;
import wbs.utils.util.WbsRunnable;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

@Spell(name = "Frost Shards",
		cost = 25,
		cooldown = 15,
		description = "Fire a large number of ice shards in the direction you're facing. Hold shift to reduce the spray radius and increase damage.")
@DamageSpell(deathFormat = "%victim% was torn to pieces by %attacker%'s frost shards!",
		defaultDamage = 7
)
@RestrictWandControls(dontRestrictLineOfSight = true)
@SpellOption(optionName = "amount", type = SpellOptionType.INT, defaultInt = 5)
@SpellOption(optionName = "accuracy", type = SpellOptionType.DOUBLE, defaultDouble = 0.1, aliases = {"max-accuracy"})
@SpellOption(optionName = "min-accuracy", type = SpellOptionType.DOUBLE, defaultDouble = 1)
@SpellOption(optionName = "size", type = SpellOptionType.DOUBLE, defaultDouble = 0.075)
// Override parent class defaults for these
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 50)
@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 40)
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.BAD, enumType = AlignmentType.class)
public class FrostShards extends ProjectileSpell {
	public FrostShards(SpellConfig config, String directory) {
		super(config, directory);

		amount = config.getInt("amount");
		maxAccuracy = config.getDouble("accuracy");
		minAccuracy = config.getDouble("min-accuracy");
		double size = config.getDouble("size");
		damage = config.getDouble("damage");

		NormalParticleEffect effect = new NormalParticleEffect();
		int particleAmount = (int) (size * 25);
		effect.setAmount(particleAmount);
		effect.setXYZ(size);
		DustOptions data = new DustOptions(Color.fromRGB(100, 130, 150), 0.8F);
		effect.setOptions(data);

		NormalParticleEffect coreEffect = new NormalParticleEffect();
		coreEffect.setAmount(1);
		coreEffect.setXYZ(size*5);
		BlockData snowData = Bukkit.createBlockData(Material.SNOW);
		coreEffect.setOptions(snowData);

		NormalParticleEffect coreEffect2 = new NormalParticleEffect();
		coreEffect2.setAmount(1);
		coreEffect2.setXYZ(size);
		BlockData iceData = Bukkit.createBlockData(Material.ICE);
		coreEffect2.setOptions(iceData);

		NormalParticleEffect endEffect = new NormalParticleEffect();
		endEffect.setAmount(20);
		endEffect.setXYZ(size*3);
		endEffect.setSpeed(5);
		endEffect.setOptions(iceData);
		
		ringEffect.setAmount(8);

		Particle iceParticle = Particle.BLOCK_DUST;
		Particle snowParticle = Particle.FALLING_DUST;
		Particle particle = Particle.REDSTONE;
		effects.addEffect(effect, particle)
				.addEffect(coreEffect, snowParticle, 10)
				.addEffect(coreEffect2, iceParticle, 90);
		
		endGroup.addEffect(endEffect, iceParticle);
	}
	
	private final int amount;
	private final double maxAccuracy;
	private final double minAccuracy;

	@Override
	public boolean cast(CastingContext context) {
		SpellCaster caster = context.caster;
		beginCharging(caster);
		return true;
	}
	
	private final RingParticleEffect ringEffect = new RingParticleEffect();

	private final Particle ringParticle = Particle.REDSTONE;

	private void beginCharging(SpellCaster caster) {
		
		WbsRunnable runnable = new WbsRunnable() {
			private double spread = minAccuracy;
			@Override
			public void run() {
				
				ringEffect.setAbout(caster.getFacingVector());
				ringEffect.setRadius(spread);
				ringEffect.setRotation(spread*3);
				DustOptions ringData = new DustOptions(Color.fromRGB(115, 140, 220), (float) Math.max(spread/2, 0.2));
				ringEffect.setOptions(ringData);
				
				ringEffect.buildAndPlay(ringParticle, caster.getEyeLocation().add(caster.getFacingVector()));
				
				if (!caster.isCasting()) { // Charge interrupted externally
					cancel();
				} else if (caster.isSneaking()) {
					spread -= 0.03;
					if (spread <= maxAccuracy) {
						caster.sendActionBar("Maximum accuracy!");
						spread = maxAccuracy;
					}
				} else {
					fire(caster, spread);
					cancel();
				}
			}
			
			@Override
			protected void finish() {
				caster.sendActionBar("Spell interrupted!");
			}
		};

		caster.setCasting(this, runnable);
		
		runnable.runTaskTimer(plugin, 0L, 1L);
	}

	private final double damage;

	private final WbsParticleGroup effects = new WbsParticleGroup();
	private final WbsParticleGroup endGroup = new WbsParticleGroup();
	
	private void fire(SpellCaster caster, double spread) {
		caster.stopCasting();
		Vector centralVec = caster.getFacingVector();
		Location spawnLoc = caster.getEyeLocation();
		FrostShards castingSpell = this;
		new BukkitRunnable() {
			int fired = 0;
			
			@Override
			public void run() {
				FrostShardProjectile projectile = new FrostShardProjectile(spawnLoc, caster, castingSpell);
				
				projectile.setDamage(damage);
				
				projectile.setHitSound(getCastSound());
				
				projectile.setParticle(effects);
				projectile.setEndEffects(endGroup);
				
				Vector fireDirection = centralVec.clone();
				Vector offset = WbsMath.randomVector(spread);
				Vector perpRandom = WbsMath.scaleVector(offset, offset.dot(fireDirection)/fireDirection.lengthSquared());
				fireDirection.add(perpRandom);
				
				projectile.setDirection(fireDirection);
				projectile.run();
				if (fired >= amount) {
					cancel();
				}
				fired++;
			}
		}.runTaskTimer(plugin, 0L, 3L);
	}
	
	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDamage: &7" + damage;
		asString += "\n&rAmount: &7" + amount;
		asString += "\n&rMin accuracy: &7" + minAccuracy;
		asString += "\n&rMax accuracy: &7" + maxAccuracy;

		return asString;
	}
}
