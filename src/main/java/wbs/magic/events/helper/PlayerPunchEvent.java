package wbs.magic.events.helper;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerPunchEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Event event;
    private final @Nullable Block block;
    private final @Nullable Entity otherEntity;

    public PlayerPunchEvent(@NotNull Event event, @NotNull Player player, @Nullable Block block, @Nullable Entity otherEntity) {
        super(player);
        this.event = event;
        this.block = block;
        this.otherEntity = otherEntity;
    }

    public Action getAction() {
        if (event instanceof PlayerInteractEvent) {
            return ((PlayerInteractEvent) event).getAction();
        }

        if (block != null) {
            return Action.LEFT_CLICK_BLOCK;
        } else {
            return Action.LEFT_CLICK_AIR;
        }
    }

    public void autoCall() {
        Bukkit.getPluginManager().callEvent(this);

        if (event instanceof Cancellable) {
            if (isCancelled()) ((Cancellable) event).setCancelled(true);
        }
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    private boolean isCancelled = false;

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

    public Block getBlock() {
        return block;
    }

    public Entity getOtherEntity() {
        return otherEntity;
    }
}
