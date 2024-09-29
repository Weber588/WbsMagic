package wbs.magic.spells.ranged;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import wbs.magic.SpellCaster;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.options.DoubleOptions.DoubleOption;
import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.particles.CuboidParticleEffect;

import java.util.*;

@Spell(name = "Discover Item", description = "Locate an item type in nearby containers!")

@DoubleOption(optionName = "range", defaultValue = 15)
public class DiscoverItem extends RangedSpell {
    private static final DiscoverItemListener LISTENER = new DiscoverItemListener();

    public DiscoverItem(SpellConfig config, String directory) {
        super(config, directory);

        tryRegisterListener(LISTENER);

        highlight = new CuboidParticleEffect();
        highlight.setScaleAmount(true);
        highlight.setAmount(4);
    }

    private final CuboidParticleEffect highlight;

    @Override
    public boolean cast(CastingContext context) {
        SpellCaster caster = context.caster;
        MagicEntityEffect effect = new MagicEntityEffect(caster.getPlayer(), caster, this);

        effect.setMaxAge(300);
        effect.setExpireMessage("You didn't click an item in time!");

        effect.run();

        caster.sendActionBar("Click an item in an inventory to find it in nearby containers!");

        return true;
    }

    private void discover(Player player, Material material) {
        World world = player.getWorld();

        Location location = player.getLocation();
        int centerX = location.getBlockX();
        int centerY = location.getBlockY();
        int centerZ = location.getBlockZ();

        int radius = (int) Math.ceil(range);
        int radiusSquared = radius * radius;

        Set<Container> containers = new HashSet<>();

        for (int x = -radius; x < radius; x++) {
            for (int y = -radius; y < radius; y++) {
                for (int z = -radius; z < radius; z++) {
                    if (x * x + y * y + z * z < radiusSquared) {
                        int currentX = x + centerX;
                        int currentY = y + centerY;
                        int currentZ = z + centerZ;

                        Block block = world.getBlockAt(currentX, currentY, currentZ);

                        BlockState state = block.getState();
                        if (state instanceof Container) {
                            containers.add((Container) state);
                        }
                    }
                }
            }
        }

        if (containers.isEmpty()) {
            plugin.sendActionBar("&wNo containers in range!", player);
            return;
        }

        Map<Inventory, Container> holdersFound = new HashMap<>();

        int found = 0;
        for (Container container : containers) {
            Inventory inv = container.getInventory();

            if (inv.contains(material)) {
                holdersFound.put(inv, container);
            }
        }

        for (Inventory inv : holdersFound.keySet()) {
            InventoryHolder holder = inv.getHolder();
            Location loc1, loc2;
            if (holder instanceof DoubleChest) {
                DoubleChest doubleChest = (DoubleChest) holder;

                Chest left = (Chest) doubleChest.getLeftSide();
                Chest right = (Chest) doubleChest.getRightSide();

                loc1 = Objects.requireNonNull(left).getLocation();
                loc2 = Objects.requireNonNull(right).getLocation();
            } else {
                loc1 = holdersFound.get(inv).getLocation();
                loc2 = loc1;
            }

            Location center = highlight.configureBlockOutline(loc1, loc2);
            highlight.play(Particle.HAPPY_VILLAGER, center);
            found++;
        }

        if (found > 0) {
            plugin.sendActionBar("Discovered &h" + WbsEnums.toPrettyString(material) +
                    "&r in &h" + found + "&r containers!", player);
        } else {
            plugin.sendActionBar("&wNo " + WbsEnums.toPrettyString(material) + " found!", player);
        }
    }

    @SuppressWarnings("unused")
    private static class DiscoverItemListener implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onSelectItem(InventoryClickEvent event) {
            HumanEntity player = event.getWhoClicked();

            int slot = event.getSlot();
            Inventory clicked = event.getClickedInventory();
            if (clicked == null) {
                return;
            }
            ItemStack item = clicked.getItem(slot);
            if (item != null && item.getType().isItem()) {
                MagicEntityEffect effect = MagicEntityEffect.getEffectBySpell(player, DiscoverItem.class);
                if (effect != null) {
                    effect.remove(true);

                    DiscoverItem spell = (DiscoverItem) effect.getSpell();
                    event.setCancelled(true);
                    player.closeInventory();

                    spell.discover((Player) player, item.getType());
                }
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onRightClickItem(PlayerInteractEvent event) {
            Player player = event.getPlayer();

            ItemStack item = event.getItem();

            if (item != null) {
                Block targetBlock = event.getClickedBlock();
                if (targetBlock != null && targetBlock.getState() instanceof Container) {
                    return;
                }

                MagicEntityEffect effect = MagicEntityEffect.getEffectBySpell(player, DiscoverItem.class);
                if (effect != null) {
                    effect.remove(true);

                    DiscoverItem spell = (DiscoverItem) effect.getSpell();
                    event.setCancelled(true);

                    spell.discover(player, item.getType());
                }
            }
        }
    }
}
