package wbs.magic.annotations;

import org.bukkit.entity.EntityType;
import wbs.magic.enums.SpellOptionType;

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
    Class<? extends Enum> enumType() default Enum.class;
}
