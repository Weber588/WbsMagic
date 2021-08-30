package wbs.magic.objects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import wbs.magic.objects.generics.MagicObject;
import wbs.magic.objects.projectiles.FrostShardProjectile;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;

import wbs.magic.spells.ranged.projectile.ProjectileSpell;
import wbs.utils.util.WbsMath;
import wbs.utils.util.particles.DiscParticleEffect;

public class BlizzardObject extends MagicObject {
	
	private static final Vector downVec = new Vector(0, -1, 0);
	
	private static final Particle particle = Particle.CLOUD;
	
	/*.*************************** END OF STATIC ***************************.*/

	private final ProjectileSpell castingSpell;
	public BlizzardObject(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
		super(location, caster, castingSpell);

		this.castingSpell = castingSpell;
	}
	
	private double shardsPerTick = 0.6;
	private double damage = 4;
	private double radius = 7;
	private double height = 10;
	private double duration = 100;

	private Location cloudCenter;

	private final DiscParticleEffect cloudsEffect = (DiscParticleEffect) new DiscParticleEffect()
												.setSpeed(0.01)
												.setDirection(downVec)
												.setAmount(25)
												.setChance(75);
	
	private int age = 0;
	// If shardsPerTick is greater than 1, track the extra section and when it's
	// greater than 1, fire an additional missile
	private double toFire = 0;
	
	@Override
	protected boolean tick() {
		if (age >= duration) {
			return true;
		}
		
		age++;

		cloudsEffect.play(particle, cloudCenter);

		toFire += shardsPerTick;

		while (toFire >= 1) {
			toFire--;

			Location shardSpawn = WbsMath.getRandomPointOn2Disc(cloudCenter, radius);

			FrostShardProjectile shard = new FrostShardProjectile(shardSpawn, caster, castingSpell);

			shard.setDamage(damage)
					.setRange(height * 2)
					.setDirection(
							downVec.clone().add(WbsMath.randomVector(0.3))
					);

			shard.run();
		}
		
	//	if ((age % (int) (1 / shardsPerTick)) == 0) { // Casting to avoid floating point error

	//	}
		
		return false;
	}

	public BlizzardObject setShardsPerTick(double shardsPerTick) {
		this.shardsPerTick = shardsPerTick;
		return this;
	}

	public BlizzardObject setDamage(double damage) {
		this.damage = damage;
		return this;
	}
	
	public BlizzardObject setRadius(double radius) {
		this.radius = radius;
		
		cloudsEffect.setRadius(radius)
						.setVariation(radius*2)
						.setAmount((int) (5*radius));
		return this;
	}
	
	public BlizzardObject setHeight(double height) {
		this.height = height;
		
		cloudCenter = spawnLocation.clone().add(0, height, 0);
		return this;
	}
	
	public BlizzardObject setDuration(double duration) {
		this.duration = duration;
		return this;
	}
}
