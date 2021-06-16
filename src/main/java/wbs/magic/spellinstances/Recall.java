package wbs.magic.spellinstances;

import org.bukkit.*;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
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
import wbs.utils.util.string.WbsStringify;

import java.time.Duration;
import java.util.Collection;

@Spell(name = "Recall",
        cost = 25,
        cooldown = 30,
        description = "Create a location to teleport back to on subsequent casts of the spell."
)
@SpellSound(sound = Sound.ENTITY_ENDERMAN_TELEPORT)
@SpellSettings(canBeConcentration = true)
@SpellOption(optionName = "max-duration", type = SpellOptionType.DOUBLE, defaultDouble = 120, aliases = {"duration", "timeout"})
@SpellOption(optionName = "force-ground", type = SpellOptionType.BOOLEAN, defaultBool = true, aliases = {"ground"})
@SpellOption(optionName = "auto-recall", type = SpellOptionType.BOOLEAN, defaultBool = false, aliases = {"recall-on-expire"})
@SpellOption(optionName = "below-distance", type = SpellOptionType.DOUBLE, defaultDouble = 255)
public class Recall extends RangedSpell {
    private static final double DEFAULT_RANGE = 75;
    public Recall(SpellConfig config, String directory) {
        super(config, directory, DEFAULT_RANGE);

        maxDuration = config.getDouble("max-duration");
        maxDuration *= 20;

        forceGround = config.getBoolean("force-ground");
        autoRecall = config.getBoolean("auto-recall");

        poofEffect.setAmount(100);
    }

    private double maxDuration;
    private boolean forceGround;
    private boolean autoRecall;
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
            createPoint(caster);
        } else {
            recall(caster, point);
        }

        return true;
    }

    public void recall(SpellCaster caster, RecallPoint point) {
        poofEffect.play(Particle.DRAGON_BREATH,
                WbsEntities.getMiddleLocation(caster.getPlayer()));

        caster.getPlayer().setFallDistance(0);
        caster.getPlayer().teleport(point.getLocation());

        poofEffect.play(Particle.DRAGON_BREATH,
                WbsEntities.getMiddleLocation(caster.getPlayer()));

        // Don't take mana for recalling - already paid
        caster.ignoreNextCost();

        caster.sendActionBar("Recalled!");
        point.remove();
    }

    private void createPoint(SpellCaster caster) {
        if (isConcentration) caster.setConcentration(this);

        Location spawnLoc;
        if (forceGround && !caster.getPlayer().isOnGround()) {
            World world = caster.getLocation().getWorld();
            assert world != null;

            RayTraceResult result = world.rayTraceBlocks(caster.getLocation(), new Vector(0, -1, 0), 255.0, FluidCollisionMode.ALWAYS, true);

            if (result == null) {
                spawnLoc = caster.getLocation();
            } else {
                spawnLoc = result.getHitPosition().toLocation(world);
                spawnLoc.setPitch(caster.getLocation().getPitch());
                spawnLoc.setYaw(caster.getLocation().getYaw());
            }
        } else {
            spawnLoc = caster.getLocation();
        }

        RecallPoint newPoint = new RecallPoint(spawnLoc, caster, this, (int) maxDuration);
        newPoint.setAutoRecall(autoRecall);
        newPoint.run();

        // Don't start cooldown - let caster recall instantly if they want
        caster.ignoreNextCooldown();

        if (autoRecall) {
            caster.sendActionBar("You will return to this point in &h" + WbsStringify.toString(Duration.ofSeconds((long)(maxDuration / 20)), false) + "&r!");
        } else {
            caster.sendActionBar("Cast again to return to this location.");
        }
    }
}
