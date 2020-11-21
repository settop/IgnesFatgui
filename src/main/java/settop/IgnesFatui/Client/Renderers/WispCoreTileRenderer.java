package settop.IgnesFatui.Client.Renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.geometry.IModelGeometryPart;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import settop.IgnesFatui.Client.Client;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.TileEntities.WispCoreTileEntity;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class WispCoreTileRenderer extends TileEntityRenderer<WispCoreTileEntity>
{
    private IBakedModel ringModels[] = null;

    public WispCoreTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
    {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(WispCoreTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        if(ringModels == null)
        {
            ringModels = new IBakedModel[3];
            ringModels[0] = Minecraft.getInstance().getModelManager().getModel(Client.WISP_CORE_RING_0);
            ringModels[1] = Minecraft.getInstance().getModelManager().getModel(Client.WISP_CORE_RING_1);
            ringModels[2] = Minecraft.getInstance().getModelManager().getModel(Client.WISP_CORE_RING_2);
        }

        if(!tileEntityIn.IsMultiblockComplete())
        {
            return;
        }

        IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getSolid());

        tileEntityIn.renderTimer += partialTicks * 0.05f;

        float bobAmplitude = 0.03f;
        float bobCycleTime = 10.f;

        for(int i = 0; i < 3; ++i)
        {
            matrixStackIn.push();

            float bob = bobAmplitude * (3 - i) * MathHelper.sin((tileEntityIn.renderTimer + bobCycleTime * i * 0.1f) * (float)Math.PI * 2.f / bobCycleTime );
            matrixStackIn.translate(0.f, bob, 0.f);

            matrixStackIn.translate(0.5f, 0.5f, 0.5f );
            matrixStackIn.scale(1.5f, 1.5f, 1.5f);


            matrixStackIn.rotate(new Quaternion(Vector3f.YP, tileEntityIn.renderTimer * 0.1f, false));
            if(i < 3) matrixStackIn.rotate(new Quaternion(Vector3f.ZP, tileEntityIn.renderTimer * 0.33f, false));
            if(i < 2) matrixStackIn.rotate(new Quaternion(Vector3f.XP, tileEntityIn.renderTimer * 0.66f, false));
            if(i < 1) matrixStackIn.rotate(new Quaternion(Vector3f.YP, tileEntityIn.renderTimer, false));

            switch(i)
            {
                case 0:
                    matrixStackIn.rotate(new Quaternion(Vector3f.YP, 90.f, true));
                    break;
                case 1:
                    break;
                default:
                    matrixStackIn.rotate(new Quaternion(Vector3f.XP, 90.f, true));
                    break;
            }
            BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();

            dispatcher.getBlockModelRenderer().renderModel(matrixStackIn.getLast(), ivertexbuilder, null, ringModels[i],
                    1.f, 1.f, 1.f, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);

            matrixStackIn.pop();
        }
    }

}
