package wbs.magic.spells.framework;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityEvent;
import wbs.magic.SpellCaster;
import wbs.magic.WbsMagic;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;
import wbs.magic.spells.SpellInstance;
import wbs.magic.targeters.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a spell that has a primary targeter which is used for the majority of the spell.
 * @param <T>
 */
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
     * true to cancel, false to run as normal.<br/>
     * The given collection of targets is mutable, and changes will affect calls to
     * {@link #castOn(CastingContext, Entity)}.
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

        boolean sendMessages = true;
        if (this instanceof SpellInstance) {
            sendMessages = ((SpellInstance) this).sendMessages();
        }

        if (sendMessages) {
            sendConfirmationMessage(caster, targets, context.binding.getSpell());
        }

        if (preCastEntity(context, targets)) {
            return true;
        }

        if (targets.isEmpty()) {
            return false;
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

    GenericTargeter getTargeter();

    @Override
    default boolean castWithoutTargets(CastingContext context) {
        context.caster.sendActionBar(getTargeter().getNoTargetsMessage());
        return false;
    }
}
