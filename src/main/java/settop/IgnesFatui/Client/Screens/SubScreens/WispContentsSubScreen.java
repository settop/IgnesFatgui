package settop.IgnesFatui.Client.Screens.SubScreens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.sun.org.apache.xpath.internal.operations.Mult;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.Slot;
import settop.IgnesFatui.Client.Screens.MultiScreen;
import settop.IgnesFatui.GUI.BasicWispContainer;
import settop.IgnesFatui.GUI.SubContainers.SubContainer;

import java.awt.*;

public class WispContentsSubScreen extends SubScreen
{
    public WispContentsSubScreen(SubContainer container, MultiScreen<?> parentScreen)
    {
        super(container, parentScreen);
    }
    @Override
    public void init(int guiLeft, int guiTop)
    {
        super.init(guiLeft, guiTop);
    }

    @Override
    public void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        Slot baseSlot = GetSubContainer().inventorySlots.get(0);
        MultiScreen.RenderSlotRowBackground(this,matrixStack, guiLeft + baseSlot.xPos, guiTop + baseSlot.yPos, getBlitOffset(), GetSubContainer().inventorySlots.size());
    }

    @Override
    public void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY)
    {
        final float LABEL_XPOS = 5;
        final float LABEL_YPOS = 4;

        Minecraft.getInstance().fontRenderer.func_243248_b(matrixStack, GetParentScreen().getTitle(),
                LABEL_XPOS, LABEL_YPOS, Color.darkGray.getRGB());  //this.font.drawString;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
    }
}
