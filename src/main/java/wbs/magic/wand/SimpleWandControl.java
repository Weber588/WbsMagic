package wbs.magic.wand;

import org.jetbrains.annotations.NotNull;
import wbs.magic.controls.CastTrigger;
import wbs.magic.controls.WandControl;
import wbs.magic.controls.conditions.HasEntityCondition;
import wbs.magic.controls.conditions.PitchCondition;
import wbs.magic.controls.conditions.SneakCondition;

import java.util.*;

public enum SimpleWandControl {
	PUNCH, SHIFT_PUNCH,
	PUNCH_DOWN, SHIFT_PUNCH_DOWN,
	PUNCH_ENTITY, SHIFT_PUNCH_ENTITY,

	RIGHT_CLICK, SHIFT_RIGHT_CLICK,
	RIGHT_CLICK_DOWN, SHIFT_RIGHT_CLICK_DOWN,
	RIGHT_CLICK_ENTITY, SHIFT_RIGHT_CLICK_ENTITY,

	SHIFT_DROP,
	SHIFT_DROP_DOWN;

	@NotNull
	public CastTrigger toTrigger(String directory) {
		CastTrigger trigger = null;
		switch (this) {
			case PUNCH:
				trigger = new CastTrigger(WandControl.PUNCH);
				break;
			case SHIFT_PUNCH:
				trigger = new CastTrigger(WandControl.PUNCH);
				trigger.setPriority(Integer.MAX_VALUE - 1);
				trigger.addCondition(new SneakCondition(Collections.emptyList(), directory));
				break;
			case PUNCH_DOWN:
				trigger = new CastTrigger(WandControl.PUNCH);
				trigger.setPriority(Integer.MAX_VALUE - 1);
				trigger.addCondition(new PitchCondition(Arrays.asList(">", "85"), directory));
				break;
			case SHIFT_PUNCH_DOWN:
				trigger = new CastTrigger(WandControl.PUNCH);
				trigger.setPriority(Integer.MAX_VALUE - 2);
				trigger.addCondition(new SneakCondition(Collections.emptyList(), directory));
				trigger.addCondition(new PitchCondition(Arrays.asList(">", "85"), directory));
				break;
			case PUNCH_ENTITY:
				trigger = new CastTrigger(WandControl.PUNCH);
				trigger.setPriority(Integer.MAX_VALUE - 1);
				trigger.addCondition(new HasEntityCondition(Collections.emptyList(), directory));
				break;
			case SHIFT_PUNCH_ENTITY:
				trigger = new CastTrigger(WandControl.PUNCH);
				trigger.setPriority(Integer.MAX_VALUE - 2);
				trigger.addCondition(new HasEntityCondition(Collections.emptyList(), directory));
				trigger.addCondition(new SneakCondition(Collections.emptyList(), directory));
				break;
			case RIGHT_CLICK:
				trigger = new CastTrigger(WandControl.RIGHT_CLICK);
				break;
			case SHIFT_RIGHT_CLICK:
				trigger = new CastTrigger(WandControl.RIGHT_CLICK);
				trigger.setPriority(Integer.MAX_VALUE - 1);
				trigger.addCondition(new SneakCondition(Collections.emptyList(), directory));
				break;
			case RIGHT_CLICK_DOWN:
				trigger = new CastTrigger(WandControl.RIGHT_CLICK);
				trigger.setPriority(Integer.MAX_VALUE - 1);
				trigger.addCondition(new PitchCondition(Arrays.asList(">", "85"), directory));
				break;
			case SHIFT_RIGHT_CLICK_DOWN:
				trigger = new CastTrigger(WandControl.RIGHT_CLICK);
				trigger.setPriority(Integer.MAX_VALUE - 2);
				trigger.addCondition(new SneakCondition(Collections.emptyList(), directory));
				trigger.addCondition(new PitchCondition(Arrays.asList(">", "85"), directory));
				break;
			case RIGHT_CLICK_ENTITY:
				trigger = new CastTrigger(WandControl.RIGHT_CLICK);
				trigger.setPriority(Integer.MAX_VALUE - 1);
				trigger.addCondition(new HasEntityCondition(Collections.emptyList(), directory));
				break;
			case SHIFT_RIGHT_CLICK_ENTITY:
				trigger = new CastTrigger(WandControl.RIGHT_CLICK);
				trigger.setPriority(Integer.MAX_VALUE - 2);
				trigger.addCondition(new HasEntityCondition(Collections.emptyList(), directory));
				trigger.addCondition(new SneakCondition(Collections.emptyList(), directory));
				break;
			case SHIFT_DROP:
				trigger = new CastTrigger(WandControl.DROP);
				trigger.setPriority(Integer.MAX_VALUE - 1);
				trigger.addCondition(new SneakCondition(Collections.emptyList(), directory));
				break;
			case SHIFT_DROP_DOWN:
				trigger = new CastTrigger(WandControl.DROP);
				trigger.setPriority(Integer.MAX_VALUE - 2);
				trigger.addCondition(new SneakCondition(Collections.emptyList(), directory));
				trigger.addCondition(new PitchCondition(Arrays.asList(">", "85"), directory));
				break;
		}

		if (trigger == null) {
			// TODO: Add NotImplementedException in WbsUtils
			throw new RuntimeException("A SimpleWandControl did not have it's cast trigger defined");
		}

		return trigger;
	}

