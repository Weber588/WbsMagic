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
public class BlizzardSpell extends ProjectileSpell {
	private final static double DEFAULT_SPEED = 50;
	private final static double DEFAULT_RANGE = 40;
	public BlizzardSpell(SpellConfig config, String directory) {
		super(config, directory, DEFAULT_RANGE, DEFAULT_SPEED);
		
		shardsPerTick = config.getDouble("shards-per-second") / 20;
		damage = config.getDouble("damage");
		radius = config.getDouble("radius");
		height = config.getDouble("height");
		duration = config.getDouble("duration") * 20;
	}
	
	private double shardsPerTick = 1; // 20 per second
	private double damage = 4;
	private double radius = 7;
	private double height = 10; // The height above the target pos the blizzard should form
	private double duration = 140; // in ticks
	
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
