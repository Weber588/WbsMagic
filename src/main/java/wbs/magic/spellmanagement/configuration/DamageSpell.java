package wbs.magic.spellmanagement.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DamageSpell {
    String deathFormat() default "%victim% was killed by %attacker% using magic!";

    double defaultDamage();
    boolean suicidePossible() default false;
    String suicideFormat() default "";

    String[] damageTypes() default {};
}
