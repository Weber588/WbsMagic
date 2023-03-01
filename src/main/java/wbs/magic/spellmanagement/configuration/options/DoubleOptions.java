package wbs.magic.spellmanagement.configuration.options;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DoubleOptions {
    DoubleOption[] value();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(DoubleOptions.class)
    @interface DoubleOption {
        String optionName();
        double defaultValue();

        double[] listDefaults() default {};

        double[] suggestions() default {0.2, 0.5, 1.0, 10.0};

        String[] aliases() default {};

        boolean saveToDefaults() default true;
    }
}
