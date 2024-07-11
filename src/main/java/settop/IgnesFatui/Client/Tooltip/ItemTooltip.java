package settop.IgnesFatui.Client.Tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record ItemTooltip(ItemStack stack) implements TooltipComponent
{

}
