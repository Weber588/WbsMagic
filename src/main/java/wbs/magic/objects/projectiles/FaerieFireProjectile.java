package wbs.magic.objects.projectiles;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import wbs.magic.objects.MagicFireObject;
import wbs.magic.objects.generics.DynamicProjectileObject;
import wbs.magic.spells.ranged.projectile.ProjectileSpell;
import wbs.magic.SpellCaster;

public class FaerieFireProjectile extends DynamicProjectileObject {

	public FaerieFireProjectile(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
		super(location, caster, castingSpell);
	}

	private double damage = 2;
	private double duration = 2;

	@Override
	protected boolean step(int step, int stepsThisTick) {
		boolean cancel =  super.step(step, stepsThisTick);

		effects.play(location);

		return cancel;
	}

	@Override
	protected boolean hitBlock(Location hitLocation, Block hitBlock, BlockFace hitFace) {
		super.hitBlock(hitLocation, hitBlock, hitFace);

		MagicFireObject magicFire = new MagicFireObject(location, caster, castingSpell);
		magicFire.setDamage(damage);
		magicFire.setDuration((int) (20 * duration));
		magicFire.run();

		return true;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}
	
	public void setDuration(double duration) {
		this.duration = duration;
	}
}
