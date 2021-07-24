package wbs.magic.spellmanagement.configuration;

import org.bukkit.Sound;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SpellSounds.class)
public @interface SpellSound {
    Sound sound();
    float pitch() default 1;
    float volume() default 1;

}
