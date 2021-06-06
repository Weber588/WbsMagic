package wbs.magic.objects.projectiles;

import org.bukkit.Location;

import wbs.magic.objects.generics.ProjectileObject;
import wbs.magic.spellinstances.ranged.projectile.ProjectileSpell;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.WbsEntities;

public class FireballProjectile extends ProjectileObject {

	public FireballProjectile(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
		super(location, caster, castingSpell);
	}

	private double damage = 0;
	private double radius = 5;

	@Override
	protected boolean tick() {
		effects.play(location);
		
		return false;
	}
	
	@Override
	public boolean hitEntity() {
		detonate();
		return true;
	}

	@Override
	public boolean hitBlock() {
		detonate();
		return true;
	}

	@Override
	public void maxDistanceReached() {
		detonate();
	}
	
	private void detonate() {
		WbsEntities.getNearbyLiving(location, radius, caster.getPlayer());
	}
	
	public void setDamage(double damage) {
		this.damage = damage;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}
}
