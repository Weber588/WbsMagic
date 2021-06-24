package wbs.magic.spellinstances.ranged.projectile;

import org.bukkit.Location;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.DamageSpell;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.annotations.SpellSettings;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.objects.BlizzardObject;
import wbs.magic.wrappers.SpellCaster;

@Spell(name = "Blizzard",
		cost = 200,
		cooldown = 300,
		description =
				"Ice shards rain in a radius around a targeted block, " +
				"dealing damage and slowing creatures in the area."
)
@SpellSettings(canBeConcentration = true)
@DamageSpell(deathFormat = "%victim% froze to death in %attacker%'s blizzard!",
		suicidePossible = true,
		suicideFormat = "%player% froze to death in their own blizzard!",
		defaultDamage = 4
)
@SpellOption(optionName = "shards-per-second", type = SpellOptionType.DOUBLE, defaultDouble = 20)
@SpellOption(optionName = "radius", type = SpellOptionType.DOUBLE, defaultDouble = 7)
@SpellOption(optionName = "height", type = SpellOptionType.DOUBLE, defaultDouble = 10)
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 7)
// Override parent class defaults for these
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 50)
@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 40)
public class BlizzardSpell extends ProjectileSpell {
	public BlizzardSpell(SpellConfig config, String directory) {
		super(config, directory);
		
		shardsPerTick = config.getDouble("shards-per-second") / 20;
		damage = config.getDouble("damage");
		radius = config.getDouble("radius");
		height = config.getDouble("height");
		duration = config.getDouble("duration") * 20;
	}
	
	private final double shardsPerTick; // 20 per second
	private final double damage;
	private final double radius;
	private final double height; // The height above the target pos the blizzard should form
	private final double duration; // in ticks
	
	@Override
	public boolean cast(SpellCaster caster) {
		Location targetPos = caster.getTargetPos(range);
		if (targetPos == null) {
			caster.sendActionBar("You need line of sight to a block!");
			return false;
		}
		
		BlizzardObject blizzard = new BlizzardObject(targetPos, caster, this);
		
		blizzard.setShardsPerTick(shardsPerTick)
					.setDamage(damage)
					.setRadius(radius)
					.setHeight(height)
					.setDuration(duration)
					.run();
		return true;
	}
}
