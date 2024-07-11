package settop.IgnesFatui.Client.Tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ClientItemTooltip implements ClientTooltipComponent
{
    private final ItemStack itemToRender;

    public ClientItemTooltip(ItemTooltip tooltip)
    {
        itemToRender = tooltip.stack();
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics graphics)
    {
        graphics.renderItem(itemToRender, x + 1, y + 1, 0);
        graphics.renderItemDecorations(font, itemToRender, x + 1, y + 1);
    }

    @Override
    public int getHeight()
    {
        return 20;
    }

    @Override
    public int getWidth(@NotNull Font font)
    {
        return 20;
    }
}
