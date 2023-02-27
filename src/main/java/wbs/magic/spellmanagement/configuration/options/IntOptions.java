package wbs.magic.spellmanagement.configuration.options;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntOptions {
    IntOption[] value();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(IntOptions.class)
    @interface IntOption {
        String optionName();
        int defaultValue();

        int[] suggestions() default {1, 2, 5, 10, 25};

        String[] aliases() default {};

        boolean saveToDefaults() default true;
    }
}
