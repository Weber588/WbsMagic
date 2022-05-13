package wbs.magic.controls;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EventDetails {
    @NotNull
    public final Event event;
    @NotNull
    public final Player player;
    @Nullable
    private Block block;
    @Nullable
    private Entity otherEntity;

    public EventDetails(@NotNull Event event, @NotNull Player player) {
        this.event = event;
        this.player = player;
    }

    @Nullable
    public Entity getOtherEntity() {
        return otherEntity;
    }

    public void setOtherEntity(@Nullable Entity otherEntity) {
        this.otherEntity = otherEntity;
    }

    @Nullable
    public Block getBlock() {
        return block;
    }

    public void setBlock(@Nullable Block block) {
        this.block = block;
    }
}
