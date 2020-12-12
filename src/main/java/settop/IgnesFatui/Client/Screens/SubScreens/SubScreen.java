package settop.IgnesFatui.Client.Screens.SubScreens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.Widget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import settop.IgnesFatui.GUI.SubContainers.SubContainer;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class SubScreen
{
    private SubContainer container;
    private List<Widget> widgets;

    protected SubScreen(SubContainer container)
    {
        this.container = container;
        widgets = new ArrayList<>();
    }

    public abstract void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);
    public void init(int guiLeft, int guiTop){}

    public SubContainer GetSubContainer() { return container; }

    protected <T extends Widget> T AddWidget(T button)
    {
        widgets.add(button);
        return button;
    }

    public List<Widget> GetWidgets() { return widgets; }
}
