package wbs.magic.spells.ranged.targeted;

import java.util.Collection;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import wbs.magic.objects.AlignmentType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellSettings;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.SpellCaster;

import wbs.magic.spellmanagement.configuration.options.EnumOptions;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.wand.MagicWand;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.WbsRunnable;
import wbs.utils.util.entities.WbsEntityUtil;

@Spell(name = "Regenerate",
		cost = 10,
		cooldown = 5,
		description = "The caster will regenerate health until it times out, or concentration is broken."
)
@SpellSettings(isContinuousCast = true)
@SpellOption(optionName = "amount", type = SpellOptionType.DOUBLE, defaultDouble = 0.5, aliases = {"health", "heal"})
@EnumOptions.EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.GOOD, enumType = AlignmentType.class)
public class Regenerate extends TargetedSpell {
	public Regenerate(SpellConfig config, String directory) {
		super(config, directory);

		healAmount = config.getDouble("amount", healAmount);
	}
	
	private double healAmount;
	private final Particle healParticle = Particle.HEART;
	
	@Override
	public boolean preCastEntity(CastingContext context, Collection<LivingEntity> targets) {
		SpellCaster caster = context.caster;
		Player player = caster.getPlayer();
		
		Location loc = player.getLocation();
		World world = player.getWorld();

		WbsRunnable runnable = new WbsRunnable() {
			int spent = 0;
			@Override
            public void run() {
				
				if (!caster.spendMana(cost) || !player.isSneaking()) {
					cancel();
				} else if (!caster.isCasting(Regenerate.this)) {
					cancel();
				} else {
					caster.showManaLoss(spent);
				}
				
				world.spawnParticle(healParticle, WbsEntityUtil.getMiddleLocation(player), 2, 0.5, 1, 0.5, 0);
				boolean healed = false;
				for (LivingEntity target : targets) {
					double health = target.getHealth();
					double maxHealth = target.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
					
					if (loc.distance(target.getLocation()) <= targeter.getRange() && health < maxHealth) {
						world.spawnParticle(healParticle, WbsEntityUtil.getMiddleLocation(target), 2, 0.5, 1, 0.5, 0);

						health += healAmount;
						
						if (health < maxHealth) {
							healed = true;
							target.setHealth(health);
						} else {
							target.setHealth(maxHealth);
							// Don't set healed; this target is fully healed
						}
					}
				}
				
				if (!healed) {
					caster.sendActionBar("No more targets to heal.");
					cancel();
				}
				
				if (isCancelled()) {
					MagicWand wand = context.getWand();
					if (wand != null) {
						caster.setCooldownNow(Regenerate.this, wand);
					}
					caster.stopCasting();
				}
				
				spent+=cost;
            }
			
        };

		caster.setCasting(this, runnable);
		
		runnable.runTaskTimer(plugin, 5L, 5L);
        
        return true;
	}

	@Override
	public void castOn(CastingContext context, LivingEntity target) {
		
	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rHeal amount: &7" + healAmount;

		return asString;
	}
}
