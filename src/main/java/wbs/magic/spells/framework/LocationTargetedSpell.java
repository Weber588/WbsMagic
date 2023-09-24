package wbs.magic.spells.framework;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import wbs.magic.SpellCaster;
import wbs.magic.spells.SpellInstance;
import wbs.magic.targeters.location.LocationTargeter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public interface LocationTargetedSpell extends TargetedSpell {
    default boolean castLocation(CastingContext context) {
        SpellCaster caster = context.caster;
        List<Location> targets = new LinkedList<>();

        LocationTargeter targeter = getTargeter();
        double range = targeter.getRange();

        // TODO: Find a way to make targeters override this optionally
        if (context.eventDetails.getBlock() != null) {
            Block block = context.eventDetails.getBlock();

            if (block.getLocation().distanceSquared(caster.getEyeLocation()) < range * range) {
                targets.add(block.getLocation());
            }
        }

        if (targets.isEmpty()) {
            targets.addAll(targeter.getLocations(caster));
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

        if (preCastLocation(context, targets)) {
            return true;
        }

        if (targets.isEmpty()) {
            return false;
        }

        targets.forEach(target -> castOnLocation(context, caster.getPlayer().getWorld(), target));

        return true;
    }

    void castOnLocation(@NotNull CastingContext context, @NotNull World world, @NotNull Location target);

    default boolean preCastLocation(CastingContext context, List<Location> targets) {
        return false;
    }

    default void sendConfirmationMessage(SpellCaster caster, Collection<Location> targets, SpellInstance spell) {
        caster.sendActionBar("Cast &h" + spell.getName() + "&r on &h" + targets.size() + "&r locations!");
    }

    LocationTargeter getTargeter();

    @Override
    default boolean castWithoutTargets(CastingContext context) {
        context.caster.sendActionBar(getTargeter().getNoTargetsMessage());
        return false;
    }
}
