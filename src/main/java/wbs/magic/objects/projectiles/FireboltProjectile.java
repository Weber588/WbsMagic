package wbs.magic.objects.projectiles;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import wbs.magic.objects.generics.ProjectileObject;
import wbs.magic.spellinstances.ranged.projectile.ProjectileSpell;
import wbs.magic.wrappers.SpellCaster;

public class FireboltProjectile extends ProjectileObject {

	public FireboltProjectile(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
		super(location, caster, castingSpell);
	}

	private double damage = 7;
	private double size = 0.1;
	
	private final Particle finalParticle = Particle.LAVA;
	private final Particle steamParticle = Particle.CLOUD;
	
	@Override
	public boolean tick() {
		boolean cancel = false;
		
		if (step > (1/stepSize)) {
			effects.play(location);
		}
		
		if (location.getBlock().getType() == Material.WATER) {
			cancel = true;
		}
		
		if (hitLocation != null) {
			cancel = true;
			
			if (hitEntity != null) {
				double initialHealth = hitEntity.getHealth();
				caster.damage(hitEntity, damage, castingSpell);
				if (hitEntity.getHealth() < initialHealth) {
					hitEntity.setFireTicks((int) damage*60);
				}
			}
		}
				
		if (cancel) {
			if (location.getBlock().getType() == Material.WATER) {
				world.spawnParticle(steamParticle, location, 25, size*3, size*4, size*3, 0.1);
			} else {
				world.spawnParticle(finalParticle, hitLocation, 25, size*3, size*4, size*3, 0.1);
			}
		}
		
		return cancel;
	}

	public void setSize(double size) {
		this.size = size;
	}
	
	public void setDamage(double damage) {
		this.damage = damage;
	}
}
