package wbs.magic.targeters;

public enum TargeterType {
	LINE_OF_SIGHT, NEAREST, SELF, RANDOM, RADIUS;
	
	private String failMessage;
	private double range;
	
	
	static {
		LINE_OF_SIGHT.failMessage = "You need line of sight with a mob!";
		NEAREST.failMessage = "There was no mob in range!";
		SELF.failMessage = "The spell wiffed!"; // Shouldn't come up; self is always a valid target
		RANDOM.failMessage = "There was no mob in range!";
		RADIUS.failMessage = "There was no mob in range!";
		
		
		LINE_OF_SIGHT.range = 150;
		NEAREST.range = 10;
		SELF.range = 0;
		RANDOM.range = 10;
		RADIUS.range = 5;
	}
	
	public String getFailMessage() {
		return failMessage;
	}
	
	public double getRange() {
		return range;
	}
}
