package settop.IgnesFatui.Wisps;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;

import java.lang.ref.WeakReference;

public abstract class WispBase implements IWisp
{
    private WeakReference<IChunk> parentChunk;
    private int chunkDimension;
    private BlockPos pos;

    public WispBase()
    {
        //expect a load to follow
    }

    public WispBase(IChunk chunk, BlockPos inPos)
    {
        parentChunk = new WeakReference<IChunk>(chunk);
        chunkDimension = chunk.getWorldForge().func_234938_ad_();
        pos = inPos;
    }

    @Override
    public CompoundNBT Save()
    {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("pos", NBTUtil.writeBlockPos(pos));
        nbt.putString(WispConstants.WISP_TYPE_KEY, GetType());
        return nbt;
    }

    @Override
    public void Load(IChunk chunk, CompoundNBT nbt)
    {
        parentChunk = new WeakReference<IChunk>(chunk);
        chunkDimension = chunk.getWorldForge().func_234938_ad_();

        pos = NBTUtil.readBlockPos(nbt.getCompound("pos"));
    }

    @Override
    public WeakReference<IChunk> GetChunk()
    {
        return parentChunk;
    }

    @Override
    public int GetDim()
    {
        return chunkDimension;
    }

    @Override
    public BlockPos GetPos()
    {
        return pos;
    }
}
