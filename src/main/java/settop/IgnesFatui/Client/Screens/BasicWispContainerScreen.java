package settop.IgnesFatui.Client.Screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import settop.IgnesFatui.Wisps.BasicWispContainer;

public class BasicWispContainerScreen extends ContainerScreen<BasicWispContainer>
{
    public BasicWispContainerScreen(BasicWispContainer container, PlayerInventory playerInventory, ITextComponent title)
    {
        super(container, playerInventory, title);

        // Set the width and height of the gui.  Should match the size of the texture!
        xSize = 176;
        ySize = 133;
    }

    //drawGuiContainerBackgroundLayer
    @Override
    protected void func_230450_a_(MatrixStack p_230450_1_, float p_230450_2_, int p_230450_3_, int p_230450_4_)
    {

    }

}
