package wbs.magic.annotations;

import wbs.utils.util.WbsSoundGroup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpellSettings {

    /** If the spell is a continuous cast spell, shift is automatically required **/
    boolean isContinuousCast() default false;
    boolean canBeConcentration() default false;

    boolean concentrationByDefault() default true;

}
