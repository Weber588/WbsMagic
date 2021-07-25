package wbs.magic.spells.ranged;

import org.bukkit.*;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellSettings;
import wbs.magic.spellmanagement.configuration.SpellSound;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.objects.RecallPoint;
import wbs.magic.objects.generics.MagicObject;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.SpellCaster;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.WbsSound;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.string.WbsStringify;

import java.time.Duration;
import java.util.Collection;

@Spell(name = "Recall",
        cost = 25,
        cooldown = 30,
        description = "Create a location to teleport back to on subsequent casts of the spell."
)
@SpellSettings(canBeConcentration = true)
@SpellOption(optionName = "max-duration", type = SpellOptionType.DOUBLE, defaultDouble = 120, aliases = {"duration", "timeout"})
@SpellOption(optionName = "force-ground", type = SpellOptionType.BOOLEAN, defaultBool = true, aliases = {"ground"})
@SpellOption(optionName = "auto-recall", type = SpellOptionType.BOOLEAN, defaultBool = false, aliases = {"recall-on-expire"})
@SpellOption(optionName = "below-distance", type = SpellOptionType.DOUBLE, defaultDouble = 255)
//Overrides
@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 75)
public class Recall extends RangedSpell {
    public Recall(SpellConfig config, String directory) {
        super(config, directory);

        maxDuration = config.getDouble("max-duration") * 20;

        forceGround = config.getBoolean("force-ground");
        autoRecall = config.getBoolean("auto-recall");

        poofEffect.setAmount(100);
    }

    private final double maxDuration;
    private final boolean forceGround;
    private final boolean autoRecall;
    private final NormalParticleEffect poofEffect = new NormalParticleEffect().setXYZ(0).setSpeed(0.02);

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

    private final WbsSound sound = new WbsSound(Sound.ENTITY_ENDERMAN_TELEPORT);

    public void recall(SpellCaster caster, RecallPoint point) {
        poofEffect.play(Particle.DRAGON_BREATH,
                WbsEntities.getMiddleLocation(caster.getPlayer()));

        caster.getPlayer().setFallDistance(0);
        caster.getPlayer().teleport(point.getLocation());

        poofEffect.play(Particle.DRAGON_BREATH,
                WbsEntities.getMiddleLocation(caster.getPlayer()));

        // Don't take mana for recalling - already paid
        caster.ignoreNextCost();

        sound.play(point.getLocation());
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
        if (consumeWand()) {
            caster.ignoreNextConsume();
        }

        if (autoRecall) {
            caster.sendActionBar("You will return to this point in &h" + WbsStringify.toString(Duration.ofSeconds((long)(maxDuration / 20)), false) + "&r!");
        } else {
            caster.sendActionBar("Cast again to return to this location.");
        }
    }

    @Override
    public String toString() {
        String asString = super.toString();

        asString += "\n&rDuration: &7" + (maxDuration / 20) + " seconds";
        asString += "\n&rAuto recall? &7" + autoRecall;
        asString += "\n&rForce ground? &7" + forceGround;

        return asString;
    }
}
