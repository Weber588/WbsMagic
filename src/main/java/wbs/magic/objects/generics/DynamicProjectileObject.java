package wbs.magic.objects.generics;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import wbs.magic.SpellCaster;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spells.ranged.projectile.ProjectileSpell;
import wbs.utils.util.WbsSoundGroup;

public class DynamicProjectileObject extends DynamicMagicObject {

    protected double range = 100;
    @NotNull
    protected WbsSoundGroup hitSound = new WbsSoundGroup();

    public DynamicProjectileObject(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
        super(location, caster, castingSpell);

        setEntityPredicate(caster.getPredicate());

        castingSpell.configure(this);
    }

    public @NotNull WbsSoundGroup getHitSound() {
        return hitSound;
    }

    public void setHitSound(@NotNull WbsSoundGroup hitSound) {
        this.hitSound = hitSound;
    }

    @Override
    protected boolean step(int step, int stepsThisTick) {
        boolean cancel = super.step(step, stepsThisTick);

        setStepsPerTick(getVelocity().length() * 5);

        if (getLocation().distanceSquared(getSpawnLocation()) > range * range) {
            cancel = true;
            maxDistanceReached();
        }

        return cancel;
    }

    @Override
    protected boolean hitBlock(Location hitLocation, Block hitBlock, BlockFace hitFace) {
        return true;
    }

    public double getRange() {
        return range;
    }

    protected void maxDistanceReached() {

    }

    public DynamicProjectileObject setRange(double range) {
        this.range = range;
        return this;
    }
}
