package wbs.magic.spells.ranged.targeted;

import java.util.*;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import wbs.magic.DamageType;
import wbs.magic.objects.AlignmentType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.configuration.DamageSpell;
import wbs.magic.spellmanagement.configuration.FailableSpell;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.SpellCaster;

import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;
import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.LineParticleEffect;

@Spell(name = "Chain Lightning",
		cost = 75,
		cooldown = 20,
		description = "A highly volatile spell, the caster sends an arc of electricity to the target creature which spreads to nearby creatures, damaging each as it goes. This spell is much more powerful if the targets are wet. Highly effective against groups, but quite weak against individuals."
)
@DamageSpell(deathFormat = "%victim% was electrocuted by %attacker%!",
		defaultDamage = 2,
		suicidePossible = true,
		suicideFormat = "%player% electrocuted themself!",
		damageTypes = {DamageType.Name.ELECTRIC}
)
@FailableSpell("If the caster is in the rain, there is a 25% chance the spell will fail, and the caster will take damage. If the caster is under water, the chance increases to 75%.")
@SpellOption(optionName = "radius", type = SpellOptionType.DOUBLE, defaultDouble = 5)
@SpellOption(optionName = "max-jumps", type = SpellOptionType.INT, defaultDouble = 3)
@SpellOption(optionName = "max-forks", type = SpellOptionType.INT, defaultDouble = 3)
@SpellOption(optionName = "can-damage-self", type = SpellOptionType.BOOLEAN, defaultBool = true)
// Overrides
@TargeterOption(optionName = "targeter", defaultRange = 50)
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.EVIL, enumType = AlignmentType.class)
public class ChainLightning extends TargetedSpell {
	
	private final static Random RANDOM = new Random();

	public ChainLightning(SpellConfig config, String directory) {
		super(config, directory);

		radius = config.getDouble("radius", radius);
		damage = config.getDouble("damage", damage);
		maxJumps = config.getInt("max-jumps", maxJumps);
		maxForks = config.getInt("max-forks", maxForks);
		canDamageSelf = config.getBoolean("can-damage-self", canDamageSelf);

		DustOptions options1 = new DustOptions(Color.fromRGB(0, 200, 255), 0.7F);
		DustOptions options2 = new DustOptions(Color.fromRGB(160, 120, 255), 0.7F);
		effect1.setOptions(options1).setAmount(50);
		effect2.setOptions(options2).setAmount(50);
	}
	
	// The maximum distance a new chain can jump
	private double radius = 5;
	private double damage = 2;

	private boolean canDamageSelf = true;
	
	// The maximum amount of jumps that the chain can go
	private int maxJumps = 3;
	// The maximum amount of targets each target can spread a new fork to.
	private int maxForks = 3;

	private final LineParticleEffect effect1 = new LineParticleEffect();
	private final LineParticleEffect effect2 = new LineParticleEffect();
	
	private final Particle particle = Particle.DUST;
	
	@Override
	public boolean preCastEntity(CastingContext context, Collection<LivingEntity> targets) {
		SpellCaster caster = context.caster;

		Player player = caster.getPlayer();
		if (WbsEntityUtil.isInMaterial(player, Material.WATER)) {
			if (chance(80)) {
				caster.sendActionBar("The spell backfired!");
				caster.damage(player, damage, thisSpell);
				return true;
			}
		}
		if (player.getWorld().hasStorm()) {
			if (WbsEntityUtil.canSeeSky(player)) {
				if (chance(25)) {
					caster.sendActionBar("The spell backfired!");
					caster.damage(player, damage, thisSpell);
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void castOn(CastingContext context, LivingEntity target) {
		SpellCaster caster = context.caster;
		jumpTo(caster, target, 0);
		caster.damage(target, damage, thisSpell);
		
		effect1.play(particle, caster.getEyeLocation(), target.getEyeLocation());
	}
	
	private boolean jumpTo(SpellCaster caster, LivingEntity target, int jumpsSoFar) {
		if (jumpsSoFar >= maxJumps) {
			return false;
		}
		
		jumpsSoFar++;

		final int jumps = jumpsSoFar;
		List<LivingEntity> toSpread = new LinkedList<>();
		// The max amount of ticks to wait between jumps (chosen from 1-delay)
		int delay = 2;
		new BukkitRunnable() {
			@Override
			public void run() {
				Set<LivingEntity> possibleTargets = WbsEntities.getNearbyLiving(target, radius, false);
				
				int forks = 0;
				for (LivingEntity newTarget : possibleTargets) {
					if (!canDamageSelf) {
						if (newTarget.equals(caster.getPlayer())) {
							continue;
						}
					}
					if (newTarget.isDead() || newTarget.getNoDamageTicks() != 0) {
						continue;
					}
					
					if (forks < maxForks) {
						toSpread.add(newTarget);
					} else {
						break;
					}
					
					forks++;
				}
				
				for (LivingEntity spreadTo : toSpread) {
					spread(caster, spreadTo, target, jumps);
				}
			}
		}.runTaskLater(plugin, RANDOM.nextInt(delay) + 1);
		return true;
	}
	
	private final ChainLightning thisSpell = this;
	
	private void dealDamage(SpellCaster caster, LivingEntity entity) {
		if (WbsEntityUtil.canSeeSky(entity) && entity.getWorld().hasStorm()) {
			caster.damage(entity, damage * 2, thisSpell);
		} else if (WbsEntityUtil.isInMaterial(entity, Material.WATER)) {
			caster.damage(entity, damage * 3, thisSpell);
		} else {
			caster.damage(entity, damage, thisSpell);
		}
		
		entity.setVelocity(WbsMath.randomVector(0.2));
	}
	
	private void spread(SpellCaster caster, LivingEntity spreadTo, LivingEntity spreadFrom, int jumpsSoFar) {
		// How long between each pulse
		int tickSpeed = 5;
		new BukkitRunnable() {
			@Override
			public void run() {
				if (chance(50)) {
					dealDamage(caster, spreadTo);
					effect1.play(particle, spreadTo.getEyeLocation(), spreadFrom.getEyeLocation());
				} else {
					dealDamage(caster, spreadFrom);
					effect2.play(particle, spreadTo.getEyeLocation(), spreadFrom.getEyeLocation());
				}
				
				if (chance(5) || spreadTo.isDead() || spreadFrom.isDead()) {
					cancel();
				} else if (spreadTo.getEyeLocation().distance(spreadFrom.getEyeLocation()) > radius) {
					cancel();
				}
				
				if (!isCancelled()) {
					if (chance(15)) {
						jumpTo(caster, spreadTo, jumpsSoFar + 1);
					}
				}
 			}
		}.runTaskTimer(plugin, 0L, tickSpeed);
		
		if (!spreadTo.isDead()) {
			jumpTo(caster, spreadTo, jumpsSoFar);
		}
	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDamage: &7" + damage;
		asString += "\n&rRadius: &7" + radius;
		asString += "\n&rMax jumps: &7" + maxJumps;
		asString += "\n&rMax forks: &7" + maxForks;
		asString += "\n&rCan damage self?: &7" + canDamageSelf;

		return asString;
	}
}
