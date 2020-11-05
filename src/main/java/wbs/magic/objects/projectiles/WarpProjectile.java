package wbs.magic.objects.projectiles;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import wbs.magic.objects.generics.ProjectileObject;
import wbs.magic.spellinstances.ranged.projectile.ProjectileSpell;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.particles.RingParticleEffect;

public class WarpProjectile extends ProjectileObject {

	public WarpProjectile(Location location, SpellCaster caster, Location targetPos, ProjectileSpell castingSpell) {
		super(location, caster, castingSpell);
		
		this.targetPos = targetPos;
		
		Player player = caster.getPlayer();
		distance = player.getLocation().distance(targetPos);
	}
	
	private final double distance;
	private final Location targetPos;
	
	private final Particle particle = Particle.END_ROD;
	private RingParticleEffect effect;

	@Override
	public boolean tick() {
		boolean cancel = false;

		double radius = (1 - Math.sin((step*stepSize)/distance*Math.PI))*1.25+0.1;

		// in degrees
		double rotationChange = 5 / (radius);
		
		effect.setRadius(radius);
		effect.setRotation(step * rotationChange);
		
		effect.buildAndPlay(particle, location);
		
		if (location.distance(targetPos) <= 1) {
			cancel = true;
		}
		
		if (cancel) {
			hitSound.play(targetPos);
			caster.getPlayer().teleport(targetPos);
		}
		return cancel;
	}
	
	public void setEffect(RingParticleEffect effect) {
		this.effect = effect.clone();
	}

}
