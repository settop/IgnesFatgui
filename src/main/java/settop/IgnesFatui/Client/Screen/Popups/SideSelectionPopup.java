package settop.IgnesFatui.Client.Screen.Popups;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.Client.Screen.WispContainerScreenBase;

import java.util.ArrayList;
import java.util.List;

public abstract class SideSelectionPopup extends ScreenPopup
{
    private static final String[] BUTTON_NAMES = {"B", "T", "N", "S", "W", "E"};
    public class FaceButton extends AbstractButton
    {
        private final Direction direction;

        public FaceButton(int x, int y, int width, int height, Direction direction)
        {
            super(x, y, width, height, null);
            this.direction = direction;
        }

        @Override
        public void onPress()
        {
            if(direction == selectedDirection)
            {
                selectedDirection = null;
                OnSelectedDirectionChange(null);
            }
            else
            {
                selectedDirection = direction;
                OnSelectedDirectionChange(direction);
            }
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
        {
            boolean isDown = selectedDirection == direction;

            Minecraft minecraft = Minecraft.getInstance();
            BakedModel blockModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState);

            RandomSource random = RandomSource.create(42);
            List<BakedQuad> quads = blockModel.getQuads(blockState, direction, random);

            if(quads != null && quads.size() == 1)
            {
                TextureAtlasSprite sprite = quads.getFirst().getSprite();

                // RenderSystem.setShaderTexture(0, sprite.atlasLocation());
                RenderSystem.enableDepthTest();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                guiGraphics.blitSprite(sprite.atlasLocation(), getX(), getY(), getTabOrderGroup(), 20, 20 );

                //this.renderBg(matrixStack, minecraft, mouseX, mouseY);
            }
            else
            {
                WispContainerScreenBase.GuiPart buttonPart = isHoveredOrFocused() ? WispContainerScreenBase.BUTTON_HOVERED : WispContainerScreenBase.BUTTON;

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(getX(), getY(), 0);
                guiGraphics.pose().scale((float)BUTTON_SIZE / buttonPart.width, (float)BUTTON_SIZE / buttonPart.height, 1.f);

                guiGraphics.blit(WispContainerScreenBase.GUI_PARTS_TEXTURE,0, 0, buttonPart.uStart, buttonPart.vStart, buttonPart.width, buttonPart.height );

                guiGraphics.pose().translate(8.f,5.f, getTabOrderGroup() + 1.f);
                float textScale = 3.f;
                guiGraphics.pose().scale(textScale, textScale, 1.f);
                guiGraphics.drawString(Minecraft.getInstance().font, BUTTON_NAMES[direction.get3DDataValue()], 0, 0, 0x0f0f0f);

                guiGraphics.pose().popPose();
                //this.renderBg(matrixStack, minecraft, mouseX, mouseY);
            }

            if(isDown)
            {
                RenderSystem.setShaderTexture(0, WispContainerScreenBase.GUI_PARTS_TEXTURE);
                WispContainerScreenBase.GuiPart overlayPart = isExtraction ? WispContainerScreenBase.OVERLAY_ORANGE : WispContainerScreenBase.OVERLAY_BLUE;
                guiGraphics.blit(WispContainerScreenBase.GUI_PARTS_TEXTURE, getX(), getY(), overlayPart.uStart, overlayPart.vStart, overlayPart.width, overlayPart.height );
            }
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

        }
    }

    static public final int BUTTON_SIZE = 20;

    static public final Vec3i[] OFFSETS = {
            /*Down*/new Vec3i(0, BUTTON_SIZE, 0),
            /*Up*/new Vec3i(0, -BUTTON_SIZE, 0),
            /*North*/new Vec3i(0, 0, 0),
            /*South*/new Vec3i(BUTTON_SIZE, BUTTON_SIZE, 0),
            /*East*/new Vec3i(-BUTTON_SIZE, 0, 0),
            /*West*/new Vec3i(BUTTON_SIZE, 0, 0)
    };

    private final BlockState blockState;
    private final boolean isExtraction;
    public Direction selectedDirection;
    public List<FaceButton> faceButtons;

    public SideSelectionPopup(BlockState blockState, boolean isExtraction, int x, int y)
    {
        super(x,y, BUTTON_SIZE * 3, BUTTON_SIZE * 3);

        this.blockState = blockState;
        this.isExtraction = isExtraction;
        faceButtons = new ArrayList<>();
        selectedDirection = null;
        for(int i = 0; i < 6; ++i)
        {
            FaceButton faceButton = new FaceButton(x + OFFSETS[i].getX() + BUTTON_SIZE, y + OFFSETS[i].getY() + BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE, Direction.from3DDataValue(i));
            faceButtons.add( AddListener(faceButton) );
        }
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        guiGraphics.fillGradient(getX(), getY(), getX() + width, getY() + height, WispContainerScreenBase.BG_COLOUR, WispContainerScreenBase.BG_COLOUR);
        for(FaceButton faceButton : faceButtons)
        {
            faceButton.setTabOrderGroup(getTabOrderGroup() + 1);
            faceButton.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
        WispContainerScreenBase.RenderBorder(this, guiGraphics, getX(), getY(), getTabOrderGroup(), width, height);
    }

    public void SetSelectedDirection(Direction direction)
    {
        selectedDirection = direction;
    }

    public Direction GetSelectedDirection()
    {
        return selectedDirection;
    }

    @Override
    public @NotNull NarrationPriority narrationPriority()
    {
        return NarrationPriority.FOCUSED;
    }

    public abstract void OnSelectedDirectionChange(Direction newDirection);
}
