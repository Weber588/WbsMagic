package wbs.magic.objects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import wbs.magic.objects.generics.MagicObject;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;
import wbs.utils.util.WbsEntities;

import java.util.Collection;

public class MagicFireObject extends MagicObject {

	public MagicFireObject(Location location, SpellCaster caster, SpellInstance castingSpell) {
		super(location, caster, castingSpell);
	}
	
	private double damage = 1;
	private double duration = 100; // in ticks

	int age = 0;
	
	@Override
	public boolean tick() {
		Location loc = spawnLocation;
		
		Collection<LivingEntity> nearby;
		nearby = WbsEntities.getNearbyLiving(spawnLocation, 1, caster.getPlayer());
		
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
	
	public void setDuration(double duration) {
		this.duration = duration;
	}
}
