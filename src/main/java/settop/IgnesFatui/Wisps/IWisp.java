package settop.IgnesFatui.Wisps;

import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;

import java.lang.ref.WeakReference;

public interface IWisp
{
    CompoundNBT Save();
    void Load(IChunk chunk, CompoundNBT nbt);
    WeakReference<IChunk> GetChunk();
    int GetDim();
    BlockPos GetPos();
    String GetType();

    void DropItemStackIntoWorld(IWorld world);
    INamedContainerProvider GetContainerProvider();
    void InitFromTagData(CompoundNBT tagData);
}
