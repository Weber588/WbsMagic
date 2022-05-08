package wbs.magic.controls.conditions;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import wbs.magic.controls.CastTrigger;
import wbs.magic.controls.EventDetails;
import wbs.magic.exceptions.EventNotSupportedException;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;

import java.util.List;

public class BlockIsMaterialCondition extends CastCondition {

    private final Material material;

    public BlockIsMaterialCondition(List<String> args, String directory) {
        super(args, directory);

        if (args.size() == 0) {
            throw new InvalidConfigurationException("Material missing." + getUsage());
        }
        String materialString = args.get(0);
        material = WbsEnums.getEnumFromString(Material.class, materialString);
        if (material == null) {
            throw new InvalidConfigurationException("Invalid material: " + materialString + "." + getUsage());
        }
    }

    @Override
    public boolean checkInternal(EventDetails details) throws EventNotSupportedException {
        if (details.getBlock() == null) {
            throw new EventNotSupportedException();
        }

        return details.getBlock().getType() == material;
    }

    @Override
    public String getUsage() {
        return "Usage: MATERIAL <material>";
    }

    @Override
    public String formatTriggerString(CastTrigger trigger, String triggerString) {
        for (Class<? extends Event> event : trigger.getControl().getEvents()) {
            if (BlockBreakEvent.class.isAssignableFrom(event)) {
                return triggerString.replace("Block", WbsEnums.toPrettyString(material));
            } else if (BlockPlaceEvent.class.isAssignableFrom(event)) {
                return triggerString.replace("Block", WbsEnums.toPrettyString(material));
            }
        }

        return triggerString;
    }
}
