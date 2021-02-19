package settop.IgnesFatui.Client.Screens.Popups;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FocusableGui;
import net.minecraft.client.gui.IGuiEventListener;

import java.util.ArrayList;
import java.util.List;

public abstract class ScreenPopup extends FocusableGui
{
    public final int x, y;
    public final int width, height;
    private ArrayList<IGuiEventListener> guiEventListeners;

    public ScreenPopup(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        guiEventListeners = new ArrayList<>();
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners()
    {
        return guiEventListeners;
    }


    public <T extends IGuiEventListener> T AddListener(T listener)
    {
        guiEventListeners.add(listener);
        return listener;
    }

    public abstract void Render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return x < mouseX && mouseX < x + width &&
                y < mouseY && mouseY < y + height;
    }

    public void OnOpen() {}
    public void OnClose() {}
}
