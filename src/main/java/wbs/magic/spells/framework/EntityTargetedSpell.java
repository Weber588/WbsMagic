package wbs.magic.spells.framework;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityEvent;
import wbs.magic.SpellCaster;
import wbs.magic.WbsMagic;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spells.SpellInstance;
import wbs.magic.targeters.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@SpellOption(optionName = "targeter", type = SpellOptionType.STRING, defaultString = "LINE_OF_SIGHT", aliases = {"target", "targetter"})
public interface EntityTargetedSpell<T extends Entity> extends TargetedSpell {

    Class<T> getEntityClass();

    /**
     * Run the spells effect on a given target. Potentially run multiple times per
     * casting, as it happens for each target.
     * @param context The caster
     * @param target The target to cast on
     */
    void castOn(CastingContext context, T target);

    /**
     * Run before each target has {@link #castOn(CastingContext, T)} called on it. Mainly for setting concentration.
     * Also returns a boolean for canceling the spell before it is run on each target;
     * true to cancel, false to run as normal.
     * @param context The context of the cast
     * @return true to cancel
     */
    default boolean preCastEntity(CastingContext context, Collection<T> targets) {
        return false;
    }

    @SuppressWarnings("unchecked")
    default boolean castEntity(CastingContext context) {
        SpellCaster caster = context.caster;
        Set<T> targets = new HashSet<>();

        GenericTargeter targeter = getTargeter();

        // TODO: Find a way to make targeters override this optionally
        if (context.eventDetails.getOtherEntity() != null) {
            Entity entity = context.eventDetails.getOtherEntity();

            if (getEntityClass().isInstance(entity)) {
                targets.add((T) entity);
            } else {
                return false;
            }
        }

        if (targets.isEmpty()) {
            targets = targeter.getTargets(caster, getEntityClass());
        }

        if (targets.isEmpty()) {
            return castWithoutTargets(context);
        }

        sendConfirmationMessage(caster, targets, context.binding.getSpell());

        if (preCastEntity(context, targets)) {
            return true;
        }

        targets.forEach(target -> castOn(context, target));

        return true;
    }

    default void sendConfirmationMessage(SpellCaster caster, Set<T> targets, SpellInstance spell) {
        if (targets.size() == 1) {
            T displayTarget = null;
            for (T target : targets) {
                displayTarget = target;
            }
            caster.sendActionBar("Cast &h" + spell.getName() + "&r on &h" + displayTarget.getName() + "&r!");
        } else {
            caster.sendActionBar("Cast &h" + spell.getName() + "&r on &h" + targets.size() + "&r creatures!");
        }
    }

    void setTargeter(GenericTargeter targeter);
    GenericTargeter getTargeter();

    default void configureTargeter(SpellConfig config, String directory) {
        String targeterString = "";
        targeterString = config.getString("targeter", targeterString);

        double range = config.getDouble("range");

        GenericTargeter targeter = null;

        switch (targeterString.toUpperCase().replace(" ", "").replace("_", "")) {
            case "LINEOFSIGHT":
            case "LOOKING":
            case "SIGHT":
                if (range == -1) {
                    range = LineOfSightTargeter.DEFAULT_RANGE;
                }
                targeter = new LineOfSightTargeter(range);
                break;
            case "NEAREST":
            case "CLOSEST":
                if (range == -1) {
                    range = NearestTargeter.DEFAULT_RANGE;
                }
                targeter = new NearestTargeter(range);
                break;
            case "SELF":
            case "USER":
            case "PLAYER":
            case "CASTER":
                if (range == -1) {
                    range = SelfTargeter.DEFAULT_RANGE;
                }
                targeter = new SelfTargeter();
                break;
            case "RADIUS":
            case "NEAR":
            case "CLOSE":
                if (range == -1) {
                    range = RadiusTargeter.DEFAULT_RANGE;
                }
                targeter = new RadiusTargeter(range);
                break;
            case "RANDOM":
                if (range == -1) {
                    range = RandomTargeter.DEFAULT_RANGE;
                }
                targeter = new RandomTargeter(range);
                break;
            default:
                if (!targeterString.equals("")) {
                    WbsMagic.getInstance().settings.logError("Invalid targeter: " + targeterString, directory);
                }
                targeter = new LineOfSightTargeter(range);
        }
        setTargeter(targeter);
    }
}
