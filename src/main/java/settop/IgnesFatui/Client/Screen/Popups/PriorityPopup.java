package settop.IgnesFatui.Client.Screen.Popups;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.Client.Screen.Widgets.NumberSpinner;
import settop.IgnesFatui.Client.Screen.WispContainerScreenBase;

public abstract class PriorityPopup extends ScreenPopup
{
    public class PrioritySetter extends NumberSpinner
    {
        public PrioritySetter(Font font, int x, int y, int width, int height)
        {
            super(font, true, x, y, width, height, Integer.MIN_VALUE + 1, Integer.MAX_VALUE);
        }

        @Override
        public void ValueChanged(int value)
        {
            PriorityChanged(value);
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        }
    }

    public static final int Width = 100;
    public static final int Height = 32;

    private final PrioritySetter prioritySetter;

    public PriorityPopup(int x, int y)
    {
        super(x, y, Width, Height);

        prioritySetter = new PrioritySetter(Minecraft.getInstance().font, x + 2, y + 18, Width - 4, 10);
        AddListener(prioritySetter);
    }

    public void SetValue(int value)
    {
        prioritySetter.SetValue(value);
    }

    public abstract void PriorityChanged(int value);

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, WispContainerScreenBase.BG_COLOUR, WispContainerScreenBase.BG_COLOUR);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, getTabOrderGroup() + 1);
        prioritySetter.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.pose().popPose();
        WispContainerScreenBase.RenderBorder(this, guiGraphics, getX(), getY(), getTabOrderGroup(), width, height);
    }

    @Override
    public @NotNull NarrationPriority narrationPriority()
    {
        return NarrationPriority.FOCUSED;
    }
}
