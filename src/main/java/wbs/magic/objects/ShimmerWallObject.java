package wbs.magic.objects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.magic.SpellCaster;
import wbs.magic.events.objects.MagicObjectMoveEvent;
import wbs.magic.objects.colliders.Collision;
import wbs.magic.objects.colliders.QuadCollider;
import wbs.magic.objects.generics.MagicObject;
import wbs.magic.spells.SpellInstance;
import wbs.utils.util.particles.CuboidParticleEffect;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.RingParticleEffect;

public class ShimmerWallObject extends MagicObject {
    public ShimmerWallObject(Location location, SpellCaster caster, SpellInstance castingSpell) {
        super(location, caster, castingSpell);

        effect.setXYZ(0);
        effect.setScaleAmount(true);
        effect.setAmount(1);
    }

    private Vector facing;
    private PersistenceLevel level = PersistenceLevel.NORMAL;
    private int hits = Integer.MAX_VALUE;

    private final CuboidParticleEffect effect = new CuboidParticleEffect();

    private double width, height;

    private Location wallCenter = getLocation();

    @Override
    protected boolean tick() {

        if (getAge() % 2 == 0) {
            effect.setX(Math.random() * width);
            effect.setY(Math.random() * height);
            if (getAge() % 3 == 0) {
                effect.buildAndPlay(Particle.FIREWORKS_SPARK, wallCenter);
            } else {
                effect.buildAndPlay(Particle.SPELL_INSTANT, wallCenter);
            }
        } else {
            effect.setX(width);
            effect.setY(height);
            effect.buildAndPlay(Particle.SPELL_INSTANT, wallCenter);
        }


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

        Location point1 = getLocation().add(sideways);
        Location point2 = getLocation().subtract(sideways);
        Location point3 = point2.clone().add(0, height, 0);

        collider = new ShimmerWallCollider(this, point1, point2, point3);

        double angleAroundY = Math.atan2(facing.getX(), facing.getZ());

        effect.setRotation(Math.toDegrees(angleAroundY));

        wallCenter = getLocation().add(0, height / 2, 0);
    }

    private class ShimmerWallCollider extends QuadCollider {

        public ShimmerWallCollider(MagicObject parent, @NotNull Location point1, @NotNull Location point2, @NotNull Location point3) {
            super(parent, point1, point2, point3);
            hitEffect.setAbout(normal);
            hitEffect.setAmount(25);
            hitEffect.setRelative(true);
            hitEffect.setSpeed(2);
            hitEffect.setRadius(0.05);

            collideOnLeave = true;
        }

        private final RingParticleEffect hitEffect = new RingParticleEffect();

        @Override
        protected void onCollide(MagicObjectMoveEvent moveEvent, Collision collision) {
            if (moveEvent.getMagicObject().dispel(level)) {
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
