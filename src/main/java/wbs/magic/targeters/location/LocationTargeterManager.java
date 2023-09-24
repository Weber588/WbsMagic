package wbs.magic.targeters.location;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.MagicSettings;
import wbs.magic.targeters.*;

import java.util.*;
import java.util.function.Supplier;

public final class LocationTargeterManager {
    private static final Map<String, TargeterRegistration<?>> registeredTargeters = new LinkedHashMap<>();

    static {
        TargeterRegistration<SightLocationTargeter> sightReg =
                new TargeterRegistration<>("lineofsight",
                        SightLocationTargeter.class,
                        SightLocationTargeter::new,
                        SightLocationTargeter.DEFAULT_RANGE);
        sightReg.addAliases("looking", "sight");
        registerTargeter(sightReg);

        TargeterRegistration<RingTargeter> ringReg =
                new TargeterRegistration<>("ring",
                        RingTargeter.class,
                        RingTargeter::new,
                        RingTargeter.DEFAULT_RANGE);
        ringReg.addAliases("radius");
        registerTargeter(ringReg);
    }

    /**
     * Register a targeter class with a producer and ID.
     * @param <T> The targeter type.
     */
    public static <T extends LocationTargeter> void registerTargeter(TargeterRegistration<T> registration) {
        String id = registration.getId();

        if (registeredTargeters.putIfAbsent(stripSyntax(id), registration) != null) { // If the value already existed
            MagicSettings.getInstance().logError("Id already registered: \"" + stripSyntax(id) + "\"", "Internal");
        }
    }

    @Nullable
    private static TargeterRegistration<?> getRegistration(Class<? extends LocationTargeter> targeterType) {
        for (String id : registeredTargeters.keySet()) {
            TargeterRegistration<?> reg = registeredTargeters.get(id);
            if (reg.clazz == targeterType) {
                return reg;
            }
        }

        return null;
    }

    @Nullable
    public static Class<? extends LocationTargeter> getTargeterType(String id) {
        id = stripSyntax(id);
        TargeterRegistration<?> registration = registeredTargeters.get(id);

        if (registration == null) {
            for (TargeterRegistration<?> check : registeredTargeters.values()) {
                for (String alias : check.getAliases()) {
                    if (id.equals(alias)) {
                        return check.clazz;
                    }
                }
            }
            return null;
        }

        return registration.clazz;
    }

    @NotNull
    public static String getDefaultId(Class<? extends LocationTargeter> targeterType) {
        for (String id : registeredTargeters.keySet()) {
            TargeterRegistration<?> reg = registeredTargeters.get(id);
            if (reg.clazz == targeterType) {
                return id;
            }
        }

        throw new IllegalStateException("Targeter not registered: " + targeterType.getCanonicalName());
    }

    @Nullable
    public static LocationTargeter getTargeter(String id) {
        TargeterRegistration<?> registration = registeredTargeters.get(stripSyntax(id));

        if (registration == null) {
            return null;
        }

        return registration.supplier.get();
    }

    @NotNull
    public static <T extends LocationTargeter> T getTargeter(Class<T> targeterType) {
        TargeterRegistration<?> reg = getRegistration(targeterType);

        if (reg != null) {
            return targeterType.cast(reg.supplier.get());
        } else {
            throw new IllegalStateException("Targeter not registered: " + targeterType.getCanonicalName());
        }
    }

    public static double getDefaultRange(Class<? extends LocationTargeter> targeterType) {
        TargeterRegistration<?> reg = getRegistration(targeterType);

        if (reg != null) {
            return reg.defaultRange;
        } else {
            throw new IllegalStateException("Targeter not registered: " + targeterType.getCanonicalName());
        }
    }

    public static Collection<String> getDefaultIds() {
        Map<Class<? extends LocationTargeter>, String> defaults = new HashMap<>();
        for (String id : registeredTargeters.keySet()) {
            TargeterRegistration<?> reg = registeredTargeters.get(id);
            if (!defaults.containsKey(reg.clazz)) {
                defaults.put(reg.clazz, id);
            }
        }

        return defaults.values();
    }

    public static String stripSyntax(String idString) {
        return idString.replaceAll("[\\s_-]", "").toLowerCase();
    }

    private static class TargeterRegistration<T extends LocationTargeter> {
        @NotNull
        private final Class<T> clazz;
        @NotNull
        private final Supplier<T> supplier;
        private final double defaultRange;
        private final String id;
        private final List<String> aliases = new LinkedList<>();

        /**
         * @param id The id to use in configs to represent a targeter type.
         * @param clazz The targeter type.
         * @param supplier The producer which must produce an instance of the given clazz which may not be null.
         * @param defaultRange The default range
         */
        public TargeterRegistration(String id, @NotNull Class<T> clazz, @NotNull Supplier<T> supplier, double defaultRange) {
            this.id = id;
            this.clazz = clazz;
            this.supplier = supplier;
            this.defaultRange = defaultRange;
        }

        public String getId() {
            return id;
        }

        public List<String> getAliases() {
            return aliases;
        }

        public void addAliases(String ... aliases) {
            this.aliases.addAll(Arrays.asList(aliases));
        }
    }
}
