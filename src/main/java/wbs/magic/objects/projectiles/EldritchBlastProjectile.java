package wbs.magic.objects.projectiles;

import org.bukkit.Location;

import wbs.magic.objects.generics.ProjectileObject;
import wbs.magic.spellinstances.ranged.projectile.ProjectileSpell;
import wbs.magic.wrappers.SpellCaster;

public class EldritchBlastProjectile extends ProjectileObject {
	
	public EldritchBlastProjectile(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
		super(location, caster, castingSpell);
	}
	
	private double damage = 7;
	
	@Override
	public boolean tick() {
		boolean cancel = false;
		if (step > 5) {
			effects.play(location);
		}
		
		if (hitLocation != null) {
			cancel = true;
			
			if (hitEntity != null) {
				caster.damage(hitEntity, damage, castingSpell);
			}
		}
		
		if (cancel) {
			hitSound.play(hitLocation);
		}
		return cancel;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}
}
