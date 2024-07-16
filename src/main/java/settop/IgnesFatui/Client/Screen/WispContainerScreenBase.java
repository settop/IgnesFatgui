package settop.IgnesFatui.Client.Screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.Client.Screen.Popups.ScreenPopup;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Menu.WispContainerMenuBase;
import settop.IgnesFatui.Network.PacketHandler;

import java.util.ArrayList;

public abstract class WispContainerScreenBase<T extends WispContainerMenuBase> extends AbstractContainerScreen<T> implements MenuAccess<T>
{
    public static final ResourceLocation GUI_PARTS_TEXTURE = new ResourceLocation(IgnesFatui.MOD_ID, "textures/gui/gui_parts.png");
    public static class GuiPart
    {
        public GuiPart(int uStart, int vStart, int width, int height)
        {
            this.uStart = uStart;
            this.vStart = vStart;
            this.width = width;
            this.height = height;
        }
        public int uStart, width;
        public int vStart, height;
    }
    public static final int BG_COLOUR = 0xffc6c6c6;
    public static final GuiPart PLAYER_INVENTORY = new GuiPart(0, 32, 166, 81);
    public static final GuiPart INV_SLOT = new GuiPart(2, 35, 18, 18);
    public static final GuiPart INV_ROW = new GuiPart(2, 35, 162, 18);
    public static final GuiPart BUTTON = new GuiPart(0, 0, 32, 32);
    public static final GuiPart BUTTON_HOVERED = new GuiPart(32, 0, 32, 32);
    public static final GuiPart BUTTON_INACTIVE = new GuiPart(64, 0, 32, 32);
    public static final GuiPart BUTTON_INACTIVE_HOVERED = new GuiPart(96, 0, 32, 32);
    public static final GuiPart BORDER_TOP_LEFT = new GuiPart(0, 0, 5, 5);
    public static final GuiPart BORDER_TOP_RIGHT = new GuiPart(27, 0, 5, 5);
    public static final GuiPart BORDER_BOTTOM_LEFT = new GuiPart(0, 27, 5, 5);
    public static final GuiPart BORDER_BOTTOM_RIGHT = new GuiPart(27, 27, 5, 5);
    public static final GuiPart BORDER_TOP = new GuiPart(5, 0, 22, 5);
    public static final GuiPart BORDER_LEFT = new GuiPart(0, 5, 5, 22);
    public static final GuiPart BORDER_RIGHT = new GuiPart(27, 5, 5, 22);
    public static final GuiPart BORDER_BOTTOM = new GuiPart(5, 27, 22, 5);
    public static final GuiPart BACKGROUND = new GuiPart(5, 5, 24, 24);
    public static final GuiPart BUTTON_N = new GuiPart(0, 113, 20, 20);
    public static final GuiPart BUTTON_S = new GuiPart(0, 133, 20, 20);
    public static final GuiPart BUTTON_E = new GuiPart(20, 113, 20, 20);
    public static final GuiPart BUTTON_W = new GuiPart(20, 133, 20, 20);
    public static final GuiPart BUTTON_T = new GuiPart(40, 113, 20, 20);
    public static final GuiPart BUTTON_B = new GuiPart(40, 133, 20, 20);
    public static final GuiPart OVERLAY_ORANGE = new GuiPart(60, 113, 20, 20);
    public static final GuiPart OVERLAY_BLUE  = new GuiPart(60, 133, 20, 20);
    public static final GuiPart[] BUTTON_DIRECTIONS = {BUTTON_B, BUTTON_T, BUTTON_N, BUTTON_S, BUTTON_W, BUTTON_E};
    public static final GuiPart OVERLAY_SIDE_CONFIG = new GuiPart(128, 0 , 32, 32);
    public static final GuiPart OVERLAY_BLACKLIST = new GuiPart(160, 0 , 32, 32);
    public static final GuiPart OVERLAY_WHITELIST = new GuiPart(192, 0 , 32, 32);
    public static final GuiPart OVERLAY_ARROW = new GuiPart(224, 0 , 32, 32);

    private final ArrayList<ScreenPopup> openPopups = new ArrayList<>();

