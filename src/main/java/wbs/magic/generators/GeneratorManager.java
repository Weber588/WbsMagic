package wbs.magic.generators;

import java.lang.annotation.Annotation;
import java.util.*;

public final class GeneratorManager {
    private GeneratorManager() {}

    private static final Map<Annotation, RegisteredGenerator> generatorAnnotations = new HashMap<>();

    public static <T extends Annotation> void registerGenerator(T generator) {
        RegisteredGenerator registeredGenerator = new RegisteredGenerator(generator);

        generatorAnnotations.put(generator, registeredGenerator);
    }

    public List<RegisteredGenerator> getGeneratorsFor(Class<?> annotatedClass) {
        List<RegisteredGenerator> generators = new LinkedList<>();

        for (Annotation annotation : annotatedClass.getAnnotations()) {
            RegisteredGenerator generator = generatorAnnotations.get(annotation);
            if (generator != null) {
                generators.add(generator);
            }
        }

        return generators;
    }
}
