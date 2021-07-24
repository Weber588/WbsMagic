package wbs.magic.spells.ranged;

import org.bukkit.Location;
import org.bukkit.Particle;

import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.FailableSpell;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellSettings;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.objects.TornadoObject;
import wbs.magic.SpellCaster;

import wbs.utils.util.particles.LineParticleEffect;

@Spell(name = "Tornado",
		cost = 25,
		cooldown = 30,
		description = "Summon a tornado at the block you're looking at that throws nearby creatures into the air when they approach."
)
@FailableSpell("The spell will fail if the caster is not looking at a block, or the block is too far away.")
@SpellSettings(canBeConcentration = true)
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 15)
@SpellOption(optionName = "radius", type = SpellOptionType.DOUBLE, defaultDouble = 1)
@SpellOption(optionName = "amount", type = SpellOptionType.INT, defaultInt = 5)
@SpellOption(optionName = "force", type = SpellOptionType.DOUBLE, defaultDouble = 0.8)
// Overrides
@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 30)
public class Tornado extends RangedSpell {
	public Tornado(SpellConfig config, String directory) {
		super(config, directory);

		duration = config.getDouble("duration", duration);
		radius = config.getDouble("radius", radius);
		amount = config.getInt("amount", amount);
		force = config.getDouble("force", force);
		
		line.setAmount(60);
		line.setRadius(0.1);
	}

	private double duration = 15;
	private double radius = 1;
	private int amount = 5;
	private double force = 0.8;

	LineParticleEffect line = new LineParticleEffect();
	
	@Override
	public boolean cast(SpellCaster caster) {

		Location targetPos = caster.getTargetPos(range);
		if (targetPos == null) {
			caster.sendActionBar("You need line of sight to a block!");
			return false;
		}
		
		if (isConcentration) {
			caster.setConcentration(this);
		}
		
		line.play(Particle.CLOUD, caster.getPlayer().getEyeLocation(), targetPos);
		
		TornadoObject tornado = new TornadoObject(targetPos, caster, this, duration, radius, amount, force);
		tornado.start();
		
		return true;
	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rDuration: &7" + duration + " seconds";
		asString += "\n&rRadius: &7" + radius;
		asString += "\n&rForce: &7" + force;
		
		return asString;
	}
}
