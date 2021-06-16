package wbs.magic.spellinstances.ranged;

import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import wbs.magic.annotations.SpellSound;
import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.Spell;
import wbs.magic.objects.generics.MagicObject;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.particles.NormalParticleEffect;

@Spell(name = "Negate Magic",
		cost = 0,
		cooldown = 15,
		description = "Cancel all magic within a radius of you, including projectiles, magic objects, and players casting spells."
)
@SpellSound(sound = Sound.ENTITY_WITHER_SPAWN, pitch = 2, volume = 0.3F)
public class NegateMagic extends RangedSpell {

	protected final static double DEFAULT_RANGE = 5;
	
	public NegateMagic(SpellConfig config, String directory) {
		super(config, directory, DEFAULT_RANGE);

		effect.setX(range);
		effect.setY(range);
		effect.setZ(range);
		effect.setAmount((int) (range*range*range));
	}
	
	private NormalParticleEffect effect = new NormalParticleEffect();
	private Particle mainParticle = Particle.END_ROD;
	
	@Override
	public boolean cast(SpellCaster caster) {
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
