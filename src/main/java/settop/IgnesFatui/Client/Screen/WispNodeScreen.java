package settop.IgnesFatui.Client.Screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.BlockEntities.WispNodeBlockEntity;
import settop.IgnesFatui.Menu.WispNodeMenu;

import java.util.List;

public class WispNodeScreen extends AbstractContainerScreen<WispNodeMenu> implements MenuAccess<WispNodeMenu>
{
    private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("textures/gui/container/generic_54.png");

    public WispNodeScreen(WispNodeMenu menu, Inventory inv, Component title)
    {
        super(menu, inv, title);
        this.imageHeight = 114 + 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    public void render(@NotNull GuiGraphics guiGraphics, int x, int y, float partialTick)
    {
        super.render(guiGraphics, x, y, partialTick);
        //this.renderTooltip(guiGraphics, x, y);

        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem())
        {
            ItemStack itemstack = this.hoveredSlot.getItem();
            List<Component> tooltips = getTooltipFromContainerItem(itemstack);

            int cannotConnectReason = getMenu().GetCannotConnectReasonForSlot(this.hoveredSlot.index);
            WispNodeBlockEntity.CannotConnectReason[] reasons = WispNodeBlockEntity.CannotConnectReason.values();
            for(int i = 0; i < reasons.length; ++i)
            {
                if((cannotConnectReason & reasons[i].BitField()) != 0)
                {
                    String reason = WispNodeBlockEntity.CannotConnectReason.values()[i].name().toLowerCase();
                    tooltips.add(Component.translatable("menu.sif1.cannot_connect_reason.%s", reason));
                }
            }

            guiGraphics.renderTooltip(this.font, tooltips, itemstack.getTooltipImage(), itemstack, x, y);
        }
    }

    protected void renderBg(@NotNull GuiGraphics guiGraphics, float p_282334_, int p_282603_, int p_282158_)
    {
        int $$4 = (this.width - this.imageWidth) / 2;
        int $$5 = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(CONTAINER_BACKGROUND, $$4, $$5, 0, 0, this.imageWidth, 18 + 17);
        guiGraphics.blit(CONTAINER_BACKGROUND, $$4, $$5 + 18 + 17, 0, 126, this.imageWidth, 96);
    }
}
