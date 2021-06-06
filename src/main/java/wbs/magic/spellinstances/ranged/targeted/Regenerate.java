package wbs.magic.spellinstances.ranged.targeted;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.annotations.SpellSettings;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.LineOfSightTargeter;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.WbsEntities;
import wbs.utils.util.WbsRunnable;

@Spell(name = "Regenerate",
		cost = 10,
		cooldown = 5,
		description = "The caster will regenerate health until it times out, or concentration is broken."
)
@SpellSettings(isContinuousCast = true)
@SpellOption(optionName = "amount", type = SpellOptionType.DOUBLE, defaultDouble = 0.5, aliases = {"health", "heal"})
public class Regenerate extends TargetedSpell {

	private static final GenericTargeter DEFAULT_TARGETER = new LineOfSightTargeter();
	
	public Regenerate(SpellConfig config, String directory) {
		super(config, directory, DEFAULT_TARGETER);

		healAmount = config.getDouble("amount", healAmount);
		healAmount = config.getDouble("health", healAmount);
	}
	
	private double healAmount = 0.5;
	private final Particle healParticle = Particle.HEART;
	
	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rHealth: &7" + healAmount;
		
		return asString;
	}
	
	@Override
	protected <T extends LivingEntity> boolean preCast(SpellCaster caster, Set<T> targets) {
		Player player = caster.getPlayer();
		
		Location loc = player.getLocation();
		World world = loc.getWorld();
		
		Regenerate thisSpell = this;
		
		WbsRunnable runnable = new WbsRunnable() {
			int spent = 0;
			@Override
            public void run() {
				
				if (!caster.spendMana(cost) || !player.isSneaking()) {
					cancel();
				} else if (!caster.isCasting(Regenerate.this)) {
					cancel();
				} else {
					caster.showManaChange(spent);
				}
				
				world.spawnParticle(healParticle, WbsEntities.getMiddleLocation(player), 2, 0.5, 1, 0.5, 0);
				boolean healed = false;
				for (LivingEntity target : targets) {
					double health = target.getHealth();
					double maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
					
					if (loc.distance(target.getLocation()) <= range && health < maxHealth) {
						world.spawnParticle(healParticle, WbsEntities.getMiddleLocation(target), 2, 0.5, 1, 0.5, 0);

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
					//	caster.setCooldownNow(thisSpell, wand);
					// TODO: Fix cooldown by passing wand into casts
					caster.stopCasting();
				}
				
				spent+=cost;
            }
			
			@Override
			protected void finish() {
				caster.sendActionBar("Spell interrupted!");
			}
			
        };

		caster.setCasting(this, runnable);
		
		runnable.runTaskTimer(plugin, 5L, 5L);
        
        return true;
	}

	@Override
	protected <T extends LivingEntity> void castOn(SpellCaster caster, T target) {
		
	}
}
