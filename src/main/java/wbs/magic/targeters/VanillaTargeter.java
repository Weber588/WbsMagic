package wbs.magic.targeters;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.magic.MagicSettings;
import wbs.magic.SpellCaster;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class VanillaTargeter extends GenericTargeter {

    public static double DEFAULT_RANGE = 5;

    private final static String DISTANCE_KEY = "distance";
    public final static String DEFAULT_SELECTOR = "@e[" + DISTANCE_KEY + "=0..%range%]";

    private final static String CASTER = "caster";
    private final static String CASTER_LOOKING = CASTER + "-looking";
    private final static String RANGE = "range";

    @NotNull
    private String selectorString = DEFAULT_SELECTOR;

    public void setSelectorString(@NotNull String selectorString) {
        if (!selectorString.toLowerCase().contains(DISTANCE_KEY)) {
            if (selectorString.endsWith("]")) {
                // Remove closing square bracket
                selectorString = selectorString.substring(0, selectorString.length() - 1);
                selectorString += ",";
            } else {
                selectorString += "[";
            }
            selectorString += DISTANCE_KEY + "=0.." + range + "]";
        } else if (selectorString.contains("..")) {
            int distIndex = selectorString.indexOf(DISTANCE_KEY);
            int upperBoundIndex = selectorString.substring(distIndex).indexOf("..");
            if (upperBoundIndex != -1) {
                upperBoundIndex += 2; // Move past ".."
                upperBoundIndex += distIndex;

                StringBuilder upperBoundString = new StringBuilder();
                int index = upperBoundIndex;
                char checkChar = selectorString.charAt(index);
                boolean hasHadPeriod = false;
                while (index < selectorString.length() && (Character.isDigit(checkChar) || (!hasHadPeriod && checkChar == '.'))) {
                    if (checkChar == '.') {
                        hasHadPeriod = true;
                    }
                    upperBoundString.append(checkChar);
                    index++;
                }

                double upperBound = -1;
                try {
                    upperBound = Double.parseDouble(upperBoundString.toString());
                } catch (NumberFormatException e) {
                    // Shouldn't be possible unless something is malformed
                    e.printStackTrace();
                }

                if (upperBound >= range) {
                    // Undo last ++ if the string length was hit
                    if (index == selectorString.length()) {
                        index--;
                    }
                    selectorString = selectorString.substring(0, upperBoundIndex) + range + selectorString.substring(index);
                }
            }
        }

        this.selectorString = selectorString;
    }

    public @NotNull String getSelectorString() {
        return selectorString;
    }

    @Override
    public <T extends Entity> List<T> getTargets(SpellCaster caster, Class<T> clazz) {
        String selector = getSelectorString();

        Player player = caster.getPlayer();

        Predicate<Entity> predicate = getPredicate(caster, clazz);

        // Fill placeholders
        if (selector.contains("%")) {
            selector = fillPlaceholders(caster, predicate, selector);
        }

        List<Entity> entities;
        try {
            entities = Bukkit.selectEntities(player, selector);
        } catch (IllegalArgumentException e) {
            MagicSettings.getInstance().logError("Invalid vanilla targeter used: " + selector, "(Unknown)");
            entities = Bukkit.selectEntities(player, fillPlaceholders(caster, predicate, DEFAULT_SELECTOR));
        }

        List<T> targets = new LinkedList<>();

        for (Entity entity : entities) {
            if (!predicate.test(entity)) continue;

            if (clazz.isAssignableFrom(entity.getClass())) {
                targets.add(clazz.cast(entity));
            }
        }

        return targets;
    }

    private String fillPlaceholders(SpellCaster caster, Predicate<Entity> predicate, String selector) {
        Player player = caster.getPlayer();
        Location casterLoc = player.getLocation();

        if (selector.contains(CASTER)) {

            selector = selector.replaceAll("%" + CASTER + "%", caster.getName());
            selector = selector.replaceAll("%" + CASTER + "-name%", caster.getName());

            selector = selector.replaceAll("%" + CASTER + "-x%", casterLoc.getX() + "");
            selector = selector.replaceAll("%" + CASTER + "-y%", casterLoc.getY() + "");
            selector = selector.replaceAll("%" + CASTER + "-z%", casterLoc.getZ() + "");
        }

        if (selector.contains(RANGE)) {
            selector = selector.replaceAll("%" + RANGE + "%", range + "");
        }

        if (selector.contains(CASTER_LOOKING)) {
            World world = player.getWorld();
            RayTraceResult result =
                    world.rayTrace(player.getEyeLocation(),
                            caster.getFacingVector(),
                            100,
                            FluidCollisionMode.NEVER,
                            false,
                            1,
                            predicate);

            if (result != null) {
                Vector hitPos = result.getHitPosition();
                selector = selector.replaceAll("%" + CASTER_LOOKING + "-x%", hitPos.getX() + "");
                selector = selector.replaceAll("%" + CASTER_LOOKING + "-y%", hitPos.getY() + "");
                selector = selector.replaceAll("%" + CASTER_LOOKING + "-z%", hitPos.getZ() + "");
            } else {
                selector = selector.replaceAll("%" + CASTER_LOOKING + "-x%", casterLoc.getX() + "");
                selector = selector.replaceAll("%" + CASTER_LOOKING + "-y%", casterLoc.getY() + "");
                selector = selector.replaceAll("%" + CASTER_LOOKING + "-z%", casterLoc.getZ() + "");
            }
        }

        return selector;
    }

    @Override
    public String getNoTargetsMessage() {
        return "No targets found!";
    }

    @Override
    public String toString() {
        return "Vanilla (" + getSelectorString() + ")";
    }
}
