package wbs.magic.spells.ranged;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.DamageSpell;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.SpellCaster;

import wbs.utils.util.WbsEntities;

@Spell(name = "Prismatic Ray",
		cost = 50,
		cooldown = 5,
		description = "A beam of energy is instantly sent out in the direct you're facing dealing damage to ALL creatures in its path."
)
@DamageSpell(deathFormat = "%victim% was zapped by %attacker%!",
		defaultDamage = 4
)
// Overrides
@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 200)
public class PrismaticRay extends RangedSpell {
	public PrismaticRay(SpellConfig config, String directory) {
		super(config, directory);
		
		damage = config.getDouble("damage");
	}

	private final double stepSize = 0.3;
	private final double damage;
	
	@Override
	public boolean cast(SpellCaster caster) {
		Location eyeLoc = caster.getEyeLocation();
		
		Location endLoc = caster.getTargetPos(range);
		if (endLoc == null) {
			endLoc = eyeLoc.clone().add(caster.getFacingVector(range));
		}

		World world = eyeLoc.getWorld();
		
		double distance = endLoc.distance(eyeLoc);
		
		Vector direction = caster.getFacingVector(stepSize);
		Location currentPos = eyeLoc.clone();

		Set<LivingEntity> alreadyHit = new HashSet<>();
		
		Particle display = Particle.SPELL_INSTANT;
		double spread = 0.2;
		
		for (int i = 0; i <= distance/stepSize; i++) {
			currentPos.add(direction);
			Set<LivingEntity> hit = WbsEntities.getNearbyLiving(currentPos, 0.2, caster.getPlayer());
			hit.removeAll(alreadyHit);
			for (LivingEntity target : hit) {
				caster.damage(target, damage, this);
			}
			alreadyHit.addAll(hit);
			world.spawnParticle(display, currentPos, 2, spread, spread, spread, 0, null, true);
		}
		
		return true;
	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDamage: &7" + damage;
		
		return asString;
	}
}
