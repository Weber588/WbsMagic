package wbs.magic.spellinstances;

import org.bukkit.Particle;
import org.bukkit.Sound;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.annotations.SpellSettings;
import wbs.magic.annotations.SpellSound;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.objects.RecallPoint;
import wbs.magic.objects.generics.MagicObject;
import wbs.magic.spellinstances.ranged.RangedSpell;
import wbs.magic.spells.SpellConfig;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.particles.NormalParticleEffect;

import java.util.Collection;

@Spell(name = "Recall",
        cost = 25,
        cooldown = 30,
        description = "Create a location to teleport back to on subsequent casts of the spell."
)
@SpellSound(sound = Sound.ENTITY_ENDERMAN_TELEPORT)
@SpellSettings(canBeConcentration = true)
@SpellOption(optionName = "max-duration", type = SpellOptionType.DOUBLE, defaultDouble = 120, aliases = {"duration", "timeout"})
public class Recall extends RangedSpell {
    private static final double DEFAULT_RANGE = 75;
    public Recall(SpellConfig config, String directory) {
        super(config, directory, DEFAULT_RANGE);

        maxDuration = config.getDouble("max-duration");
        maxDuration = config.getDouble("duration", maxDuration);
        maxDuration = config.getDouble("timeout", maxDuration);
        maxDuration *= 20;


        poofEffect.setAmount(100);
    }

    private double maxDuration;
    private NormalParticleEffect poofEffect = new NormalParticleEffect().setXYZ(0).setSpeed(0.02);

    @Override
    public boolean cast(SpellCaster caster) {

        Collection<MagicObject> ownedObjects = MagicObject.getAllActive(caster);

        RecallPoint point = null;
        for (MagicObject obj : ownedObjects) {
            if (obj instanceof RecallPoint && obj.getSpell() == this) {
                if (obj.getLocation().distance(caster.getLocation()) > range) {
                    caster.sendActionBar("Too far away!");
                    return false;
                } else {
                    point = (RecallPoint) obj;
                    break;
                }
            }
        }

        if (point == null) {
            if (isConcentration) caster.setConcentration(this);
            RecallPoint newPoint = new RecallPoint(caster.getLocation(), caster, this, (int) maxDuration);

            newPoint.run();

            // Don't start cooldown - let caster recall instantly if they want
            caster.ignoreNextCooldown();

            caster.sendActionBar("Cast again to return to this location.");
        } else {
            poofEffect.play(Particle.DRAGON_BREATH,
                    WbsEntities.getMiddleLocation(caster.getPlayer()));

            caster.getPlayer().setFallDistance(0);
            caster.getPlayer().teleport(point.getLocation());

            poofEffect.play(Particle.DRAGON_BREATH,
                    WbsEntities.getMiddleLocation(caster.getPlayer()));

            // Don't take mana for recalling - already paid
            caster.ignoreNextCost();

            caster.sendActionBar("Recalled!");
            point.fizzle();
        }

        return true;
    }
}
