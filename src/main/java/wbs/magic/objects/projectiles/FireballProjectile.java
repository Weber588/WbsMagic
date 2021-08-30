package wbs.magic.objects.projectiles;

import org.bukkit.Location;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import wbs.magic.objects.generics.DynamicProjectileObject;
import wbs.magic.spells.ranged.projectile.ProjectileSpell;
import wbs.magic.SpellCaster;

import wbs.utils.util.WbsEntities;

public class FireballProjectile extends DynamicProjectileObject {

	public FireballProjectile(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
		super(location, caster, castingSpell);
	}

	private double damage = 0;
	private double radius = 5;

	@Override
	protected boolean step(int step, int stepsThisTick) {
		boolean cancel = super.step(step, stepsThisTick);
		effects.play(location);
		return cancel;
	}

	@Override
	protected boolean hitEntity(Location hitLocation, LivingEntity hitEntity) {
		detonate();
		return true;
	}

	@Override
	protected boolean hitBlock(Location hitLocation, Block hitBlock, BlockFace hitFace) {
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
