package wbs.magic.objects.generics;

import org.bukkit.Location;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;

public class DamagingProjectileObject extends ProjectileObject {
    public DamagingProjectileObject(Location location, SpellCaster caster, SpellInstance castingSpell) {
        super(location, caster, castingSpell);
    }

    protected double damage;

    @Override
    protected boolean tick() {
        boolean cancel = false;

        if (hitLocation != null) {
            cancel = true;

            if (hitEntity != null) {
                caster.damage(hitEntity, damage, castingSpell);
            }
        }

        return cancel;
    }

    public DamagingProjectileObject setDamage(double damage) {
        this.damage = damage;
        return this;
    }
}
