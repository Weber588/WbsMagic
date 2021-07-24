package wbs.magic.objects.projectiles;

import org.bukkit.Location;

import wbs.magic.objects.generics.DamagingProjectileObject;
import wbs.magic.spells.ranged.projectile.ProjectileSpell;
import wbs.magic.SpellCaster;

public class EldritchBlastProjectile extends DamagingProjectileObject {
	
	public EldritchBlastProjectile(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
		super(location, caster, castingSpell);
	}
	
	@Override
	public boolean tick() {
		boolean cancel = super.tick();
		if (step > 5) {
			effects.play(location);
		}
		
		if (cancel) {
			hitSound.play(hitLocation);
		}
		return cancel;
	}
}
