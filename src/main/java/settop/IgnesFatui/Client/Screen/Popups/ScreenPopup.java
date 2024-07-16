package settop.IgnesFatui.Client.Screen.Popups;


import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.Widget;

import java.util.ArrayList;
import java.util.List;

public abstract class ScreenPopup extends AbstractContainerWidget implements Widget
{
    private final ArrayList<GuiEventListener> guiEventListeners;

    public ScreenPopup(int x, int y, int width, int height)
    {
        super(x, y, width, height, Component.empty());

        guiEventListeners = new ArrayList<>();
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children()
    {
        return guiEventListeners;
    }

    public <T extends GuiEventListener> T AddListener(T listener)
    {
        guiEventListeners.add(listener);
        return listener;
    }

    //public abstract void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks);

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return getX() < mouseX && mouseX < getX() + width &&
                getY() < mouseY && mouseY < getY() + height;
    }

    public void OnOpen() {}
    public void OnClose() {}
}
