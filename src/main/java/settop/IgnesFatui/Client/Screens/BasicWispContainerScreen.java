package settop.IgnesFatui.Client.Screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import settop.IgnesFatui.GUI.Network.Packets.CContainerTabSelected;
import settop.IgnesFatui.GUI.SubContainers.PlayerInventorySubContainer;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.GUI.BasicWispContainer;
import settop.IgnesFatui.Wisps.BasicWispContents;
import settop.IgnesFatui.Wisps.Enhancements.EnhancementTypes;

import java.awt.*;
import java.util.ArrayList;

public class BasicWispContainerScreen extends MultiScreen<BasicWispContainer> implements Button.IPressable
{
    static class TabButton extends Button
    {
        public static final GuiPart ACTIVE_TAB = new GuiPart(64, 0, 32, 15);
        public static final GuiPart HOVERED_TAB = new GuiPart(96, 0, 32, 15);
        public static final GuiPart INACTIVE_TAB = new GuiPart(0, 0, 32, 16);

        public TabButton(int x, int y, int width, int height, ITextComponent title, IPressable pressedAction)
        {
            super(x, y, width, height, title, pressedAction);
        }
        public TabButton(int x, int y, int width, int height, ITextComponent title, IPressable pressedAction, ITooltip onTooltip)
        {
            super(x, y, width, height, title, pressedAction, onTooltip);
        }

        @Override
        public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
        {
            Minecraft.getInstance().getTextureManager().bindTexture(GUI_PARTS_TEXTURE);
            GuiPart renderedPart = active ? (isHovered() ? HOVERED_TAB : ACTIVE_TAB) : INACTIVE_TAB;

            this.blit(matrixStack, x, y, renderedPart.uStart, renderedPart.vStart, renderedPart.width, renderedPart.height);
            if(!active)
            {
                this.fillGradient(matrixStack, x + 3, y + renderedPart.height, x + renderedPart.width - 3, y + renderedPart.height + 2, BG_COLOUR, BG_COLOUR);
            }
            if (isHovered())
            {
                renderToolTip(matrixStack, mouseX, mouseY);
            }
        }
    }

    private ArrayList<Button> tabs;
    private BasicWispContents wispContents;

    public BasicWispContainerScreen(BasicWispContainer container, PlayerInventory playerInventory, ITextComponent title)
    {
        super(container, playerInventory, title);
        wispContents = container.GetWispContents();

        // Set the width and height of the gui.  Should match the size of the texture!
        xSize = 166;
        ySize = BasicWispContainer.PLAYER_INVENTORY_YPOS + PLAYER_INVENTORY.height;
    }

    @Override
    public void onPress(Button pressedButton)
    {
        for(int i = 0; i < tabs.size(); ++i)
        {
            if(tabs.get(i) == pressedButton)
            {
                IgnesFatui.MULTI_SCREEN_CHANNEL.sendToServer( new CContainerTabSelected( this.container.windowId, i ));
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

        final float PLAYER_INV_LABEL_YPOS = BasicWispContainer.PLAYER_INVENTORY_YPOS - FONT_Y_SPACING;
        font.func_243248_b(matrixStack, playerInventory.getDisplayName(),                  ///    this.font.drawString
                LABEL_XPOS, PLAYER_INV_LABEL_YPOS, Color.darkGray.getRGB());

        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
    }


    //drawGuiContainerBackgroundLayer
    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI_PARTS_TEXTURE);   // this.minecraft.getTextureManager()

        PlayerInventorySubContainer playerContainer = container.GetPlayerInventorySubContainer();

        this.fillGradient(matrixStack, guiLeft, guiTop, guiLeft + this.xSize, guiTop + this.ySize, BG_COLOUR, BG_COLOUR);
        RenderPlayerInv(matrixStack, playerContainer.GetXPos(), playerContainer.GetYPos());

        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);

        RenderBorder(this, matrixStack, guiLeft, guiTop, getBlitOffset(), this.xSize, this.ySize);
    }

    //Setup GUI
    @Override
    protected void init()
    {
        super.init();

        tabs = new ArrayList<>();

        tabs.add(addButton( new TabButton(0, 0, 32, 20, new TranslationTextComponent(""), this,
                (Button button, MatrixStack matrix, int mouseX, int mouseY)->this.renderTooltip(matrix, new TranslationTextComponent("sif1.wisp_contents"), mouseX, mouseY)) ));
        for(int i = 0; i < EnhancementTypes.NUM; ++i)
        {
            final int j = i;
            tabs.add(addButton( new TabButton(0, 0, 32, 20, new TranslationTextComponent(""), this,
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
