package wbs.magic.spellinstances.ranged.targeted;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.persistence.PersistentDataType;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.spells.SpellConfig;
import wbs.magic.targeters.LineOfSightTargeter;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.WbsEntities;

import java.util.Set;

@Spell(name = "Dominate Monster",
        description = "Force a monster to target another random nearby monster. That mob can no longer target you.")
@SpellOption(optionName = "radius", type = SpellOptionType.DOUBLE, defaultDouble = 50)
@SpellOption(optionName = "ignore-creepers", type = SpellOptionType.BOOLEAN, defaultBool = true)
public class DominateMonster extends TargetedSpell {

    public static final NamespacedKey DOMINATE_KEY = new NamespacedKey(plugin, "dominate_monster");

    private static final LineOfSightTargeter TARGETER = new LineOfSightTargeter();

    public DominateMonster(SpellConfig config, String directory) {
        super(config, directory, TARGETER);

        radius = config.getDouble("radius");
        ignoreCreepers = config.getBoolean("ignore-creepers");

        targetClass = Monster.class;
    }

    private double radius;
    private boolean ignoreCreepers;

    @Override
    protected <T extends LivingEntity> void castOn(SpellCaster caster, T target) {
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
}
