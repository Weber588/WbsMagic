package wbs.magic.objects;

import wbs.magic.spells.ranged.projectile.EldritchBlast;
import wbs.magic.spells.ranged.targeted.*;
import wbs.magic.spells.ranged.targeted.missile.MagicMissiles;

/**
 * Represents how good or bad an effect or spell is.<br/>
 * Most effects should fall between {@link AlignmentType#GOOD} to {@link AlignmentType#BAD},
 * reserving {@link AlignmentType#DIVINE} and {@link AlignmentType#EVIL} for stylistic effects
 * and thematic uses (such as dispelling all {@link AlignmentType#EVIL} effects).<br/>
 * <br/>
 * Examples:
 * <ul>
 *     <li>{@link DivineShield} is {@link AlignmentType#DIVINE} (because it is purely protective, and has
 *     potential to protect against arbitrarily high damage)
 *     <li>{@link FortifyVitality} is {@link AlignmentType#GOOD} (because it can only remove debuffs)
 *     <li>{@link Push} is {@link AlignmentType#POSITIVE} (because it's used to repel, which is mostly defensive,
 *     but could theoretically be used to throw someone off a cliff)
 *     <li>{@link PlanarBinding} is {@link AlignmentType#NEUTRAL} (because it prevents escape, but also approach)
 *     <li>{@link DrainLife} is {@link AlignmentType#NEGATIVE} (because it is primarily damaging, but has a
 *     positive upside)
 *     <li>{@link EldritchBlast} is {@link AlignmentType#BAD} (because it can only cause damage)
 *     <li>{@link MagicMissiles} is {@link AlignmentType#EVIL} (because it can only cause damage, and is extremely
 *     difficult to avoid)
 * </ul>
 */
public enum AlignmentType {
    /**
     * Divine effects, typically relating to absolute protection, or powerful healing
     */
    DIVINE(3),
    /**
     * Effects that are exclusively positive, such as healing spells, defensive spells,
     * and damaging spells that only damage undead mobs
     */
    GOOD(2),
    /**
     * Effects that are generally good, but not healing or particularly powerful
     */
    POSITIVE(1),
    /**
     * Effects that are neither good nor bad, or, that may be good or bad depending on context
     */
    NEUTRAL(0),
    /**
     * Effects that are damaging or offensive, but are either weak (like a slowness spell) or have a minor upside
     * (such as healing + damage)
     */
    NEGATIVE(-1),
    /**
     * Effects that can only be negative, such as a purely damaging spell or powerful debuff spells
     */
    BAD(-2),
    /**
     * Effects that can only be negative and are generally powerful or otherwise thematically evil
     */
    EVIL(-3),
    ;

    private final int level;

    AlignmentType(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Names of the {@link AlignmentType} names for use in annotations.
     */
    public static class Name {
        /**
         * Divine effects, typically relating to absolute protection, or powerful healing
         */
        public static final String DIVINE = "DIVINE";
        /**
         * Effects that are exclusively positive, such as healing spells, defensive spells,
         * and damaging spells that only damage undead mobs
         */
        public static final String GOOD = "GOOD";
        /**
         * Effects that are generally good, but not healing or particularly powerful
         */
        public static final String POSITIVE = "POSITIVE";
        /**
         * Effects that are neither good nor bad, or, that may be good or bad depending on context
         */
        public static final String NEUTRAL = "NEUTRAL";
        /**
         * Effects that are damaging or offensive, but are either weak (like a slowness spell) or have a minor upside
         * (such as healing + damage)
         */
        public static final String NEGATIVE = "NEGATIVE";
        /**
         * Effects that can only be negative, such as a purely damaging spell or powerful debuff spells
         */
        public static final String BAD = "BAD";
        /**
         * Effects that can only be negative and are generally powerful or otherwise thematically evil
         */
        public static final String EVIL = "EVIL";
    }
}
