package wbs.magic.spellinstances;

import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.magic.annotations.*;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.objects.AntiMagicShellObject;
import wbs.magic.spells.SpellConfig;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.particles.SphereParticleEffect;

@Spell(name = "Anti Magic Shell",
        cost = 25,
        cooldown = 5,
        description = "Form a shield around you that prevents any magic objects from entering or leaving"
)
@SpellSettings(canBeConcentration = true)
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 10)
@SpellOption(optionName = "radius", type = SpellOptionType.DOUBLE, defaultDouble = 6)
@SpellOption(optionName = "reflect", type = SpellOptionType.BOOLEAN, defaultBool = true)
@SpellOption(optionName = "follow-player", type = SpellOptionType.BOOLEAN, defaultBool = false)
@SpellOption(optionName = "allow-caster-spells", type = SpellOptionType.BOOLEAN, defaultBool = true)
@SpellOption(optionName = "hits", type = SpellOptionType.INT, defaultInt = 5)
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
    public boolean cast(SpellCaster caster) {

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
}
