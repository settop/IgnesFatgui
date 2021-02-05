package settop.IgnesFatui.TileEntities;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Wisps.ChunkWispData;
import settop.IgnesFatui.Wisps.WispNode;

public class WispConnectionNodeTileEntity extends TileEntity
{
    private WispNode node;

    public WispConnectionNodeTileEntity()
    {
        super(IgnesFatui.TileEntities.WISP_CONNECTION_NODE_TILE_ENTITY.get());
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        return super.write(compound);
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        if(!world.isRemote)
        {
            node = new WispNode(getPos(), 8);
            ChunkWispData.RegisterNode(world, node);
        }
    }

    @Override
    public void onChunkUnloaded()
    {
        super.onChunkUnloaded();
        if(node != null)
        {
            ChunkWispData.UnregisterNode(world, node);
        }
    }

    @Override
    public void remove()
    {
        super.remove();
        if(node != null)
        {
            ChunkWispData.UnregisterNode(world, node);
        }
    }


}
