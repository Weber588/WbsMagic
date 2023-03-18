package wbs.magic;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;
import wbs.magic.spells.SpellInstance;

import java.util.HashSet;
import java.util.Set;

public class DamageSource {
    private final Set<DamageType> types = new HashSet<>();
    @Nullable
    private final SpellInstance spell;

    public DamageSource() {
        this(null);
    }
    public DamageSource(@Nullable SpellInstance spell) {
        this.spell = spell;
        if (spell != null) {
            types.add(DamageType.MAGIC);
        }
    }

    public void addType(DamageType type) {
        types.add(type);
    }

    public boolean is(DamageType type) {
        return types.contains(type);
    }

    public boolean is(EntityDamageEvent.DamageCause cause) {
        return types.stream().anyMatch(type -> type.matches(cause));
    }

    @Nullable
    public SpellInstance getSpell() {
        return spell;
    }

    public double calculateDamage(LivingEntity target, double baseDamage) {
        double modifier = 1;

        for (DamageType type : types) {
            modifier += type.getModifier(target);
        }

        return baseDamage * Math.max(0, modifier);
    }
}
