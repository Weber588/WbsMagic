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
import wbs.magic.objects.colliders.AntiMagicShellCollider;
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

        collider = new AntiMagicShellCollider(this, 5);
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

    public void setHits(int hits) {
        this.hits = hits;
    }

    public int getHits() {
        return hits;
    }

    public void setReflect(boolean reflect) {
        this.reflect = reflect;
        collider.setBouncy(reflect);
    }

    public void setRadius(double radius) {
        this.radius = radius;

        effect.setRadius(radius);
        effect.setAmount((int) (4 * Math.PI * radius * radius));

        ((AntiMagicShellCollider) collider).setRadius(radius);
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
