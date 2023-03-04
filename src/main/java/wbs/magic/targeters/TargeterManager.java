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
        TargeterRegistration<LineOfSightTargeter> lineReg =
                new TargeterRegistration<>("lineofsight",
                        LineOfSightTargeter.class,
                        LineOfSightTargeter::new,
                        LineOfSightTargeter.DEFAULT_RANGE);
        lineReg.addAliases("looking", "sight");
        registerTargeter(lineReg);

        TargeterRegistration<NearestTargeter> nearestReg =
                new TargeterRegistration<>("nearest",
                        NearestTargeter.class,
                        NearestTargeter::new,
                        NearestTargeter.DEFAULT_RANGE);
        nearestReg.addAliases("closest");
        registerTargeter(nearestReg);

        TargeterRegistration<SelfTargeter> selfReg =
                new TargeterRegistration<>("self",
                        SelfTargeter.class,
                        SelfTargeter::new,
                        SelfTargeter.DEFAULT_RANGE);
        selfReg.addAliases("user", "player", "caster");
        registerTargeter(selfReg);

        TargeterRegistration<RadiusTargeter> radiusReg =
                new TargeterRegistration<>("radius",
                        RadiusTargeter.class,
                        RadiusTargeter::new,
                        RadiusTargeter.DEFAULT_RANGE);
        radiusReg.addAliases("near", "close");
        registerTargeter(radiusReg);

        TargeterRegistration<RandomTargeter> randomReg =
                new TargeterRegistration<>("random",
                        RandomTargeter.class,
                        RandomTargeter::new,
                        RandomTargeter.DEFAULT_RANGE);
        registerTargeter(randomReg);

        TargeterRegistration<VanillaTargeter> vanillaReg =
                new TargeterRegistration<>("vanilla",
                        VanillaTargeter.class,
                        VanillaTargeter::new,
                        VanillaTargeter.DEFAULT_RANGE);
        radiusReg.addAliases("minecraft");
        registerTargeter(vanillaReg);
    }

    /**
     * Register a targeter class with a producer and ID.
     * @param <T> The targeter type.
     */
    public static <T extends GenericTargeter> void registerTargeter(TargeterRegistration<T> registration) {
        String id = registration.getId();

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
