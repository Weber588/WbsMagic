package wbs.magic.spellmanagement.configuration.options;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StringOptions {
    StringOption[] value();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(StringOptions.class)
    @interface StringOption {
        String optionName();
        String defaultValue();

        String[] listDefaults() default {};

        String[] suggestions() default {};

        String[] aliases() default {};

        boolean saveToDefaults() default true;
    }
}
