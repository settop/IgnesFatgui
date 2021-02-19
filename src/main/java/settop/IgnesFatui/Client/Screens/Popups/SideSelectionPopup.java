package settop.IgnesFatui.Client.Screens.Popups;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.client.model.data.EmptyModelData;
import settop.IgnesFatui.Client.Screens.MultiScreen;
import settop.IgnesFatui.GUI.Network.Packets.CSubContainerDirectionChange;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Utils.BoolArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SideSelectionPopup extends ScreenPopup
{
    public class FaceButton extends AbstractButton
    {
        private final Direction direction;
        private final BlockState blockState;
        private BoolArray setDirections;
        private final int windowID;
        private final int subWindowID;

        public FaceButton(BlockState blockState, BoolArray setDirections, int windowID, int subWindowID, int x, int y, int width, int height, Direction direction)
        {
            super(x, y, width, height, null);
            this.direction = direction;
            this.blockState = blockState;
            this.setDirections = setDirections;
            this.windowID = windowID;
            this.subWindowID = subWindowID;
        }

        @Override
        public void onPress()
        {
            int d = direction.ordinal();
            boolean isDown = setDirections.GetBool(direction.ordinal());
            boolean nextIsDown = !isDown;
            setDirections.SetBool(d, nextIsDown);

            IgnesFatui.MULTI_SCREEN_CHANNEL.sendToServer(new CSubContainerDirectionChange(windowID, subWindowID, direction, nextIsDown));
        }

        public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
        {
            boolean isDown = setDirections.get(direction.ordinal()) != 0;

            Minecraft minecraft = Minecraft.getInstance();
            IBakedModel blockModel = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(blockState);

            Random random = new Random();
            random.setSeed(42);
            List<BakedQuad> quads = blockModel.getQuads(blockState, direction, random, EmptyModelData.INSTANCE);

            if(quads != null && quads.size() == 1)
            {
                TextureAtlasSprite sprite = quads.get(0).getSprite();

                minecraft.getTextureManager().bindTexture(sprite.getAtlasTexture().getTextureLocation());
                RenderSystem.enableDepthTest();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                blit(matrixStack, x, y, getBlitOffset(), 20, 20, sprite );

                this.renderBg(matrixStack, minecraft, mouseX, mouseY);
            }
            else
            {
                minecraft.getTextureManager().bindTexture(MultiScreen.GUI_PARTS_TEXTURE);
                RenderSystem.enableDepthTest();
                FontRenderer fontrenderer = minecraft.fontRenderer;
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

                MultiScreen.GuiPart sidePart = MultiScreen.BUTTON_DIRECTIONS[direction.getIndex()];

                blit(matrixStack, this.x, this.y, sidePart.uStart, sidePart.vStart, sidePart.width, sidePart.height );
                this.renderBg(matrixStack, minecraft, mouseX, mouseY);
            }

            if(isDown)
            {
                minecraft.getTextureManager().bindTexture(MultiScreen.GUI_PARTS_TEXTURE);
                MultiScreen.GuiPart overlayPart = MultiScreen.OVERLAY_BLUE;
                blit(matrixStack, this.x, this.y, overlayPart.uStart, overlayPart.vStart, overlayPart.width, overlayPart.height );
            }
        }
    }

    static public final int BUTTON_SIZE = 20;

    static public final Vector3i OFFSETS[] = {
            /*Down*/new Vector3i(0, BUTTON_SIZE, 0),
            /*Up*/new Vector3i(0, -BUTTON_SIZE, 0),
            /*North*/new Vector3i(0, 0, 0),
            /*South*/new Vector3i(BUTTON_SIZE, BUTTON_SIZE, 0),
            /*East*/new Vector3i(-BUTTON_SIZE, 0, 0),
            /*West*/new Vector3i(BUTTON_SIZE, 0, 0)
    };

    public List<FaceButton> faceButtons;

    public SideSelectionPopup(BlockState blockState, BoolArray setDirections, int windowID, int subWindowID, int x, int y)
    {
        super(x,y, BUTTON_SIZE * 3, BUTTON_SIZE * 3);

        faceButtons = new ArrayList<>();
        for(int i = 0; i < 6; ++i)
        {
            FaceButton faceButton = new FaceButton( blockState, setDirections, windowID, subWindowID, x + OFFSETS[i].getX() + BUTTON_SIZE, y + OFFSETS[i].getY() + BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE, Direction.byIndex(i));
            faceButtons.add( AddListener(faceButton) );
        }
    }

    @Override
    public void Render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.fillGradient(matrixStack, x, y, x + width, y + height, MultiScreen.BG_COLOUR, MultiScreen.BG_COLOUR);
        for(FaceButton faceButton : faceButtons)
        {
            faceButton.setBlitOffset(getBlitOffset() + 1);
            faceButton.render(matrixStack, mouseX, mouseY, partialTicks);
        }
        MultiScreen.RenderBorder(this, matrixStack, x, y, getBlitOffset(), width, height);
    }

    @Override
    public void OnOpen()
    {
    }

    @Override
    public void OnClose()
    {
    }

}
