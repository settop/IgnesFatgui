package settop.IgnesFatui.Client;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import settop.IgnesFatui.IgnesFatui;

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
}
