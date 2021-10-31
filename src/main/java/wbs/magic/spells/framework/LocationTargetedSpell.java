package wbs.magic.spells.framework;

public interface LocationTargetedSpell extends TargetedSpell {
    boolean castLocation(CastingContext context);
}
