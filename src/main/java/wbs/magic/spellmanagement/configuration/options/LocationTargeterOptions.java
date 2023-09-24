package wbs.magic.spellmanagement.configuration.options;

import wbs.magic.targeters.location.LocationTargeter;
import wbs.magic.targeters.location.SightLocationTargeter;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LocationTargeterOptions {
    LocationTargeterOption[] value();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(LocationTargeterOptions.class)
    @interface LocationTargeterOption {
        Class<? extends LocationTargeter> defaultType() default SightLocationTargeter.class;
        double defaultRange() default 10;
        int defaultCount() default 1;
        String optionName();

        boolean saveToDefaults() default true;

        String[] aliases() default {};
    }
}
