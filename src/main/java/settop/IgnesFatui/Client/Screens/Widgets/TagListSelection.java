package settop.IgnesFatui.Client.Screens.Widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import org.apache.commons.lang3.StringUtils;
import settop.IgnesFatui.GUI.FakeSlot;
import settop.IgnesFatui.Utils.FakeInventory;

import java.util.ArrayList;


public abstract class TagListSelection extends Widget
{
    public static final int LINE_HEIGHT = 10;
    public static final int DEFAULT_TEXT_COLOUR = 0xE0E0E0;
    public static final int TEXT_REGION_BORDER_SIZE = 4;
    public static final int POSSIBLE_TAG_GAP = 4;

    protected TextFieldWidget textEntry;

    protected final FontRenderer fontRenderer;
    protected int fullHeight;
    protected float tagEntryFailedTimer = 0.f;
    private int selectedTagIndex = -1;
    private FakeSlot itemTagFetchSlot;
    private ItemStack previousTagFetchItem;
    private ArrayList<String> possibleTags = new ArrayList<>();

    protected abstract ArrayList<String> GetTagList();
    protected abstract void UpdateTagList(ArrayList<String> updatedList);

    public TagListSelection(FontRenderer font, int x, int y, int width, int height, ITextComponent title, FakeSlot tagFetchSlot)
    {
        super(x, y, width, height, title);
        textEntry = new TextFieldWidget(font, x, y, width, LINE_HEIGHT, title);
        fontRenderer = font;
        fullHeight = height;

        itemTagFetchSlot = tagFetchSlot;
    }

