package wbs.magic.spellmanagement.configuration.options;

import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.loot.Lootable;
import org.bukkit.material.Colorable;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityOptions {
    EntityOption[] value();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(EntityOptions.class)
    @interface EntityOption {
        String optionName();

        String entityType();

        Class<? extends Entity> classRestriction() default Entity.class;

        String[] aliases() default {};

        boolean saveToDefaults() default true;

        // ======================== All Entities =======================

        /**
         * For {@link Entity}
         */
        double fireDuration() default -1;

        // ====================== Living Entities ======================

        /**
         * For {@link LivingEntity}
         */
        double potionDuration() default 10;

        /**
         * For {@link LivingEntity}
         */
        String potionType() default "";

        /**
         * For {@link LivingEntity}
         */
        int potionAmplifier() default 1;

        /**
         * For {@link LivingEntity}
         */
        String holdingItem() default "";

        // ========================== Ageable ==========================

        /**
         * For {@link Ageable}
         */
        boolean baby() default false;

        // ==================== More niche options =====================

        /**
         * For {@link Lootable} and {@link FallingBlock}
         */
        boolean doDrops() default false;

        /**
         * For {@link Explosive}
         */
        float yield() default -1;

        /**
         * For {@link TNTPrimed} and {@link FireworkEffectMeta}
         */
        double fuseDuration() default 4;

        /**
         * For {@link Creeper} and {@link ThrownPotion}
         */
        boolean lingering() default false;

        /**
         * For {@link Creeper}
         */
        boolean charged() default false;

        /**
         * For {@link FallingBlock}
         */
        String material() default "oak_planks";

        /**
         * For {@link Colorable} and several others
         */
        String colour() default "";
    }
}
