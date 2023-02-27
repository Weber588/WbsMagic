package wbs.magic.targeters;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.MagicSettings;

import java.util.*;
import java.util.function.Supplier;

public final class TargeterManager {
    private TargeterManager() {}

    private static final Map<String, TargeterRegistration<?>> registeredTargeters = new LinkedHashMap<>();

    static {
        registerTargeter("lineofsight", LineOfSightTargeter.class, LineOfSightTargeter::new, LineOfSightTargeter.DEFAULT_RANGE);
        registerTargeter("looking", LineOfSightTargeter.class, LineOfSightTargeter::new, LineOfSightTargeter.DEFAULT_RANGE);
        registerTargeter("sight", LineOfSightTargeter.class, LineOfSightTargeter::new, LineOfSightTargeter.DEFAULT_RANGE);

        registerTargeter("nearest", NearestTargeter.class, NearestTargeter::new, NearestTargeter.DEFAULT_RANGE);
        registerTargeter("closest", NearestTargeter.class, NearestTargeter::new, NearestTargeter.DEFAULT_RANGE);

        registerTargeter("self", SelfTargeter.class, SelfTargeter::new, SelfTargeter.DEFAULT_RANGE);
        registerTargeter("user", SelfTargeter.class, SelfTargeter::new, SelfTargeter.DEFAULT_RANGE);
        registerTargeter("player", SelfTargeter.class, SelfTargeter::new, SelfTargeter.DEFAULT_RANGE);
        registerTargeter("caster", SelfTargeter.class, SelfTargeter::new, SelfTargeter.DEFAULT_RANGE);

        registerTargeter("radius", RadiusTargeter.class, RadiusTargeter::new, RadiusTargeter.DEFAULT_RANGE);
        registerTargeter("near", RadiusTargeter.class, RadiusTargeter::new, RadiusTargeter.DEFAULT_RANGE);
        registerTargeter("close", RadiusTargeter.class, RadiusTargeter::new, RadiusTargeter.DEFAULT_RANGE);

        registerTargeter("random", RandomTargeter.class, RandomTargeter::new, RandomTargeter.DEFAULT_RANGE);
    }

    /**
     * Register a targeter class with a producer and ID.
     * @param id The id to use in configs to represent a targeter type.
     * @param clazz The targeter type.
     * @param supplier The producer which must produce an instance of the given clazz which may not be null.
     * @param <T> The targeter type.
     */
    public static <T extends GenericTargeter> void registerTargeter(String id, Class<T> clazz, Supplier<T> supplier, double defaultRange) {
        TargeterRegistration<T> registration = new TargeterRegistration<>(clazz, supplier, defaultRange);

        if (registeredTargeters.putIfAbsent(stripSyntax(id), registration) != null) { // If the value already existed
            MagicSettings.getInstance().logError("Id already registered: \"" + stripSyntax(id) + "\"", "Internal");
        }
    }

    @Nullable
    private static TargeterRegistration<?> getRegistration(Class<? extends GenericTargeter> targeterType) {
        for (String id : registeredTargeters.keySet()) {
            TargeterRegistration<?> reg = registeredTargeters.get(id);
            if (reg.clazz == targeterType) {
                return reg;
            }
        }

        return null;
    }

    @Nullable
    public static Class<? extends GenericTargeter> getTargeterType(String id) {
        TargeterRegistration<?> registration = registeredTargeters.get(stripSyntax(id));

        if (registration == null) {
            return null;
        }

        return registration.clazz;
    }

    @NotNull
    public static String getDefaultId(Class<? extends GenericTargeter> targeterType) {
        for (String id : registeredTargeters.keySet()) {
            TargeterRegistration<?> reg = registeredTargeters.get(id);
            if (reg.clazz == targeterType) {
                return id;
            }
        }

        throw new IllegalStateException("Targeter not registered: " + targeterType.getCanonicalName());
    }

    @Nullable
    public static GenericTargeter getTargeter(String id) {
        TargeterRegistration<?> registration = registeredTargeters.get(stripSyntax(id));

        if (registration == null) {
            return null;
        }

        return registration.supplier.get();
    }

    @NotNull
    public static <T extends GenericTargeter> T getTargeter(Class<T> targeterType) {
        TargeterRegistration<?> reg = getRegistration(targeterType);

        if (reg != null) {
            return targeterType.cast(reg.supplier.get());
        } else {
            throw new IllegalStateException("Targeter not registered: " + targeterType.getCanonicalName());
        }
    }

    public static double getDefaultRange(Class<? extends GenericTargeter> targeterType) {
        TargeterRegistration<?> reg = getRegistration(targeterType);

        if (reg != null) {
            return reg.defaultRange;
        } else {
            throw new IllegalStateException("Targeter not registered: " + targeterType.getCanonicalName());
        }
    }

    public static Collection<String> getDefaultIds() {
        Map<Class<? extends GenericTargeter>, String> defaults = new HashMap<>();
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

    private static class TargeterRegistration<T extends GenericTargeter> {
        @NotNull
        private final Class<T> clazz;
        @NotNull
        private final Supplier<T> supplier;
        private final double defaultRange;

        private TargeterRegistration(@NotNull Class<T> clazz, @NotNull Supplier<T> supplier, double defaultRange) {
            this.clazz = clazz;
            this.supplier = supplier;
            this.defaultRange = defaultRange;
        }
    }
}
