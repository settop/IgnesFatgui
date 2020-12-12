package settop.IgnesFatui.Client.Screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import settop.IgnesFatui.GUI.Network.Packets.ContainerTabSelected;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.GUI.BasicWispContainer;
import settop.IgnesFatui.Wisps.BasicWispContents;
import settop.IgnesFatui.Wisps.Enhancements.EnhancementTypes;

import java.awt.*;
import java.util.ArrayList;

public class BasicWispContainerScreen extends MultiScreen<BasicWispContainer> implements Button.IPressable
{
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(IgnesFatui.MOD_ID, "textures/gui/mbe30_inventory_basic_bg.png");
    private ArrayList<Button> tabs;
    private BasicWispContents wispContents;

    public BasicWispContainerScreen(BasicWispContainer container, PlayerInventory playerInventory, ITextComponent title)
    {
        super(container, playerInventory, title);
        wispContents = container.GetWispContents();

        // Set the width and height of the gui.  Should match the size of the texture!
        xSize = 176;
        ySize = 133;
    }

    @Override
    public void onPress(Button pressedButton)
    {
        for(int i = 0; i < tabs.size(); ++i)
        {
            if(tabs.get(i) == pressedButton)
            {
                IgnesFatui.MULTI_SCREEN_CHANNEL.sendToServer( new ContainerTabSelected( this.container.windowId, i ));
                break;
            }
        }
    }

    //render

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        UpdateButtons();

        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY)
    {
        final float LABEL_XPOS = 5;
        final float FONT_Y_SPACING = 12;
        final float CHEST_LABEL_YPOS = BasicWispContainer.WISP_SLOT_YPOS - FONT_Y_SPACING;
        font.func_243248_b(matrixStack, title,
                LABEL_XPOS, CHEST_LABEL_YPOS, Color.darkGray.getRGB());  //this.font.drawString;

        final float PLAYER_INV_LABEL_YPOS = BasicWispContainer.PLAYER_INVENTORY_YPOS - FONT_Y_SPACING;
        font.func_243248_b(matrixStack, playerInventory.getDisplayName(),                  ///    this.font.drawString
                LABEL_XPOS, PLAYER_INV_LABEL_YPOS, Color.darkGray.getRGB());
    }


    //drawGuiContainerBackgroundLayer
    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(BACKGROUND_TEXTURE);   // this.minecraft.getTextureManager()

        // width and height are the size provided to the window when initialised after creation.
        // xSize, ySize are the expected size of the texture-? usually seems to be left as a default.
        // The code below is typical for vanilla containers, so I've just copied that- it appears to centre the texture within
        //  the available window
        int edgeSpacingX = (this.width - this.xSize) / 2;
        int edgeSpacingY = (this.height - this.ySize) / 2;
        this.blit(matrixStack, edgeSpacingX, edgeSpacingY, 0, 0, this.xSize, this.ySize);
    }

    //Setup GUI
    @Override
    protected void init()
    {
        super.init();

        tabs = new ArrayList<>();

        tabs.add(addButton( new Button(0, 0, 32, 20, new TranslationTextComponent(""), this,
                (Button button, MatrixStack matrix, int mouseX, int mouseY)->this.renderTooltip(matrix, new TranslationTextComponent("sif1.wisp_contents"), mouseX, mouseY)) ));
        for(int i = 0; i < EnhancementTypes.NUM; ++i)
        {
            final int j = i;
            tabs.add(addButton( new Button(0, 0, 32, 20, new TranslationTextComponent(""), this,
                    (Button button, MatrixStack matrix, int mouseX, int mouseY)->this.renderTooltip(matrix, new TranslationTextComponent(EnhancementTypes.values()[j].GetName()), mouseX, mouseY))));
        }
        UpdateButtons();
    }

    private void UpdateButtons()
    {
        int xPos = guiLeft;
        for(int i = 0; i < tabs.size(); ++i)
        {
            Button button = tabs.get(i);
            if(container.IsTabActive(i))
            {
                button.visible = true;
                button.active = !container.IsTabSelected(i);

                button.x = xPos;
                button.y = guiTop - button.getHeightRealms();

                xPos += button.getWidth();
            }
            else
            {
                button.visible = false;
                button.active = false;
            }
        }
    }

}
