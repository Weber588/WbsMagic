package wbs.magic.wand;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ConfiguredAttribute {

    public final Attribute attribute;
    public final AttributeModifier modifier;

    public ConfiguredAttribute(@Nullable EquipmentSlot slot, @NotNull Attribute attribute, double amount) {
        this.attribute = attribute;

        modifier = new AttributeModifier(UUID.randomUUID(), attribute.name(), amount, AttributeModifier.Operation.MULTIPLY_SCALAR_1, slot);
    }
}
