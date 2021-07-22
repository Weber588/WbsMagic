package wbs.magic.annotations.generators;

import org.jetbrains.annotations.Nullable;
import wbs.magic.MagicSettings;
import wbs.magic.WbsMagic;
import wbs.magic.annotations.SpellOption;
import wbs.magic.annotations.SpellOptions;
import wbs.magic.spellinstances.SpellInstance;
import wbs.magic.spells.SpellConfig;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class RegisteredGenerator {

    private final Annotation annotation;
    private final List<SpellOption> options = new LinkedList<>();
    private final Class<? extends OptionGenerator> generatorClass;

    @SuppressWarnings("unchecked")
    public RegisteredGenerator(Annotation annotation) {
        this.annotation = annotation;

        GeneratorType generatorType;
        Class<? extends Annotation> annotationClass = annotation.getClass();

        try {
            generatorType = annotationClass.getAnnotation(GeneratorType.class);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("The specified annotation lacks the GeneratorType annotation.");
        }

        generatorClass = generatorType.value();

        SpellOptions options = annotationClass.getAnnotation(SpellOptions.class);
        if (options != null) {
            SpellOption[] optionArray = options.value();
            this.options.addAll(Arrays.asList(optionArray));
        } else { // Try for single SpellOption
            SpellOption option = annotationClass.getAnnotation(SpellOption.class);
            if (option != null) {
                this.options.add(option);
            }
        }


    }

    @Nullable
    public OptionGenerator buildGenerator(SpellConfig config, String directory) {
        OptionGenerator generator;

        MagicSettings settings = WbsMagic.getInstance().settings;

        try {
            Constructor<? extends OptionGenerator> constructor = generatorClass.getConstructor(SpellConfig.class, MagicSettings.class, String.class);
            generator = constructor.newInstance(config, settings, directory);
        } catch (SecurityException | NoSuchMethodException | InstantiationException
                | IllegalAccessException | IllegalArgumentException e) {
            settings.logError("Invalid constructor for generator type " + generatorClass.getSimpleName(), directory);
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e){
            Throwable cause = e.getCause();
            if (cause instanceof InvalidConfigurationException) {
                settings.logError(cause.getMessage(), directory);
            } else {
                settings.logError("An error occurred while constructing generator " + generatorClass.getSimpleName(), directory);
                e.printStackTrace();
            }
            return null;
        }

        return generator;
    }

    public Collection<SpellOption> getOptions() {
        return Collections.unmodifiableCollection(options);
    }

    public Annotation getAnnotation() {
        return annotation;
    }
}
