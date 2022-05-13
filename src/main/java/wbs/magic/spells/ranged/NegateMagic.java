package wbs.magic.spells.ranged;

import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellSound;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.objects.generics.MagicObject;
import wbs.magic.SpellCaster;
import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.particles.NormalParticleEffect;

@Spell(name = "Negate Magic",
		cost = 0,
		cooldown = 15,
		description = "Cancel all magic within a radius of you, including projectiles, magic objects, and players casting spells."
)
@SpellSound(sound = Sound.ENTITY_WITHER_SPAWN, pitch = 2, volume = 0.3F)
// Overrides
@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 10)
public class NegateMagic extends RangedSpell {
	public NegateMagic(SpellConfig config, String directory) {
		super(config, directory);

		effect.setX(range / 2);
		effect.setY(range / 2);
		effect.setZ(range / 2);
		effect.setAmount((int) (range*range*range));
	}
	
	private final NormalParticleEffect effect = new NormalParticleEffect();
	private final Particle mainParticle = Particle.END_ROD;
	
	@Override
	public boolean cast(CastingContext context) {
		SpellCaster caster = context.caster;
		Player player = caster.getPlayer();
		Location location = player.getLocation();
		Collection<LivingEntity> targets = caster.getNearbyLiving(range, false);
		caster.stopConcentration();
		String negateMessage = "&b" + caster.getName() + "&r cast Negate Magic!";
		for (LivingEntity target : targets) {
			if (target instanceof Player) {
				if (SpellCaster.isRegistered((Player) target)) {
					SpellCaster otherCaster = SpellCaster.getCaster((Player) target);
					otherCaster.stopConcentration();
					otherCaster.forceStopCasting();
					otherCaster.sendActionBar(negateMessage);
				}
			}
		}
		
		for (MagicObject object : MagicObject.getNearbyActive(location, range)) {
			object.remove();
		}
		
		effect.play(mainParticle, location);
		
		for (MagicObject object : MagicObject.getNearbyActive(location, range)) {
			if (!object.isPersistent()) {
				object.remove();
			}
		}
		
		return true;
	}
}
