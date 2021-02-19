package settop.IgnesFatui.Client.Screens.Widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import settop.IgnesFatui.Client.Screens.MultiScreen;

public abstract class SmallButton extends Button
{
    public SmallButton(int x, int y, ITextComponent title)
    {
        super(x, y, 8, 8, title, null);
    }

    public SmallButton(int x, int y, ITextComponent title, IPressable pressedAction)
    {
        super(x, y, 8, 8, title, pressedAction);
    }

    public SmallButton(int x, int y, ITextComponent title, IPressable pressedAction, ITooltip onTooltip)
    {
        super(x, y, 8, 8, title, pressedAction, onTooltip);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        matrixStack.push();
        //have to deal with the backwards way minecraft handles matrices :/
        //so need to translate first
        matrixStack.translate(this.x, this.y, 0.f);
        matrixStack.scale(0.25f, 0.25f, 1.f);

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bindTexture(MultiScreen.GUI_PARTS_TEXTURE);

        MultiScreen.GuiPart buttonPart = isHovered() ? MultiScreen.BUTTON_HOVERED : MultiScreen.BUTTON;

        blit(matrixStack, 0, 0, buttonPart.uStart, buttonPart.vStart, buttonPart.width, buttonPart.height );
        RenderOverlay(matrixStack, mouseX, mouseY, partialTicks);

        matrixStack.pop();

        if (this.isHovered())
        {
            this.renderToolTip(matrixStack, mouseX, mouseY);
        }
    }

    public abstract void RenderOverlay(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);
}
