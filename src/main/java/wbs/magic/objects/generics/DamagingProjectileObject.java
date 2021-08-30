package wbs.magic.objects.generics;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;
import wbs.magic.spells.ranged.projectile.ProjectileSpell;

public class DamagingProjectileObject extends DynamicProjectileObject {
    public DamagingProjectileObject(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
        super(location, caster, castingSpell);
    }

    protected double damage;

    @Override
    protected boolean hitBlock(Location hitLocation, Block hitBlock, BlockFace hitFace) {
        return true;
    }

    @Override
    protected boolean hitEntity(Location hitLocation, LivingEntity hitEntity) {
        caster.damage(hitEntity, damage, castingSpell);
        return true;
    }

    public DamagingProjectileObject setDamage(double damage) {
        this.damage = damage;
        return this;
    }
}
