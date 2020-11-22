package settop.IgnesFatui.Wisps;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunk;

import javax.annotation.Nonnull;

public class WispFactory
{
    @Nonnull
    public static IWisp CreateNewWisp(String type, IChunk chunk, BlockPos inPos)
    {
        switch (type)
        {
            case WispConstants.BASIC_WISP: return new BasicWisp(chunk, inPos);
            default: throw new RuntimeException(type + " is not a valid wisp type");
        }
    }

    @Nonnull
    public static IWisp LoadWisp(IChunk chunk, CompoundNBT nbt)
    {
        String type = nbt.getString(WispConstants.WISP_TYPE_KEY);
        IWisp wisp = null;
        switch (type)
        {
            case WispConstants.BASIC_WISP: wisp = new BasicWisp(); break;
            default: throw new RuntimeException(type + " is not a valid wisp type");
        }
        wisp.Load(chunk, nbt);
        return wisp;
    }
}
