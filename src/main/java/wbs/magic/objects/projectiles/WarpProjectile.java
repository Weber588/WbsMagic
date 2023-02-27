package wbs.magic.objects.projectiles;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import wbs.magic.objects.generics.DynamicProjectileObject;
import wbs.magic.spells.ranged.projectile.ProjectileSpell;
import wbs.magic.SpellCaster;
import wbs.utils.util.particles.RingParticleEffect;

public class WarpProjectile extends DynamicProjectileObject {

	public WarpProjectile(Location location, SpellCaster caster, Location targetPos, ProjectileSpell castingSpell) {
		super(location, caster, castingSpell);
		
		this.targetPos = targetPos;
		
		Player player = caster.getPlayer();
		double distance = player.getLocation().distance(targetPos);

		stepsToTake = (int) (distance / perStep(getVelocity()).length());
	}

	private final Location targetPos;

	private final int stepsToTake;

	private final Particle particle = Particle.END_ROD;
	private RingParticleEffect effect;

	@Override
	protected boolean step(int step, int stepsThisTick) {
		boolean cancel = super.step(step, stepsThisTick);

		double radius = (1 - Math.sin(stepsTaken() * Math.PI / stepsToTake)) * 1.25 + 0.1;

		// in degrees
		double rotationChange = 5 / (radius);

		effect.setRadius(radius);
		effect.setRotation(stepsTaken() * rotationChange);

		effect.buildAndPlay(particle, location);

		if (location.distance(targetPos) <= 1) {
			// Try to teleport, but don't care about the result - we need to cancel even if the teleport fails
			caster.teleport(targetPos);
			cancel = true;
		}

		return cancel;
	}

	@Override
	protected boolean hitBlock(Location hitLocation, Block hitBlock, BlockFace hitFace) {
		caster.teleport(hitLocation);
		return true;
	}

	public void setEffect(RingParticleEffect effect) {
		this.effect = effect.clone();
	}
}