    public WispContainerScreenBase(T screenContainer, Inventory inv, Component titleIn)
    {
        super(screenContainer, inv, titleIn);
    }
    @Override
    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int x, int y)
    {
        for(ScreenPopup popup : openPopups)
        {
            if(popup.isMouseOver(x, y))
            {
                return;
            }
        }
        super.renderTooltip(guiGraphics, x, y);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        for(ScreenPopup popup : openPopups)
        {
            popup.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    public static void RenderBorder(AbstractWidget widget, @NotNull GuiGraphics guiGraphics, int x, int y, int tabOrderGroup, int width, int height)
    {
        int oldTabOrderGroup = widget.getTabOrderGroup();
        widget.setTabOrderGroup(tabOrderGroup);

        guiGraphics.blit(GUI_PARTS_TEXTURE, x - BORDER_TOP_LEFT.width, y - BORDER_TOP_LEFT.height, BORDER_TOP_LEFT.uStart, BORDER_TOP_LEFT.vStart, BORDER_TOP_LEFT.width, BORDER_TOP_LEFT.height);
        guiGraphics.blit(GUI_PARTS_TEXTURE, x + width, y - BORDER_TOP_RIGHT.height, BORDER_TOP_RIGHT.uStart, BORDER_TOP_RIGHT.vStart, BORDER_TOP_RIGHT.width, BORDER_TOP_RIGHT.height);
        guiGraphics.blit(GUI_PARTS_TEXTURE, x - BORDER_BOTTOM_LEFT.width, y + height, BORDER_BOTTOM_LEFT.uStart, BORDER_BOTTOM_LEFT.vStart, BORDER_BOTTOM_LEFT.width, BORDER_BOTTOM_LEFT.height);
        guiGraphics.blit(GUI_PARTS_TEXTURE, x + width, y + height, BORDER_BOTTOM_RIGHT.uStart, BORDER_BOTTOM_RIGHT.vStart, BORDER_BOTTOM_RIGHT.width, BORDER_BOTTOM_RIGHT.height);

        int numHorizontalBorders = width / BORDER_TOP.width;
        for(int i = 0; i < numHorizontalBorders; ++i)
        {
            guiGraphics.blit(GUI_PARTS_TEXTURE, x + BORDER_TOP.width * i, y - BORDER_TOP.height, BORDER_TOP.uStart, BORDER_TOP.vStart, BORDER_TOP.width, BORDER_TOP.height);
            guiGraphics.blit(GUI_PARTS_TEXTURE, x + BORDER_BOTTOM.width * i, y + height, BORDER_BOTTOM.uStart, BORDER_BOTTOM.vStart, BORDER_BOTTOM.width, BORDER_BOTTOM.height);
        }
        if(width % BORDER_TOP.width != 0)
        {
            guiGraphics.blit(GUI_PARTS_TEXTURE, x + width - BORDER_TOP.width, y - BORDER_TOP.height, BORDER_TOP.uStart, BORDER_TOP.vStart, BORDER_TOP.width, BORDER_TOP.height);
            guiGraphics.blit(GUI_PARTS_TEXTURE, x + width - BORDER_TOP.width, y + height, BORDER_BOTTOM.uStart, BORDER_BOTTOM.vStart, BORDER_BOTTOM.width, BORDER_BOTTOM.height);
        }

        int numVerticalBorders = height / BORDER_LEFT.height;
        for(int i = 0; i < numVerticalBorders; ++i)
        {
            guiGraphics.blit(GUI_PARTS_TEXTURE, x - BORDER_LEFT.width, y + BORDER_LEFT.height * i, BORDER_LEFT.uStart, BORDER_LEFT.vStart, BORDER_LEFT.width, BORDER_LEFT.height);
            guiGraphics.blit(GUI_PARTS_TEXTURE, x + width, y + BORDER_RIGHT.height * i, BORDER_RIGHT.uStart, BORDER_RIGHT.vStart, BORDER_RIGHT.width, BORDER_RIGHT.height);
        }
        if(height % BORDER_LEFT.height != 0)
        {
            guiGraphics.blit(GUI_PARTS_TEXTURE, x - BORDER_LEFT.width, y + height - BORDER_LEFT.height, BORDER_LEFT.uStart, BORDER_LEFT.vStart, BORDER_LEFT.width, BORDER_LEFT.height);
            guiGraphics.blit(GUI_PARTS_TEXTURE, x + width, y + height - BORDER_RIGHT.height, BORDER_RIGHT.uStart, BORDER_RIGHT.vStart, BORDER_RIGHT.width, BORDER_RIGHT.height);
        }
        widget.setTabOrderGroup(oldTabOrderGroup);
    }

    public static void RenderSlotRowBackground(AbstractWidget widget, @NotNull GuiGraphics guiGraphics, int x, int y, int tabOrderGroup, int numSlots)
    {
        int oldTabOrderGroup = widget.getTabOrderGroup();
        widget.setTabOrderGroup(tabOrderGroup);
        RenderSystem.setShaderTexture(0, GUI_PARTS_TEXTURE);

        guiGraphics.blit(GUI_PARTS_TEXTURE, x - 1, y - 1, INV_SLOT.uStart, INV_SLOT.vStart, INV_SLOT.width * numSlots, INV_SLOT.height);

        widget.setTabOrderGroup(oldTabOrderGroup);
    }

    public void RenderPlayerInv(@NotNull GuiGraphics guiGraphics, int playerInvXPos, int playerInvYPos)
    {
        int playerInvXOffset = -3;
        int playerInvYOffset = -4;
        guiGraphics.blit(GUI_PARTS_TEXTURE, leftPos + playerInvXPos + playerInvXOffset, topPos + playerInvYPos + playerInvYOffset, PLAYER_INVENTORY.uStart, PLAYER_INVENTORY.vStart, PLAYER_INVENTORY.width, PLAYER_INVENTORY.height);
    }

    @Override
    protected void init()
    {
        super.init();
    }

    @Override
    public void onClose()
    {
        super.onClose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(openPopups.isEmpty())
        {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        else
        {
            ScreenPopup topPopup = openPopups.getLast();
            if(topPopup.isMouseOver(mouseX, mouseY))
            {
                return topPopup.mouseClicked(mouseX, mouseY, button);
            }
            else
            {
                //close the popup
                CloseTopPopup();
                return true;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta, double unknownValue)
    {
        if(openPopups.isEmpty())
        {
            int slotID = hoveredSlot == null ? -1 : hoveredSlot.index;
            if(getMenu().mouseScrolled(slotID, mouseX, mouseY, delta))
            {
                PacketHandler.SendMouseWheelScrolled(getMenu().containerId, slotID, (float)delta);
                return true;
            }
            return super.mouseScrolled(mouseX, mouseY, delta, unknownValue);
        }
        else
        {
            ScreenPopup topPopup = openPopups.getLast();
            if(topPopup.isMouseOver(mouseX, mouseY))
            {
                return topPopup.mouseScrolled(mouseX, mouseY, delta, unknownValue);
            }
            return false;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if(openPopups.isEmpty())
        {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        else
        {
            if(InputConstants.getKey(keyCode, scanCode).getValue() == InputConstants.KEY_ESCAPE)
            {
                CloseTopPopup();
                return true;
            }

            ScreenPopup topPopup = openPopups.getLast();
            return topPopup.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers)
    {
        if(openPopups.isEmpty())
        {
            return super.keyReleased(keyCode, scanCode, modifiers);
        }
        else
        {
            ScreenPopup topPopup = openPopups.getLast();
            return topPopup.keyReleased(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers)
    {
        if(openPopups.isEmpty())
        {
            return super.charTyped(codePoint, modifiers);
        }
        else
        {
            ScreenPopup topPopup = openPopups.getLast();
            return topPopup.charTyped(codePoint, modifiers);
        }
    }

    public void OpenPopup(ScreenPopup popup)
    {
        popup.setTabOrderGroup(500 + 100 * openPopups.size());
        openPopups.add(popup);
        addRenderableWidget(popup);
        popup.OnOpen();
    }

    public void CloseTopPopup()
    {
        if(openPopups.isEmpty())
        {
            return;
        }
        ScreenPopup topPopup = openPopups.getLast();
        topPopup.OnClose();
        openPopups.removeLast();
        removeWidget(topPopup);
    }
}
