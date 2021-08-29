package wbs.magic.objects.generics;

import org.bukkit.Location;
import wbs.magic.SpellCaster;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spells.ranged.projectile.ProjectileSpell;

public class DynamicProjectileObject extends DynamicMagicObject {
    public DynamicProjectileObject(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
        super(location, caster, castingSpell);

        setEntityPredicate(caster.getPredicate());

        castingSpell.configure(this);
    }
}
