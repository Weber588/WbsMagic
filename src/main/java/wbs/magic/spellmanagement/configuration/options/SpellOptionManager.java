package wbs.magic.spellmanagement.configuration.options;

import org.jetbrains.annotations.Nullable;
import wbs.magic.exceptions.OptionAlreadyRegisteredException;
import wbs.magic.generators.EntityGenerator;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.options.BoolOptions.BoolOption;
import wbs.magic.spellmanagement.configuration.options.DoubleOptions.DoubleOption;
import wbs.magic.spellmanagement.configuration.options.EntityOptions.EntityOption;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spellmanagement.configuration.options.IntOptions.IntOption;
import wbs.magic.spellmanagement.configuration.options.StringOptions.StringOption;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Singleton manager for registering and linking annotation options to {@link ConfiguredSpellOption}s.
 */
public final class SpellOptionManager {
    private SpellOptionManager() {}

    public static void registerNativeOptions() {
        register(DoubleOption.class, Double.class, ConfiguredDoubleOption::new, DoubleOption::optionName);
        register(IntOption.class, Integer.class, ConfiguredIntegerOption::new, IntOption::optionName);
        register(StringOption.class, String.class, ConfiguredStringOption::new, StringOption::optionName);
        register(BoolOption.class, Boolean.class, ConfiguredBooleanOption::new, BoolOption::optionName);

        register(EnumOption.class, Enum.class, ConfiguredEnumOption::new, EnumOption::optionName);

        register(TargeterOption.class, GenericTargeter.class, ConfiguredTargeterOption::new, TargeterOption::optionName);
        register(EntityOption.class, EntityGenerator.class, ConfiguredEntityOption::new, EntityOption::optionName);

        register(SpellOption.class, Object.class, ConfiguredLegacySpellOption::new, SpellOption::optionName);
    }

    private static final List<RegisteredSpellOption<?, ?>> registeredOptions = new LinkedList<>();

    public static <T, K extends Annotation> void register(Class<K> annotationClass, Class<T> type,
                                                          Function<K, ConfiguredSpellOption<T, K>> producer, Function<K, String> nameGetter) {
        if (getRegistration(annotationClass) != null) {
            throw new OptionAlreadyRegisteredException("SpellOption annotation class was already registered.");
        }

        RegisteredSpellOption<T, K> registration = new RegisteredSpellOption<>(annotationClass, type, producer, nameGetter);

        registeredOptions.add(registration);
    }

    @Nullable
    public static <K extends Annotation> RegisteredSpellOption<?, K> getRegistration(Class<K> annotationClass) {
        for (RegisteredSpellOption<?, ?> registration : registeredOptions) {
            if (registration.getAnnotationClass().equals(annotationClass)) {
                //noinspection unchecked
                return (RegisteredSpellOption<?, K>) registration;
            }
        }
        return null;
    }

    @Nullable
    public static <K extends Annotation> ConfiguredSpellOption<?, K> getConfiguredOption(K annotation) {
        RegisteredSpellOption<?, ?> registration = getRegistration(annotation.annotationType());

        if (registration == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        RegisteredSpellOption<?, K> typedRegistration = (RegisteredSpellOption<?, K>) registration;

        return typedRegistration.producer.apply(annotation);
    }

    @Nullable
    public static <K extends Annotation> String getOptionName(K annotation) {
        RegisteredSpellOption<?, ?> registration = getRegistration(annotation.annotationType());

        if (registration == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        RegisteredSpellOption<?, K> typedRegistration = (RegisteredSpellOption<?, K>) registration;

        return typedRegistration.nameGetter.apply(annotation);
    }

    public static List<Class<? extends Annotation>> getRegisteredAnnotations() {
        return registeredOptions.stream()
                .map(RegisteredSpellOption::getAnnotationClass)
                .collect(Collectors.toList());
    }

    private static class RegisteredSpellOption<T, K extends Annotation> {
        private final Class<K> annotationClass;
        private final Class<T> type;
        private final Function<K, ConfiguredSpellOption<T, K>> producer;
        private final Function<K, String> nameGetter;

        public RegisteredSpellOption(Class<K> annotationClass, Class<T> type, Function<K, ConfiguredSpellOption<T, K>> producer, Function<K, String> nameGetter) {
            this.annotationClass = annotationClass;
            this.type = type;
            this.producer = producer;
            this.nameGetter = nameGetter;
        }

        public Class<K> getAnnotationClass() {
            return annotationClass;
        }
    }
}
