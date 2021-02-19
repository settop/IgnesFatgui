package settop.IgnesFatui.Client.Screens.SubScreens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import settop.IgnesFatui.Client.Client;
import settop.IgnesFatui.Client.Screens.MultiScreen;
import settop.IgnesFatui.Client.Screens.Popups.SideSelectionPopup;
import settop.IgnesFatui.Client.Screens.Widgets.SmallButton;
import settop.IgnesFatui.Client.Screens.Widgets.TagListSelection;
import settop.IgnesFatui.GUI.FakeSlot;
import settop.IgnesFatui.GUI.Network.Packets.CSubWindowPropertyUpdatePacket;
import settop.IgnesFatui.GUI.Network.Packets.CSubWindowStringPropertyUpdatePacket;
import settop.IgnesFatui.GUI.SubContainers.ProviderEnhancementSubContainer;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Wisps.Enhancements.ProviderEnhancement;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class ProviderSubScreen extends SubScreen
{

    public class SideConfigButton extends SmallButton
    {
        public SideConfigButton(int x, int y)
        {
            super(x, y, new TranslationTextComponent(""));
        }

        @Override
        public void onPress()
        {
            GetParentScreen().OpenPopup(sideSelectionPopup);
        }

        @Override
        public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY)
        {
            GetParentScreen().renderTooltip(matrixStack, new TranslationTextComponent("sif1.side_config"), mouseX, mouseY);
        }

        @Override
        public void RenderOverlay(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
        {
            MultiScreen.GuiPart overlayPart = MultiScreen.OVERLAY_SIDE_CONFIG;
            blit(matrixStack, 0, 0, overlayPart.uStart, overlayPart.vStart, overlayPart.width, overlayPart.height );
        }
    }
    public class WhitelistToggle extends SmallButton
    {
        public WhitelistToggle(int x, int y)
        {
            super(x, y, new TranslationTextComponent(""));
        }

        @Override
        public void onPress()
        {
            ProviderEnhancementSubContainer providerContainer = (ProviderEnhancementSubContainer)GetSubContainer();
            boolean newEnabled = !providerContainer.GetWhitelistEnabled();
            providerContainer.SetWhitelistEnabled(newEnabled);

            IgnesFatui.MULTI_SCREEN_CHANNEL.sendToServer(new CSubWindowPropertyUpdatePacket(GetParentScreen().getContainer().windowId, GetSubContainer().GetSubWindowID(), ProviderEnhancementSubContainer.WHITELIST_PROPERTY_ID,  newEnabled ? 1 : 0));
        }

        @Override
        public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY)
        {
            ProviderEnhancementSubContainer providerContainer = (ProviderEnhancementSubContainer)GetSubContainer();
            GetParentScreen().renderTooltip(matrixStack, new TranslationTextComponent(providerContainer.GetWhitelistEnabled() ? "sif1.whitelist" : "sif1.blacklist"), mouseX, mouseY);
        }

        @Override
        public void RenderOverlay(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
        {
            ProviderEnhancementSubContainer providerContainer = (ProviderEnhancementSubContainer)GetSubContainer();
            MultiScreen.GuiPart overlayPart = providerContainer.GetWhitelistEnabled() ? MultiScreen.OVERLAY_WHITELIST : MultiScreen.OVERLAY_BLACKLIST;
            blit(matrixStack, 0, 0, overlayPart.uStart, overlayPart.vStart, overlayPart.width, overlayPart.height );
        }
    }

    public class FilterTypeCycle extends SmallButton
    {
        public FilterTypeCycle(int x, int y)
        {
            super(x, y, null);
        }

        @Override
        public void onPress()
        {
            ProviderEnhancementSubContainer providerContainer = (ProviderEnhancementSubContainer)GetSubContainer();

            int nextValue = providerContainer.GetFilterType().ordinal() + 1;
            if(nextValue >= ProviderEnhancement.eFilterType.values().length)
            {
                nextValue = 0;
            }
            providerContainer.SetFilterType(ProviderEnhancement.eFilterType.values()[nextValue]);
            IgnesFatui.MULTI_SCREEN_CHANNEL.sendToServer(new CSubWindowPropertyUpdatePacket(GetParentScreen().getContainer().windowId, GetSubContainer().GetSubWindowID(), ProviderEnhancementSubContainer.FILTER_TYPE_PROPERTY_ID,  nextValue));
        }

        @Override
        public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY)
        {
            ProviderEnhancementSubContainer providerContainer = (ProviderEnhancementSubContainer)GetSubContainer();
            TranslationTextComponent text;
            switch(providerContainer.GetFilterType())
            {
                default:
                case Item:
                    text = new TranslationTextComponent("sif1.item_filter");
                    break;
                case Tag:
                    text = new TranslationTextComponent("sif1.tag_filter");
                    break;
            }
            GetParentScreen().renderTooltip(matrixStack, text, mouseX, mouseY);
        }

        @Override
        public void RenderOverlay(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
        {
        }
    }

    public class FilterTags extends TagListSelection
    {

        public FilterTags(FontRenderer font, int x, int y, int width, int height, ITextComponent title, FakeSlot tagFetchSlot)
        {
            super(font, x, y, width, height, title, tagFetchSlot);
        }

        @Override
        protected ArrayList<String> GetTagList()
        {
            ProviderEnhancementSubContainer providerContainer = (ProviderEnhancementSubContainer)GetSubContainer();
            return providerContainer.GetFilterTags();
        }

        @Override
        protected void UpdateTagList(ArrayList<String> updatedList)
        {
            ProviderEnhancementSubContainer providerContainer = (ProviderEnhancementSubContainer)GetSubContainer();
            String tagList = providerContainer.SetFilterTags(updatedList);
            IgnesFatui.MULTI_SCREEN_CHANNEL.sendToServer(new CSubWindowStringPropertyUpdatePacket(GetParentScreen().getContainer().windowId, GetSubContainer().GetSubWindowID(), ProviderEnhancementSubContainer.FILTER_TAGS_STRING_PROPERTY_ID,  tagList));

        }
    }

    private SideSelectionPopup sideSelectionPopup;
    private SideConfigButton sideSelectionButton;
    private WhitelistToggle whitelistToggle;
    private FilterTypeCycle filterTypeCycle;
    private FilterTags tagSelection;


    public ProviderSubScreen(ProviderEnhancementSubContainer container, MultiScreen<?> parentScreen)
    {
        super(container, parentScreen);
    }

    @Override
    public void init(int guiLeft, int guiTop)
    {
        super.init(guiLeft, guiTop);

        int xPos = GetSubContainer().GetXPos();
        int yPos = GetSubContainer().GetYPos();

        ProviderEnhancementSubContainer providerContainer = (ProviderEnhancementSubContainer)GetSubContainer();

        sideSelectionButton = AddWidget(new SideConfigButton(guiLeft + xPos, guiTop + yPos));
        whitelistToggle = AddWidget(new WhitelistToggle(guiLeft + xPos, guiTop + yPos + 8));
        filterTypeCycle = AddWidget(new FilterTypeCycle(guiLeft + xPos, guiTop + yPos + 16));

        final int popupXOffset = 6;
        final int popupYOffset = 6;

        sideSelectionPopup = new SideSelectionPopup
                (
                        providerContainer.GetBlockState(),
                        providerContainer.GetDirectionsProvided(),
                        providerContainer.GetParentContainer().windowId,
                        providerContainer.GetSubWindowID(),
                        sideSelectionButton.x + sideSelectionButton.getWidth() + popupXOffset,
                        sideSelectionButton.y + popupYOffset
                );

        int tagSelectionXOffset = xPos + 21;
        tagSelection = AddWidget(new FilterTags
                (
                        Minecraft.getInstance().fontRenderer,
                        guiLeft + tagSelectionXOffset, guiTop + yPos,
                        GetParentScreen().getXSize() - tagSelectionXOffset, 80,
                        new TranslationTextComponent(""),
                        providerContainer.GetTagFetchHelperSlot()
                ));
    }

    @Override
    public void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        for(int r = 0; r < ProviderEnhancement.FILTER_NUM_ROWS; ++r)
        {
            MultiScreen.RenderSlotRowBackground(this, matrixStack, guiLeft + ProviderEnhancementSubContainer.FILTER_SLOT_X, guiTop + ProviderEnhancementSubContainer.FILTER_SLOT_Y + r * Client.SLOT_Y_SPACING, getBlitOffset(), ProviderEnhancement.FILTER_NUM_COLUMNS);
        }

        ProviderEnhancementSubContainer providerContainer = (ProviderEnhancementSubContainer)GetSubContainer();
        if(providerContainer.GetFilterType() == ProviderEnhancement.eFilterType.Tag)
        {
            MultiScreen.RenderSlotRowBackground(this, matrixStack, guiLeft + ProviderEnhancementSubContainer.TAG_FETCH_HELPER_SLOT_X, guiTop + ProviderEnhancementSubContainer.TAG_FETCH_HELPER_SLOT_Y, getBlitOffset(), 1);
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
    }

    @Override
    public void SetActive(boolean active)
    {
        if(this.active != active)
        {
            this.active = active;

            sideSelectionButton.active =  sideSelectionButton.visible = active;
            whitelistToggle.active =  whitelistToggle.visible = active;
            filterTypeCycle.active =  filterTypeCycle.visible = active;
        }

        ProviderEnhancementSubContainer providerContainer = (ProviderEnhancementSubContainer)GetSubContainer();
        tagSelection.active =  tagSelection.visible = active && (providerContainer.GetFilterType() == ProviderEnhancement.eFilterType.Tag);
    }
}
