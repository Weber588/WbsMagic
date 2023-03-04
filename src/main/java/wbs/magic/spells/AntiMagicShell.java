package wbs.magic.spells;

import org.bukkit.Sound;
import wbs.magic.objects.AlignmentType;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.objects.AntiMagicShellObject;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.SpellCaster;
import wbs.magic.spellmanagement.configuration.options.EnumOptions;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.WbsEntities;

@Spell(name = "Anti Magic Shell",
        cost = 25,
        cooldown = 5,
        description = "Form a shield around you that prevents any magic objects from entering or leaving"
)
@SpellSettings(canBeConcentration = true)
@SpellSound(sound = Sound.BLOCK_BEACON_DEACTIVATE, pitch = 1.5f)
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 10)
@SpellOption(optionName = "radius", type = SpellOptionType.DOUBLE, defaultDouble = 6)
@SpellOption(optionName = "reflect", type = SpellOptionType.BOOLEAN, defaultBool = true)
@SpellOption(optionName = "follow-player", type = SpellOptionType.BOOLEAN, defaultBool = false)
@SpellOption(optionName = "allow-caster-spells", type = SpellOptionType.BOOLEAN, defaultBool = true)
@SpellOption(optionName = "hits", type = SpellOptionType.INT, defaultInt = 5)
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.GOOD, enumType = AlignmentType.class)
public class AntiMagicShell extends SpellInstance {
    public AntiMagicShell(SpellConfig config, String directory) {
        super(config, directory);

        radius = config.getDouble("radius");
        duration = (int) (config.getDouble("duration") * 20);
        reflect = config.getBoolean("reflect");
        followPlayer = config.getBoolean("follow-player");
        allowCasterSpells = config.getBoolean("allow-caster-spells");
        hits = config.getInt("hits");
        if (hits <= 0) {
            hits = Integer.MAX_VALUE;
        }
    }

    private final double radius;
    private final int duration;
    private final boolean reflect;
    private final boolean followPlayer;
    private final boolean allowCasterSpells;
    private int hits;

    @Override
    public boolean cast(CastingContext context) {
        SpellCaster caster = context.caster;

        AntiMagicShellObject shellObject = new AntiMagicShellObject(WbsEntities.getMiddleLocation(caster.getPlayer()), caster, this);

        shellObject.setRadius(radius);
        shellObject.setDuration(duration);
        shellObject.setHits(hits);
        shellObject.setReflect(reflect);
        shellObject.setFollowPlayer(followPlayer);
        shellObject.setAllowCasterSpells(allowCasterSpells);

        shellObject.run();

        caster.sendActionBar("You cast &h" + getName() + "&r!");

        return true;
    }

    @Override
    public String toString() {
        String asString = super.toString();

        asString += "\n&rDuration: &7" + (duration / 20) + " seconds";
        asString += "\n&rRadius: &7" + radius;
        if (hits == Integer.MAX_VALUE) {
            asString += "\n&rMax hits: &7Infinite";
        } else {
            asString += "\n&rMax hits: &7" + hits;
        }
        asString += "\n&rReflect projectiles? &7" + reflect;
        asString += "\n&rFollow player? &7" + followPlayer;
        asString += "\n&rAllow caster spells? &7" + allowCasterSpells;

        return asString;
    }
}
