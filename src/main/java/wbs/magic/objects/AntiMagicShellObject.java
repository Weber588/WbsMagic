package wbs.magic.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import wbs.magic.events.objects.MagicObjectMoveEvent;
import wbs.magic.events.objects.MagicObjectSpawnEvent;
import wbs.magic.objects.generics.*;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;
import wbs.utils.util.WbsMath;
import wbs.utils.util.particles.SphereParticleEffect;

import java.util.LinkedList;
import java.util.List;

public class AntiMagicShellObject extends KinematicMagicObject implements Listener {
    public AntiMagicShellObject(Location location, SpellCaster caster, SpellInstance castingSpell) {
        super(location, caster, castingSpell);
    }

    private int duration;
    private int hits;
    private boolean reflect;
    private boolean followPlayer;
    private boolean allowCasterSpells;
    private int age = 0;
    private double radius;

    private PersistenceLevel level = PersistenceLevel.NORMAL;

    private final SphereParticleEffect effect = new SphereParticleEffect();

    @Override
    protected void onRun() {
        effect.build();

        // Fizzle objects already in the shell
        List<MagicObject> insideShell = MagicObject.getNearbyActive(getLocation(), radius);
        for (MagicObject object : insideShell) {
            if (object != this) object.remove(false);
        }

        if (castingSpell.isConcentration()) caster.setConcentration(castingSpell);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    protected void onRemove() {
        super.onRemove();

        MagicObjectMoveEvent.getHandlerList().unregister(this);
        MagicObjectSpawnEvent.getHandlerList().unregister(this);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onMagicObjectMove(MagicObjectMoveEvent event) {
        KinematicMagicObject obj = event.getMagicObject();

        if (obj == this) return;
        if (obj.isExpired()) return;
        if (allowCasterSpells && obj.caster == caster) return;

        double newDist = event.getNewLocation().distance(getLocation());
        double currentDist = obj.distance(this);


        if ((newDist < radius && currentDist > radius) ||
                (newDist > radius && currentDist < radius))
        {
            boolean entering = newDist < radius;

            if (reflect) {
                if (obj instanceof DynamicMagicObject) {
                    DynamicMagicObject dynObj = (DynamicMagicObject) obj;

                    Vector normal = dynObj.getLocation().subtract(getLocation()).toVector();

                    Location hitPos = normal.normalize()
                            // Make sure the next measurement isn't too small
                            // By doing this, we avoid the risk of normal = 0
                            // causing the projectile to freeze
                            .multiply(radius + (entering ? -1 : 1))
                            .toLocation(world)
                            .add(getLocation());

                    normal = dynObj.getLocation().subtract(hitPos).toVector();

                    // Add a tiny bit of noise to make it more fun :)
                    // TODO: Make this configurable
                    normal.add(WbsMath.randomVector(0.05));

                    if (dynObj.bounce(normal)) {
                        if (obj instanceof DynamicProjectileObject) {
                            // Allow reflected projectiles to hit the sender
                            dynObj.setEntityPredicate(SpellInstance.VALID_TARGETS_PREDICATE);
                        }

                        hits--;

                        event.setCancelled(true);

                        return;
                    }
                }
            }

            if (newDist < radius) {
                if (obj.dispel(level)) {
                    caster.sendMessage("Removed");
                    hits--;
                }
            }
        }
    }

    @Override
    protected boolean tick() {
        if (age % 25 == 0) {
            effect.play(Particle.VILLAGER_HAPPY, getLocation());
        }

        age++;

        if (age >= duration) {
            caster.sendActionBar("Your " + castingSpell.getName() + " fizzles away...");
            return true;
        }

        if (hits <= 0) {
            caster.sendActionBar("Your " + castingSpell.getName() + " ran out of energy!");
            return true;
        }

        if (castingSpell.isConcentration() && !caster.isConcentratingOn(castingSpell)) {
            caster.sendActionBar("Spell interrupted!");
            return true;
        }

        if (followPlayer) {
            move(caster.getLocation());
        }

        return false;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setHits(int maxHits) {
        this.hits = maxHits;
    }

    public void setReflect(boolean reflect) {
        this.reflect = reflect;
    }

    public void setRadius(double radius) {
        this.radius = radius;

        effect.setRadius(radius);
        effect.setAmount((int) (4 * Math.PI * radius * radius));
    }

    public void setFollowPlayer(boolean followPlayer) {
        this.followPlayer = followPlayer;
    }

    public void setAllowCasterSpells(boolean allowCasterSpells) {
        this.allowCasterSpells = allowCasterSpells;
    }

    public PersistenceLevel getLevel() {
        return level;
    }

    public void setLevel(PersistenceLevel level) {
        this.level = level;
    }
}
