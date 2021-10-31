package wbs.magic.spells.framework;

public interface TargetedSpell extends Castable {
    default boolean castWithoutTargets(CastingContext context) {
        return false;
    }
}
