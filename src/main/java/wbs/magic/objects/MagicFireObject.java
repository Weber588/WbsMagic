package wbs.magic.objects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import wbs.magic.objects.generics.MagicObject;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;
import wbs.magic.targeters.RadiusTargeter;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.WbsSound;

import java.util.Collection;

public class MagicFireObject extends MagicObject {

	public MagicFireObject(Location location, SpellCaster caster, SpellInstance castingSpell) {
		super(location, caster, castingSpell);
	}
	
	private double damage = 1;
	private double duration = 100; // in ticks
	private final WbsSound sound = new WbsSound(Sound.BLOCK_FIRE_AMBIENT, 1, 2);

	private RadiusTargeter radiusTargeter = new RadiusTargeter(1);

	private int age = 0;

	@Override
	public boolean tick() {
		Location loc = spawnLocation;

		if (age % 20 == 0) {
			sound.play(getLocation());
		}
		
		Collection<LivingEntity> nearby = radiusTargeter.getTargets(caster, spawnLocation);
		
		if (!nearby.isEmpty()) {
			for (LivingEntity target : nearby) {
				caster.damage(target, damage, castingSpell);
			}
		}
		
		world.spawnParticle(Particle.SPELL_WITCH, loc, 20, 0.3, 0.3, 0.3, 0);
		world.spawnParticle(Particle.SMOKE_NORMAL, loc, 5, 0.05, 0.05, 0.05, 0);
		
		age++;
		return age > duration;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public void setRadius(double radius) {
		radiusTargeter = new RadiusTargeter(radius);
	}
	
	public void setDuration(double duration) {
		this.duration = duration;
	}
}
