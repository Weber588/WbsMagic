package wbs.magic.spellmanagement.configuration.options;

import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.LineOfSightTargeter;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TargeterOptions {
    TargeterOption[] value();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(TargeterOptions.class)
    @interface TargeterOption {
        Class<? extends GenericTargeter> defaultType() default LineOfSightTargeter.class;
        double defaultRange() default 10;
        String optionName();

        boolean saveToDefaults() default true;

        String[] aliases() default {};
    }
}
