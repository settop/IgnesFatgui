package settop.IgnesFatui.Client.Screens.SubScreens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import settop.IgnesFatui.Client.Screens.MultiScreen;
import settop.IgnesFatui.GUI.SubContainers.SubContainer;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class SubScreen extends AbstractGui
{
    protected int guiLeft;
    protected int guiTop;

    private SubContainer container;
    private MultiScreen<?> parentScreen;
    private List<Widget> widgets;
    protected boolean active = false;

    protected SubScreen(SubContainer container, MultiScreen<?> parentScreen)
    {
        this.container = container;
        this.parentScreen = parentScreen;
        widgets = new ArrayList<>();
    }

    public void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {}
    public void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {}
    public abstract void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);
    public void init(int guiLeft, int guiTop)
    {
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
    }
    public void onClose() {}

    public SubContainer GetSubContainer() { return container; }
    public MultiScreen<?> GetParentScreen() { return parentScreen; }

    protected <T extends Widget> T AddWidget(T button)
    {
        widgets.add(button);
        return button;
    }

    public List<Widget> GetWidgets() { return widgets; }

    public void SetActive(boolean active)
    {
        if(this.active != active)
        {
            this.active = active;
            for(Widget widget : GetWidgets())
            {
                widget.active = active;
                widget.visible = active;
            }
        }
    }
}
