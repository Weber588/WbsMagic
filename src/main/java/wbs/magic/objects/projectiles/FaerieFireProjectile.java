package wbs.magic.objects.projectiles;

import org.bukkit.Location;
import wbs.magic.objects.MagicFireObject;
import wbs.magic.objects.generics.ProjectileObject;
import wbs.magic.spells.ranged.projectile.ProjectileSpell;
import wbs.magic.SpellCaster;

public class FaerieFireProjectile extends ProjectileObject {

	public FaerieFireProjectile(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
		super(location, caster, castingSpell);
	}

	private double damage = 2;
	private double duration = 2;
	
	
	@Override
	public boolean tick() {
		effects.play(location);
		return false;
	}
	
	@Override
	public boolean hitBlock() {
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
