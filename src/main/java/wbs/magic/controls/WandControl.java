package wbs.magic.controls;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import wbs.magic.events.helper.PlayerPunchEvent;
import wbs.magic.events.helper.PlayerRightClickEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents one or more events that can be used in spell triggers
 */
public enum WandControl {

    PUNCH(PlayerPunchEvent.class),
    RIGHT_CLICK(PlayerRightClickEvent.class),

    DROP(PlayerDropItemEvent.class),
    BREAK_BLOCK(BlockBreakEvent.class),
    PLACE_BLOCK(BlockPlaceEvent.class),
    SNEAK(PlayerToggleSneakEvent.class),
    DEATH(PlayerDeathEvent.class),
    EAT(PlayerItemConsumeEvent.class),
    CATCH_FISH(PlayerFishEvent.class), // Check event.getState() == PlayerFishEvent.State.CAUGHT_FISH
    RIPTIDE(PlayerRiptideEvent.class),
    ITEM_BREAK(PlayerItemBreakEvent.class), // Maybe fire this event when wands are using durability too?
    START_SPRINTING(PlayerToggleSprintEvent.class)
    ;

    private final Set<Class<? extends Event>> events = new HashSet<>();

    @SuppressWarnings("unchecked")
    WandControl(Class<?> ... events) {
        for (Class<?> event : events) {
            this.events.add((Class<? extends Event>) event);
        }
    }

    public Set<Class<? extends Event>> getEvents() {
        return events;
    }
}
