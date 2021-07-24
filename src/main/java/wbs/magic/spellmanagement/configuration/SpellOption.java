package wbs.magic.spellmanagement.configuration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SpellOptions.class)
public @interface SpellOption {

    String optionName();
    SpellOptionType type();

    String[] aliases() default {};

    int defaultInt() default 1;
    double defaultDouble() default 1;
    String defaultString() default "";
    boolean defaultBool() default false;
    String[] defaultStrings() default {};
    Class<? extends Enum> enumType() default Enum.class;
}
