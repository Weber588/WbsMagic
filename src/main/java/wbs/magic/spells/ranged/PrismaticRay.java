package wbs.magic.spells.ranged;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import wbs.magic.objects.AlignmentType;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.DamageSpell;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.SpellCaster;

import wbs.magic.spellmanagement.configuration.options.EnumOptions;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.targeters.RadiusTargeter;
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
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.BAD, enumType = AlignmentType.class)
public class PrismaticRay extends RangedSpell {
	public PrismaticRay(SpellConfig config, String directory) {
		super(config, directory);
		
		damage = config.getDouble("damage");
	}

	private final double stepSize = 0.3;
	private final double damage;

	private final RadiusTargeter radiusTargeter = new RadiusTargeter(0.2);
	
	@Override
	public boolean cast(CastingContext context) {
		SpellCaster caster = context.caster;
		Location eyeLoc = caster.getEyeLocation();
		
		Location endLoc = caster.getTargetPos(range);
		if (endLoc == null) {
			endLoc = eyeLoc.clone().add(caster.getFacingVector(range));
		}

		World world = Objects.requireNonNull(eyeLoc.getWorld());
		
		double distance = endLoc.distance(eyeLoc);
		
		Vector direction = caster.getFacingVector(stepSize);
		Location currentPos = eyeLoc.clone();

		Set<LivingEntity> alreadyHit = new HashSet<>();
		
		Particle display = Particle.INSTANT_EFFECT;
		double spread = 0.2;
		
		for (int i = 0; i <= distance/stepSize; i++) {
			currentPos.add(direction);
			Set<LivingEntity> hit = radiusTargeter.getTargets(caster, currentPos);
			hit.removeAll(alreadyHit);
			hit.remove(caster.getPlayer());
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
