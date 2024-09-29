package wbs.magic.objects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.magic.SpellCaster;
import wbs.magic.events.objects.MagicObjectMoveEvent;
import wbs.magic.objects.colliders.Collision;
import wbs.magic.objects.colliders.QuadCollider;
import wbs.magic.objects.generics.MagicObject;
import wbs.magic.spells.SpellInstance;
import wbs.utils.util.WbsMath;
import wbs.utils.util.particles.CuboidParticleEffect;
import wbs.utils.util.particles.LineParticleEffect;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.utils.util.providers.NumProvider;
import wbs.utils.util.providers.generator.num.PulseGenerator;

public class ShimmerWallObject extends MagicObject {
    public ShimmerWallObject(Location location, SpellCaster caster, SpellInstance castingSpell) {
        super(location, caster, castingSpell);

        effect.setXYZ(0.05);
        effect.setScaleAmount(true);
        effect.setAmount(2);
    }

    private Vector facing;
    private PersistenceLevel level = PersistenceLevel.NORMAL;
    private int hits = Integer.MAX_VALUE;

    private final LineParticleEffect borderParticle = new LineParticleEffect();
    private final CuboidParticleEffect effect = new CuboidParticleEffect();

    private double width, height;

    private Location wallCenter = getLocation();

    private Location point1, point2, point3, point4;

    @Override
    protected boolean tick() {

        if (getAge() % 3 == 0) {
            borderParticle.setAmount((int) (Math.random() * width));
            borderParticle.play(Particle.END_ROD, point1, point2);
            borderParticle.setAmount((int) (Math.random() * height));
            borderParticle.play(Particle.END_ROD, point2, point3);
            borderParticle.setAmount((int) (Math.random() * width));
            borderParticle.play(Particle.END_ROD, point3, point4);
            borderParticle.setAmount((int) (Math.random() * height));
            borderParticle.play(Particle.END_ROD, point4, point1);
        }

        effect.buildAndPlay(Particle.ENCHANT, wallCenter);

        return false;
    }

    public void setFacing(Vector facing) {
        this.facing = facing.clone().normalize();
    }

    public void setLevel(PersistenceLevel level) {
        this.level = level;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;

        Vector sideways = facing.getCrossProduct(new Vector(0, 1, 0)).multiply(width / 2);

        point1 = getLocation().add(sideways);
        point2 = getLocation().subtract(sideways);
        point3 = point2.clone().add(0, height, 0);
        point4 = point1.clone().add(0, height, 0);

        collider = new ShimmerWallCollider(this, point1, point2, point3);

        double angleAroundY = Math.toDegrees(Math.atan2(facing.getX(), facing.getZ()));
        effect.setRotation(angleAroundY);

        double period = 40 + Math.random() * 40;
        PulseGenerator generatorX = new PulseGenerator(0.3, width, period, 0);
        NumProvider pulseProviderX = new NumProvider(generatorX);
        PulseGenerator generatorY = new PulseGenerator(0.3, height, period, 0);
        NumProvider pulseProviderY = new NumProvider(generatorY);

        effect.setX(pulseProviderX);
        effect.setY(pulseProviderY);

        wallCenter = getLocation().add(0, height / 2, 0);
    }

    public void setReflect(boolean reflect) {
        collider.setBouncy(reflect);
    }

    public void setAllowCasterSpells(boolean allowCasterSpells) {
        if (allowCasterSpells) {
            collider.setPredicate(obj -> obj.getCaster() != caster);
        } else {
            collider.setPredicate(obj -> true);
        }
    }

    public void setOneWay(boolean oneWay) {
        collider.setCollideOnLeave(!oneWay);
    }

    private class ShimmerWallCollider extends QuadCollider {

        public ShimmerWallCollider(MagicObject parent, @NotNull Location point1, @NotNull Location point2, @NotNull Location point3) {
            super(parent, point1, point2, point3);
            hitEffect.setAbout(normal);
            hitEffect.setAmount(25);
            hitEffect.setRelative(true);
            hitEffect.setSpeed(2);
            hitEffect.setRadius(0.05);
        }

        private final RingParticleEffect hitEffect = new RingParticleEffect();

        @Override
        protected void onCollide(MagicObjectMoveEvent moveEvent, Collision collision) {
            if (bouncy || moveEvent.getMagicObject().dispel(level)) {
                hits--;
                hitEffect.play(Particle.END_ROD, collision.getHitLocation());

                moveEvent.setCancelled(true);

                if (hits <= 0) {
                    getParent().remove(true);
                }
            }
        }
    }
}
