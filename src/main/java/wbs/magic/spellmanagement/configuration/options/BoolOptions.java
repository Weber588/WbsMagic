package wbs.magic.spellmanagement.configuration.options;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BoolOptions {
    BoolOption[] value();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(BoolOptions.class)
    @interface BoolOption {
        String optionName();
        boolean defaultValue();

        boolean[] listDefaults() default {};

        String[] aliases() default {};

        boolean saveToDefaults() default true;
    }
}
