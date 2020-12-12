package settop.IgnesFatui.Client.Screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import settop.IgnesFatui.Client.Screens.SubScreens.SubScreen;
import settop.IgnesFatui.GUI.MultiScreenContainer;
import settop.IgnesFatui.GUI.SubContainers.SubContainer;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiScreen<T extends MultiScreenContainer> extends ContainerScreen<T>
{
    private List<SubScreen> subScreens;

    public MultiScreen(T screenContainer, PlayerInventory inv, ITextComponent titleIn)
    {
        super(screenContainer, inv, titleIn);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        for(SubScreen subScreen : subScreens)
        {
            if(subScreen != null)
            {
                if(subScreen.GetSubContainer().IsActive())
                {
                    subScreen.render(matrixStack, mouseX, mouseY, partialTicks);
                    for(Widget widget : subScreen.GetWidgets())
                    {
                        widget.active = true;
                        widget.visible = true;
                    }
                }
                else
                {
                    for(Widget widget : subScreen.GetWidgets())
                    {
                        widget.active = false;
                        widget.visible = false;
                    }
                }
            }
        }
    }

    @Override
    protected void init()
    {
        super.init();

        subScreens = new ArrayList<>();
        for(SubContainer subContainer : container.GetSubContainers())
        {
            SubScreen screen = subContainer.CreateScreen();
            subScreens.add(screen);
            if(screen != null)
            {
                screen.init(guiLeft, guiTop);
                for (Widget widget : screen.GetWidgets())
                {
                    addButton(widget);
                }
            }
        }
    }
}
