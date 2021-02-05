package settop.IgnesFatui.Wisps;

import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public abstract class WispBase implements INamedContainerProvider
{
    private WeakReference<IChunk> parentChunk;
    private ResourceLocation chunkDimension;
    private BlockPos pos;
    public ArrayList<WispNode> connections;

    public WispBase()
    {
        //expect a load to follow
    }

    public WispBase(IChunk chunk, BlockPos inPos)
    {
        parentChunk = new WeakReference<IChunk>(chunk);
        chunkDimension = ((World)chunk.getWorldForge()).getDimensionKey().getLocation();
        pos = inPos;
        connections = new ArrayList<>();
    }

    public void RemoveFromWorld()
    {
       while (!connections.isEmpty())
       {
           connections.get(0).DisonnectWisp(this);
       }
    }

    public CompoundNBT Save()
    {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("pos", NBTUtil.writeBlockPos(pos));
        nbt.putString(WispConstants.WISP_TYPE_KEY, GetType());
        ListNBT connectionsNBT = new ListNBT();

        return nbt;
    }

    public void Load(IChunk chunk, CompoundNBT nbt)
    {
        parentChunk = new WeakReference<IChunk>(chunk);
        chunkDimension = ((World)chunk.getWorldForge()).getDimensionKey().getLocation();
        connections = new ArrayList<>();

        pos = NBTUtil.readBlockPos(nbt.getCompound("pos"));
    }

    public WeakReference<IChunk> GetChunk()
    {
        return parentChunk;
    }

    public ResourceLocation GetDim()
    {
        return chunkDimension;
    }

    public BlockPos GetPos()
    {
        return pos;
    }

    public abstract String GetType();
    public abstract void DropItemStackIntoWorld(IWorld world);
    public abstract void InitFromTagData(CompoundNBT tagData);
    public abstract void UpdateFromContents();
    public abstract void ContainerExtraDataWriter(PacketBuffer packetBuffer);
}
