package wbs.magic.objects;

import org.bukkit.Material;
import org.bukkit.block.Block;
import wbs.magic.objects.generics.MagicObject;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;

public class MagicSpawnedBlock extends MagicObject {
    public MagicSpawnedBlock(Block block, SpellCaster caster, SpellInstance castingSpell) {
        super(block.getLocation(), caster, castingSpell);

        this.block = block;
        material = block.getType();
    }

    private final Block block;
    private boolean expireOnMaterialChange;
    private final Material material;
    private boolean removeBlockOnExpire;
    private boolean breakNaturally;
    private int duration;

    private int age = 0;

    @Override
    protected boolean tick() {
        if (expireOnMaterialChange) {
            if (block.getType() != material) {
                return true;
            }
        }

        if (duration > 0) {
            age++;

            if (age >= duration) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onRemove() {
        if (removeBlockOnExpire) {
            if (breakNaturally) {
                block.breakNaturally();
            } else {
                block.setType(Material.AIR);
            }
        }

    }

    public boolean expireOnMaterialChange() {
        return expireOnMaterialChange;
    }

    public void setExpireOnMaterialChange(boolean expireOnMaterialChange) {
        this.expireOnMaterialChange = expireOnMaterialChange;
    }

    public boolean removeBlockOnExpire() {
        return removeBlockOnExpire;
    }

    public void setRemoveBlockOnExpire(boolean removeBlockOnExpire) {
        this.removeBlockOnExpire = removeBlockOnExpire;
    }

    public boolean breakNaturally() {
        return breakNaturally;
    }

    public void setBreakNaturally(boolean breakNaturally) {
        this.breakNaturally = breakNaturally;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
