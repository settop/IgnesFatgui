package settop.IgnesFatui.Client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Wisps.ChunkWispData;

@Mod.EventBusSubscriber(modid = IgnesFatui.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
@OnlyIn(Dist.CLIENT)
public class Client
{
    public static final ResourceLocation WISP_CORE_RING_0 = new ResourceLocation(IgnesFatui.MOD_ID, "block/wisp_core_ring_0");
    public static final ResourceLocation WISP_CORE_RING_1 = new ResourceLocation(IgnesFatui.MOD_ID, "block/wisp_core_ring_1");
    public static final ResourceLocation WISP_CORE_RING_2 = new ResourceLocation(IgnesFatui.MOD_ID, "block/wisp_core_ring_2");

    public static final ResourceLocation WISP_CORE_RING_TEX = new ResourceLocation(IgnesFatui.MOD_ID, "blocks/wisp_core_ring_test");


    public static final int SLOT_X_SPACING = 18;
    public static final int SLOT_Y_SPACING = 18;
    public static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;

    @SubscribeEvent
    public static void RenderWorldLastEvent(RenderWorldLastEvent evt)
    {
        //this will only work on single player
        final int renderType = 2;
        switch (renderType)
        {
            case 0:
                break;
            case 1:
                ChunkWispData.RenderConnections(evt);
                break;
            case 2:
                ChunkWispData.RenderConnections(evt);
                break;
        }
    }
}
