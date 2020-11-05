package wbs.magic.spellinstances;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.*;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.enums.SpellType;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.WbsRunnable;
import wbs.utils.util.particles.RingParticleEffect;

@Spell(name = "Regenerate Mana",
		cost = 0,
		cooldown = 0,
		description = "The caster regenerates mana and moves slowly for the duration of the spell."
)
@FailableSpell("If the caster's mana is full, the spell will stop immediately.")
@SpellSettings(isContinuousCast = true)
@RestrictWandControls(requireShift = true)
@SpellOption(optionName = "amount", type = SpellOptionType.DOUBLE, defaultDouble = 15)
public class RegenerateMana extends SpellInstance {

	public RegenerateMana(SpellConfig config, String directory) {
		super(config, directory);

		amount = config.getInt("amount", amount);
		
		particleEffect.setRadius(1)
					.setAmount(3);
	}
	
	private int amount = 15;
	
	private final RingParticleEffect particleEffect = new RingParticleEffect();
	private final PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 6, 2, false, false);

	@Override
	public boolean cast(SpellCaster caster) {
		RegenerateMana thisSpell = this;
		
		Player player = caster.getPlayer();
		final double height = player.getHeight();
		WbsRunnable runnable = new WbsRunnable() {
			int gained = 0;
			boolean cancel = false;
			Location center = player.getLocation();
			double steps = 0;
			@Override
            public void run() {
				particleEffect.setRotation(steps);
				particleEffect.play(Particle.END_ROD, center);
				
				steps++;
				center = player.getLocation();
				double currentHeight = (Math.sin(steps/10)/2+0.5)*height;
				center.add(0, currentHeight, 0);
				
				if (steps % 5 == 0) {
			
					player.addPotionEffect(effect, true);
					
					if (!player.isSneaking()) {
						cancel = true;
						
					} else if (!caster.isCasting(getType())) {
						caster.sendActionBar("Spell interrupted!");
						cancel = true;
						
					} else if (caster.getMana() >= caster.getMaxMana()) {
						caster.sendActionBar("Mana full!");
						cancel = true;
						
					} else {
						caster.showManaChange(-gained);
						gained+=amount;
						cancel = (caster.addMana(amount) == 0);
					}
				}
					
				if (cancel) {
					caster.stopCasting();
					//	caster.setCooldownNow(thisSpell, wand);
					// TODO: Fix cooldown by passing wand into casts
					cancel();
				}
            }

			@Override
			protected void finish() {
				caster.sendActionBar("Spell interrupted!");
			}
        };
        
		caster.setCasting(getType(), runnable);
		
        runnable.runTaskTimer(plugin, 0L, 1L);
        
		return false;
	}

	@Override
	public SpellType getType() {
		return SpellType.REGENERATE_MANA;
	}
	
	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rAmount: &7" + amount;
		
		return asString;
	}
}
