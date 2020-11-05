package wbs.magic.annotations;

import wbs.magic.enums.WandControl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestrictWandControls {

    /** When true, the control used must be a shift control
     */
    boolean requireShift() default false;
    /** When true, controls that force the player to look up/down will not be allowed
     */
    boolean dontRestrictLineOfSight() default false;

}
