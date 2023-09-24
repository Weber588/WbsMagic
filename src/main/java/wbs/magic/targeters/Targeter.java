package wbs.magic.targeters;

public abstract class Targeter {

    protected double range = 5;

    public void setRange(double range) {
        this.range = range;
    }
    /**
     * Gets the range for the targeter.
     * @return The range
     */
    public double getRange() {
        return range;
    }
}
