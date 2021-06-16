package wbs.magic.objects.generics;

import java.util.Set;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.magic.exceptions.MagicObjectExistsException;
import wbs.magic.spellinstances.SpellInstance;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.WbsEntities;

public abstract class MineObject extends MagicObject {

	public MineObject(Location location, SpellCaster caster, SpellInstance castingSpell) {
		super(location, caster, castingSpell);
	}
	
	protected double radius = 1;

	@Override
	public void run() {
		if (timerID != -1) {
			throw new MagicObjectExistsException();
		}
		
		timerID = new BukkitRunnable() {
			boolean cancel = false;
			@Override
	        public void run() {
				Set<LivingEntity> entities = WbsEntities.getNearbyLiving(spawnLocation, radius, caster.getPlayer());

				cancel = tick();
				
				if (!entities.isEmpty()) {
					detonate();
					remove();
				}
				
				if (cancel || !active) {
					remove();
				}
	        }
	    }.runTaskTimer(plugin, 0L, 1L).getTaskId();
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	protected abstract void detonate();

}
