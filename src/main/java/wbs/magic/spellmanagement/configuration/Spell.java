package wbs.magic.spellmanagement.configuration;

import wbs.magic.objects.AlignmentType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Spell {
    String name();
    String description();
    String alignment() default AlignmentType.Name.NEUTRAL;

    int cost() default 10;
    double cooldown() default 10;
}
