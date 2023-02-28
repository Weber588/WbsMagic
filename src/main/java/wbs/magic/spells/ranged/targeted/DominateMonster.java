package wbs.magic.spells.ranged.targeted;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.persistence.PersistentDataType;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.SpellCaster;
import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.WbsEntities;

import java.util.Set;

@Spell(name = "Dominate Monster",
        description = "Force a monster to target another random nearby monster. That mob can no longer target you.")
@SpellOption(optionName = "radius", type = SpellOptionType.DOUBLE, defaultDouble = 50)
@SpellOption(optionName = "ignore-creepers", type = SpellOptionType.BOOLEAN, defaultBool = true)
public class DominateMonster extends TargetedSpell {

    public static final NamespacedKey DOMINATE_KEY = new NamespacedKey(plugin, "dominate_monster");

    public DominateMonster(SpellConfig config, String directory) {
        super(config, directory);

        radius = config.getDouble("radius");
        ignoreCreepers = config.getBoolean("ignore-creepers");

        targetClass = Monster.class;
    }

    private final double radius;
    private final boolean ignoreCreepers;

    @Override
    public void castOn(CastingContext context, LivingEntity target) {
        SpellCaster caster = context.caster;
        Monster monster = (Monster) target;

        Set<Monster> nearbyMonsters = WbsEntities.getNearby(monster, 50, false, Monster.class);

        Monster targetMonster = null;
        for (Monster potentialTarget : nearbyMonsters) {
            if (!ignoreCreepers || potentialTarget.getType() != EntityType.CREEPER) {
                targetMonster = potentialTarget;
                break;
            }
        }

        monster.setTarget(targetMonster);
        monster.getPersistentDataContainer().set(DOMINATE_KEY, PersistentDataType.STRING, caster.getName());
    }

    @Override
    public String toString() {
        String asString = super.toString();

        asString += "\n&rRadius: &7" + radius;
        asString += "\n&rIgnore creepers: &7" + radius;

        return asString;
    }
}
