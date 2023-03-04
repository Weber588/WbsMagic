package wbs.magic.spells.ranged;

import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import wbs.magic.controls.WandControl;
import wbs.magic.objects.AlignmentType;
import wbs.magic.objects.HomingProjectileObject;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.RestrictWandControls;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.configuration.options.EnumOptions;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.spells.framework.RawSpell;

@Spell(name = "Homing Projectile",
        cost = 0,
        cooldown = 0,
        description = "Projectiles shot from the "
)
@SpellOption(optionName = "lifetime-angle", type = SpellOptionType.DOUBLE, defaultDouble = 180, aliases = {"max-angle"})
@SpellOption(optionName = "update-rate", type = SpellOptionType.INT, defaultInt = 5, aliases = {"target-rate"})
@SpellOption(optionName = "angle-per-second", type = SpellOptionType.DOUBLE, defaultInt = 60, aliases = {"turn-rate"})
@RestrictWandControls(dontRestrictLineOfSight = true, limitedControls = {WandControl.SHOOT})
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.NEGATIVE, enumType = AlignmentType.class)
public class HomingProjectile extends RangedSpell implements RawSpell {
    public HomingProjectile(SpellConfig config, String directory) {
        super(config, directory);

        lifetimeAngle = config.getDouble("lifetime-angle");
        updateRate = config.getInt("update-rate");
        anglePerTick = (config.getDouble("angle-per-second") / 20);
    }

    private final double lifetimeAngle;
    private final int updateRate;
    private final double anglePerTick;

    @Override
    public boolean castRaw(CastingContext context) {
        Event event = context.eventDetails.event;

        if (!(event instanceof ProjectileLaunchEvent)) {
            return false;
        }

        ProjectileLaunchEvent projEvent = (ProjectileLaunchEvent) event;
        Projectile proj = projEvent.getEntity();

        HomingProjectileObject projObject = new HomingProjectileObject(proj, context.caster, this);

        projObject.setLifetimeAngle(lifetimeAngle);
        projObject.setHomingRange(range);
        projObject.setUpdateRate(updateRate);
        projObject.setAnglePerTick(anglePerTick);

        projObject.run();

        return false;
    }
}
