package settop.IgnesFatui.Client.Screens.SubScreens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.sun.org.apache.xml.internal.utils.IntVector;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.IntArray;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import settop.IgnesFatui.GUI.Network.Packets.ProviderContainerDirectionChange;
import settop.IgnesFatui.GUI.SubContainers.ProviderEnhancementSubContainer;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Utils.BoolArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class ProviderSubScreen extends SubScreen
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/checkbox.png");
    public class FaceButton extends AbstractButton
    {
        private Direction direction;

        public FaceButton(int x, int y, int width, int height, Direction direction)
        {
            super(x, y, width, height, null);
            this.direction = direction;
        }

        private BoolArray GetDirectionValues()
        {
            ProviderEnhancementSubContainer providerContainer = (ProviderEnhancementSubContainer)GetSubContainer();
            return providerContainer.GetDirectionsProvided();
        }

        @Override
        public void onPress()
        {
            int d = direction.ordinal();
            BoolArray directionValues = GetDirectionValues();
            boolean isDown = directionValues.GetBool(direction.ordinal());
            boolean nextIsDown = !isDown;
            directionValues.SetBool(d, nextIsDown);

            IgnesFatui.MULTI_SCREEN_CHANNEL.sendToServer(new ProviderContainerDirectionChange(GetSubContainer().GetParentContainer().windowId, direction, nextIsDown));
        }

        public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
        {
            boolean isDown = GetDirectionValues().get(direction.ordinal()) != 0;

            ProviderEnhancementSubContainer providerContainer = (ProviderEnhancementSubContainer)GetSubContainer();
            BlockState blockState = providerContainer.GetBlockState();

            IBakedModel blockModel = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(blockState);

            Random random = new Random();
            random.setSeed(42);
            List<BakedQuad> quads = blockModel.getQuads(blockState, direction, random, EmptyModelData.INSTANCE);

            if(quads != null && quads.size() == 1)
            {
                TextureAtlasSprite sprite = quads.get(0).getSprite();

                Minecraft minecraft = Minecraft.getInstance();
                minecraft.getTextureManager().bindTexture(sprite.getAtlasTexture().getTextureLocation());
                RenderSystem.enableDepthTest();
                FontRenderer fontrenderer = minecraft.fontRenderer;
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                blit(matrixStack, this.x, this.y, 0, 20, 20, sprite );

                if(isDown)
                {
                    int colour = ColorHelper.PackedColor.packColor(64, 0, 255, 0);
                    fill(matrixStack, x, y, x + 20, y + 20, colour);
                }

                this.renderBg(matrixStack, minecraft, mouseX, mouseY);
            }
            else
            {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.getTextureManager().bindTexture(TEXTURE);
                RenderSystem.enableDepthTest();
                FontRenderer fontrenderer = minecraft.fontRenderer;
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                blit(matrixStack, this.x, this.y, this.isFocused() ? 20.0F : 0.0F, isDown ? 20.0F : 0.0F, width, height, 64, 64);
                this.renderBg(matrixStack, minecraft, mouseX, mouseY);
            }

        }


    }

    private List<FaceButton> faceButtons;

    static private final int BUTTON_SIZE = 20;

    static private final Vector3i OFFSETS[] = {
        /*Down*/new Vector3i(0, BUTTON_SIZE, 0),
        /*Up*/new Vector3i(0, -BUTTON_SIZE, 0),
        /*North*/new Vector3i(0, 0, 0),
        /*South*/new Vector3i(BUTTON_SIZE, BUTTON_SIZE, 0),
        /*East*/new Vector3i(-BUTTON_SIZE, 0, 0),
        /*West*/new Vector3i(BUTTON_SIZE, 0, 0)
    };

    public ProviderSubScreen(ProviderEnhancementSubContainer container)
    {
        super(container);
    }

    @Override
    public void init(int guiLeft, int guiTop)
    {
        super.init(guiLeft, guiTop);

        int xPos = GetSubContainer().GetXPos();
        int yPos = GetSubContainer().GetYPos();

        ProviderEnhancementSubContainer providerContainer = (ProviderEnhancementSubContainer)GetSubContainer();
        faceButtons = new ArrayList<>();
        for(int i = 0; i < 6; ++i)
        {
            faceButtons.add(AddWidget(new FaceButton( guiLeft + xPos + OFFSETS[i].getX(), guiTop + yPos + OFFSETS[i].getY(), BUTTON_SIZE, BUTTON_SIZE, Direction.byIndex(i))));
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        for(FaceButton faceButton : faceButtons)
        {
            faceButton.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }
}
