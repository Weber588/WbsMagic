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
	protected boolean step(int step, int stepsThisTick) {
		boolean cancel = super.step(step, stepsThisTick);

		if (getAge() * getStepsPerTick() > 5) {
			effects.play(location);
		}

		return cancel;
	}
}