	// Returns true if the control has a non shift/down version
	public boolean isCombined() {
		switch (this) {
			case PUNCH:
			case RIGHT_CLICK:
			case SHIFT_DROP:
				return false;
			default:
				return true;
		}
	}

	public boolean isShift() {
		switch (this) {
			case SHIFT_PUNCH:
			case SHIFT_PUNCH_ENTITY:
			case SHIFT_PUNCH_DOWN:
			case SHIFT_RIGHT_CLICK:
			case SHIFT_RIGHT_CLICK_ENTITY:
			case SHIFT_RIGHT_CLICK_DOWN:
			case SHIFT_DROP_DOWN:
			case SHIFT_DROP:
				return true;
			default:
				return false;
		}
	}

	public boolean isDown() {
		switch (this) {
			case PUNCH_DOWN:
			case SHIFT_PUNCH_DOWN:
			case RIGHT_CLICK_DOWN:
			case SHIFT_RIGHT_CLICK_DOWN:
			case SHIFT_DROP_DOWN:
				return true;
			default:
				return false;
		}
	}

	public boolean isDirectional() {
		return isDown(); // || isUp();
	}

	public boolean isEntity() {
		switch (this) {
			case PUNCH_ENTITY:
			case SHIFT_PUNCH_ENTITY:
			case RIGHT_CLICK_ENTITY:
			case SHIFT_RIGHT_CLICK_ENTITY:
				return true;
			default:
				return false;
		}
	}


	public SimpleWandControl shiftless() {
		switch (this) {
			case SHIFT_PUNCH:
				return PUNCH;
			case SHIFT_RIGHT_CLICK:
				return RIGHT_CLICK;
			case SHIFT_DROP:
				return this; // No drop version as that is used for tier changes
			case SHIFT_PUNCH_DOWN:
				return PUNCH_DOWN;
			case SHIFT_RIGHT_CLICK_DOWN:
				return RIGHT_CLICK_DOWN;
			case SHIFT_RIGHT_CLICK_ENTITY:
				return RIGHT_CLICK_ENTITY;
			case SHIFT_PUNCH_ENTITY:
				return PUNCH_ENTITY;
			default:
				return this;
		}
	}

	public SimpleWandControl directionless() {
		switch (this) {
			case PUNCH_DOWN:
				return PUNCH;
			case RIGHT_CLICK_DOWN:
				return RIGHT_CLICK;
			case SHIFT_DROP_DOWN:
				return SHIFT_DROP;
			case SHIFT_PUNCH_DOWN:
				return SHIFT_PUNCH;
			case SHIFT_RIGHT_CLICK_DOWN:
				return SHIFT_RIGHT_CLICK;
			default:
				return this;
		}
	}

	public SimpleWandControl nonEntity() {
		switch (this) {
			case RIGHT_CLICK_ENTITY:
				return RIGHT_CLICK;
			case SHIFT_RIGHT_CLICK_ENTITY:
				return SHIFT_RIGHT_CLICK;
			case PUNCH_ENTITY:
				return PUNCH;
			case SHIFT_PUNCH_ENTITY:
				return SHIFT_PUNCH;
			default:
				return this;
		}
	}

	public SimpleWandControl uncombined() {
		return directionless().shiftless().nonEntity();
	}

	/**
	 * Get a simplified version of this control,
	 * stripping modifiers in a predefined order:
	 * Direction, Entity, Shift.
	 * @return The simplified version.
	 */
	public SimpleWandControl getSimplified() {
		if (isDown()) {
			return directionless();
		} else if (isEntity()) {
			return nonEntity();
		} else if (isShift()) {
			return shiftless();
		}
		return uncombined(); // Technically should make no difference, but incase more are added.
	}

	public String getDescription() {
		String returnString;
		switch (this) {
			case PUNCH:
				returnString = "Punch the air, or a block.";
				break;
			case PUNCH_ENTITY:
				returnString = "Punch an entity.";
				break;
			case RIGHT_CLICK:
				returnString = "Right click the air or a block.";
				break;
			case RIGHT_CLICK_ENTITY:
				returnString = "Right click an entity.";
				break;
			case SHIFT_DROP:
				returnString = "While holding shift, drop your wand.";
				break;
			case SHIFT_PUNCH:
				returnString = "While holding shift, punch the air or a block.";
				break;
			case SHIFT_RIGHT_CLICK:
				returnString = "While holding shift, right click the air or a block.";
				break;
			case PUNCH_DOWN:
				returnString = "Punch the air or a block while looking straight down.";
				break;
			case RIGHT_CLICK_DOWN:
				returnString = "Right click the air or a block while looking straight down.";
				break;
			case SHIFT_DROP_DOWN:
				returnString = "While holding shift, drop your wand while looking straight down.";
				break;
			case SHIFT_PUNCH_DOWN:
				returnString = "While holding shift, punch the air or a block while looking straight down.";
				break;
			case SHIFT_RIGHT_CLICK_DOWN:
				returnString = "While holding shift, right click the air or a block while looking straight down.";
				break;
			case SHIFT_PUNCH_ENTITY:
				returnString = "While holding shift, punch an entity";
				break;
			case SHIFT_RIGHT_CLICK_ENTITY:
				returnString = "While holding shift, right click an entity";
				break;
			default:
				returnString = "This description has not yet been configured; please contact a server administrator.";

		}
		return returnString;
	}
}
