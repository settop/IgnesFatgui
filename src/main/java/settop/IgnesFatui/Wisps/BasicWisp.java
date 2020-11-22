package settop.IgnesFatui.Wisps;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Items.BasicWispItem;
import settop.IgnesFatui.Utils.Utils;

import java.lang.ref.WeakReference;

public class BasicWisp extends WispBase
{
    public BasicWisp()
    {
        super();
    }

    public BasicWisp(IChunk chunk, BlockPos inPos)
    {
        super(chunk, inPos);
    }

    @Override
    public CompoundNBT Save()
    {
        CompoundNBT nbt = super.Save();
        return nbt;
    }

    @Override
    public void Load(IChunk chunk, CompoundNBT nbt)
    {
        super.Load(chunk, nbt);
    }

    @Override
    public String GetType()
    {
        return WispConstants.BASIC_WISP;
    }

    @Override
    public void DropItemStackIntoWorld(IWorld world)
    {
        ItemStack droppedStack = new ItemStack(IgnesFatui.RegistryHandler.WISP_ITEM.get(), 1);
        Utils.SpawnAsEntity((World)world, GetPos(), droppedStack );
    }
}
