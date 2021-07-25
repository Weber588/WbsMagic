package wbs.magic.objects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;
import wbs.magic.objects.generics.DynamicMagicObject;
import wbs.magic.objects.generics.MagicObject;
import wbs.magic.objects.generics.ProjectileObject;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;
import wbs.utils.util.particles.SphereParticleEffect;

import java.util.LinkedList;
import java.util.List;

public class AntiMagicShellObject extends DynamicMagicObject {
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

    private final SphereParticleEffect effect = new SphereParticleEffect();

    private List<MagicObject> insideLastTick = new LinkedList<>();

    @Override
    protected void onRun() {
        effect.build();

        // Fizzle objects already in the shell
        List<MagicObject> insideShell = MagicObject.getNearbyActive(getLocation(), radius);
        for (MagicObject object : insideShell) {
            if (object != this) object.remove(false);
        }

        if (castingSpell.isConcentration()) caster.setConcentration(castingSpell);
    }

    @Override
    protected boolean tick() {
        if (age % 25 == 0) {
            effect.play(Particle.VILLAGER_HAPPY, getLocation());
        }
        if (age % 50 == 0) {

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
            location = caster.getLocation();
        }

        List<MagicObject> insideShell = MagicObject.getNearbyActive(getLocation(), radius);

        if (reflect) {
            for (MagicObject object : insideShell) {
                if (allowCasterSpells) {
                    if (object.getCaster() == caster) {
                        continue;
                    }
                }
                if (!object.isExpired() && object instanceof ProjectileObject) {
                    ProjectileObject proj = (ProjectileObject) object;

                    Vector normal = proj.getLocation().subtract(getLocation()).toVector();
                    Vector direction = proj.getDirection();
                    double directionLength = direction.length();
                    // R = V - 2N(V dot N)
                    Vector reflected = direction.subtract(normal.clone().multiply(direction.dot(normal)).multiply(2));
                    reflected.normalize().multiply(directionLength);

                    proj.setDirection(reflected);

                    // Allow reflected projectiles to hit the sender
                    proj.setPredicate(SpellInstance.VALID_TARGETS_PREDICATE);

                    hits--;
                }
            }
        }

        for (MagicObject object : insideShell) {
            if (allowCasterSpells) {
                if (object.getCaster() == caster) {
                    continue;
                }
            }
            if (object != this) {
                if (!object.isExpired() && insideLastTick.contains(object)) { // Only fizzle if it's been inside longer than a tick
                    if (chance(30)) {
                        object.remove(false);
                        hits--;
                    }
                }
            }
        }

        insideLastTick = insideShell;
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
}
