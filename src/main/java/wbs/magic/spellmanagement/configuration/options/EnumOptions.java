package wbs.magic.spellmanagement.configuration.options;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumOptions {
    EnumOption[] value();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(EnumOptions.class)
    @interface EnumOption {
        String optionName();
        String defaultValue();

        @SuppressWarnings("rawtypes")
        Class<? extends Enum> enumType();

        String[] aliases() default {};

        boolean saveToDefaults() default true;
    }
}