    public static boolean IsValidTag(String str)
    {
        String[] splitString = StringUtils.split(str, ':');

        if(splitString.length != 2)
        {
            return false;
        }

        try
        {
            ResourceLocation tagId = new ResourceLocation(splitString[0], splitString[1]);
            ITag<?> tag = ItemTags.getCollection().get(tagId);
            return tag != null;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers)
    {
        return textEntry.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(!this.visible || !this.active)
        {
            return false;
        }

        if(textEntry.mouseClicked(mouseX, mouseY, button))
        {
            return true;
        }

        ArrayList<String> tagList = GetTagList();

        int listXPos = this.x + TEXT_REGION_BORDER_SIZE;

        int listYStartPossibleTags = this.y + LINE_HEIGHT + TEXT_REGION_BORDER_SIZE;
        int listYEndPossibleTags = listYStartPossibleTags + possibleTags.size() * LINE_HEIGHT;
        int listYStartTagList = listYEndPossibleTags + (possibleTags.isEmpty() ? 0 : POSSIBLE_TAG_GAP);
        int listYEndTagList = listYStartTagList + tagList.size() * LINE_HEIGHT;

        int xEnd = listXPos + textEntry.getAdjustedWidth();
        int yEndMax = this.y + fullHeight - TEXT_REGION_BORDER_SIZE;

        if(listXPos <= mouseX && mouseX <= xEnd &&
                listYStartPossibleTags <= mouseY && mouseY <= yEndMax)
        {
            if(listYStartPossibleTags <= mouseY && mouseY < listYEndPossibleTags)
            {
                int selectedPossibleTag = (int)((mouseY - listYStartPossibleTags) / LINE_HEIGHT);
                if(selectedPossibleTag >= 0 && selectedPossibleTag < possibleTags.size())
                {
                    tagList.add(possibleTags.get(selectedPossibleTag));
                    possibleTags.remove(selectedPossibleTag);
                    UpdateTagList(tagList);
                }
                selectedTagIndex = -1;
                textEntry.setText("");
                return true;
            }
            else if(listYStartTagList <= mouseY && mouseY < listYEndTagList)
            {
                int newlySelectedTagIndex = (int)((mouseY - listYStartTagList) / LINE_HEIGHT);
                if(newlySelectedTagIndex != selectedTagIndex && newlySelectedTagIndex >= 0 && newlySelectedTagIndex < tagList.size())
                {
                    selectedTagIndex = newlySelectedTagIndex;
                    textEntry.setText(tagList.get(selectedTagIndex));
                }
                else
                {
                    selectedTagIndex = -1;
                    textEntry.setText("");
                }
            }
            return true;
        }
        else
        {
            selectedTagIndex = -1;
            textEntry.setText("");
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        boolean consumeKey = false;
        if(textEntry.canWrite())
        {
            if(keyCode >= 65 && keyCode <= 90)
            {
                //make sure that any keybinds on letters don't do anything while text is focused
                consumeKey = true;
            }
            else if(keyCode == 257)
            {
                //enter pressed
                consumeKey = true;

                String tag = textEntry.getText();
                if(IsValidTag(tag))
                {
                    ArrayList<String> tagList = GetTagList();
                    if(selectedTagIndex >= 0 && selectedTagIndex < tagList.size())
                    {
                        tagList.set(selectedTagIndex, tag);
                        selectedTagIndex = -1;
                    }
                    else
                    {
                        tagList.add(tag);
                    }
                    textEntry.setText("");
                    UpdateTagList(tagList);
                }
                else
                {
                    tagEntryFailedTimer = 1.5f;
                }
            }
        }
        else if(keyCode == 261 && selectedTagIndex != -1)
        {
            ArrayList<String> tagList = GetTagList();
            tagList.remove(selectedTagIndex);
            selectedTagIndex = -1;
            textEntry.setText("");
            UpdateTagList(tagList);
            return true;
        }
        return textEntry.keyPressed(keyCode, scanCode, modifiers) || consumeKey;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        if (!visible)
        {
            return;
        }

        tagEntryFailedTimer -= (partialTicks / 20.f);
        if(tagEntryFailedTimer > 0.f)
        {
            int failedColour = ColorHelper.PackedColor.packColor(255, 224, 0, 0);
            if(tagEntryFailedTimer > 0.5f)
            {
                textEntry.setTextColor(failedColour);
            }
            else
            {
                //fade back to normal
                float blend = tagEntryFailedTimer / 0.5f;
                int r = (int)(ColorHelper.PackedColor.getRed(DEFAULT_TEXT_COLOUR) * (1.f - blend) + ColorHelper.PackedColor.getRed(failedColour) * blend);
                int g = (int)(ColorHelper.PackedColor.getGreen(DEFAULT_TEXT_COLOUR) * (1.f - blend) + ColorHelper.PackedColor.getGreen(failedColour) * blend);
                int b = (int)(ColorHelper.PackedColor.getBlue(DEFAULT_TEXT_COLOUR) * (1.f - blend) + ColorHelper.PackedColor.getBlue(failedColour) * blend);

                textEntry.setTextColor(ColorHelper.PackedColor.packColor(255, r, g, b));
            }
        }
        else
        {
            textEntry.setTextColor(DEFAULT_TEXT_COLOUR);
        }

        fill(matrixStack, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.fullHeight + 1, -6250336);
        fill(matrixStack, this.x, this.y, this.x + this.width, this.y + this.fullHeight, -16777216);

        ArrayList<String> tagList = GetTagList();

        int listXPos = this.x + TEXT_REGION_BORDER_SIZE;
        int listYPos = this.y + LINE_HEIGHT + TEXT_REGION_BORDER_SIZE;

        int xEnd = listXPos + textEntry.getAdjustedWidth();
        int yEnd = listYPos + fullHeight - LINE_HEIGHT - 2 * TEXT_REGION_BORDER_SIZE;

        MainWindow mainWindow = Minecraft.getInstance().getMainWindow();
        double scaleFactor = mainWindow.getGuiScaleFactor();

        int scissorWidth = (int)(textEntry.getAdjustedWidth() * scaleFactor);
        int scissorHeight = (int)((fullHeight - LINE_HEIGHT - 2 * TEXT_REGION_BORDER_SIZE) * scaleFactor);
        int scissorX = (int)(listXPos * scaleFactor);
        int scissorY = mainWindow.getFramebufferHeight() - (int)(listYPos * scaleFactor) - scissorHeight;
        RenderSystem.enableScissor(scissorX, scissorY, scissorWidth, scissorHeight);

        ItemStack tagFetchItem = itemTagFetchSlot != null ? itemTagFetchSlot.getStack() : null;
        if(previousTagFetchItem != tagFetchItem)
        {
            previousTagFetchItem = tagFetchItem;
            possibleTags.clear();
            if(tagFetchItem != null)
            {
                for(ResourceLocation tag : tagFetchItem.getItem().getTags())
                {
                    String tagStr = tag.getNamespace() + ':' + tag.getPath();
                    if(!tagList.contains(tagStr))
                    {
                        possibleTags.add(tagStr);
                    }
                }
            }
        }

        if(!possibleTags.isEmpty())
        {
            for(String possibleTag : possibleTags)
            {
                if(listXPos <= mouseX && mouseX <= xEnd &&
                        listYPos <= mouseY && mouseY <= listYPos + LINE_HEIGHT)
                {
                    fill(matrixStack, listXPos, listYPos, xEnd, listYPos + LINE_HEIGHT, ColorHelper.PackedColor.packColor(128, 224, 224, 224));
                }
                this.fontRenderer.func_238407_a_(matrixStack, IReorderingProcessor.fromString(possibleTag, Style.EMPTY), (float)listXPos, (float)listYPos, DEFAULT_TEXT_COLOUR);
                listYPos += LINE_HEIGHT;
            }
            int finalOffset = POSSIBLE_TAG_GAP + LINE_HEIGHT;
            listYPos += POSSIBLE_TAG_GAP;
            listYPos -= finalOffset / 2;
            this.fontRenderer.func_238407_a_(matrixStack, IReorderingProcessor.fromString("-----------------------------------", Style.EMPTY), (float)listXPos, (float)listYPos, DEFAULT_TEXT_COLOUR);
            listYPos += finalOffset / 2;
        }

        for(int i = 0; i < tagList.size(); ++i)
        {
            if(i == selectedTagIndex)
            {
                fill(matrixStack, listXPos, listYPos, xEnd, listYPos + LINE_HEIGHT, ColorHelper.PackedColor.packColor(192, 224, 224, 224));
            }
            else if(listXPos <= mouseX && mouseX <= xEnd &&
                    listYPos <= mouseY && mouseY <= listYPos + LINE_HEIGHT)
            {
                fill(matrixStack, listXPos, listYPos, xEnd, listYPos + LINE_HEIGHT, ColorHelper.PackedColor.packColor(128, 224, 224, 224));
            }


            this.fontRenderer.func_238407_a_(matrixStack, IReorderingProcessor.fromString(tagList.get(i), Style.EMPTY), (float)listXPos, (float)listYPos, DEFAULT_TEXT_COLOUR);
            listYPos += LINE_HEIGHT;
        }

        RenderSystem.disableScissor();

        textEntry.renderButton(matrixStack, mouseX, mouseY, partialTicks);
    }
}
