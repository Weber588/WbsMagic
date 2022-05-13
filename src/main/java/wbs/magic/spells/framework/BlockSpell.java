package wbs.magic.spells.framework;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a spell that can cast on a block or
 * set of blocks.
 */
public interface BlockSpell extends TargetedSpell {

    /**
     * Run the spells effect on a given target. Potentially run multiple times per
     * casting, as it happens for each target.
     * @param context The caster
     * @param target The target to cast on
     */
    void castOn(CastingContext context, Block target);

    /**
     * Run before each target has {@link #castOn(CastingContext, Block)} called on it. Mainly for setting concentration.
     * Also returns a boolean for canceling the spell before it is run on each target;
     * true to cancel, false to run as normal.
     * @param context The context of the cast
     * @return true to cancel
     */
    default boolean preCastBlock(CastingContext context, Collection<Block> targets) {
        return false;
    }

    default boolean castBlock(CastingContext context) {
        Set<Block> targets = new HashSet<>();

        // TODO: Implement a nullable block targeter that overrides this to work in non-block events
        if (context.eventDetails.getBlock() != null) {
            targets.add(context.eventDetails.getBlock());
        }

        if (preCastBlock(context, targets)) {
            return true;
        }

        targets.forEach(target -> castOn(context, target));

        return true;
    }
}
