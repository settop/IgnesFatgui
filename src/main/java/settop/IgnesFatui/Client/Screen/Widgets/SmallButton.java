package settop.IgnesFatui.Client.Screen.Widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.Client.Screen.WispContainerScreenBase;

public abstract class SmallButton extends Button
{
    private final float scale;

    public SmallButton(int x, int y, int size, Component title, Button.OnPress pressedAction)
    {
        super(x, y, size, size, title, pressedAction, Button.DEFAULT_NARRATION);
        scale = size / 32.f;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(getX(), getY(), 0.f);
        guiGraphics.pose().scale(scale, scale, 1.f);

        Minecraft minecraft = Minecraft.getInstance();
        //RenderSystem.setShaderTexture(0, WispContainerScreenBase.GUI_PARTS_TEXTURE);

        WispContainerScreenBase.GuiPart buttonPart = isHoveredOrFocused() ? WispContainerScreenBase.BUTTON_HOVERED : WispContainerScreenBase.BUTTON;

        guiGraphics.blit(WispContainerScreenBase.GUI_PARTS_TEXTURE, 0, 0, buttonPart.uStart, buttonPart.vStart, buttonPart.width, buttonPart.height);
        RenderOverlay(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.pose().popPose();

        if (this.isHoveredOrFocused())
        {
            //this.renderToolTip(matrixStack, mouseX, mouseY);
        }
    }

    public abstract void RenderOverlay(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks);
}
