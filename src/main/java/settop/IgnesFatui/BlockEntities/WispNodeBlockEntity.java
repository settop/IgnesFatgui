package settop.IgnesFatui.BlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.WispNetwork.WispDataCache;
import settop.IgnesFatui.WispNetwork.WispNode;

public class WispNodeBlockEntity extends BlockEntity
{
    private WispNode node;
    private boolean entityFromLoad = false;

    public WispNodeBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(IgnesFatui.BlockEntities.WISP_NODE_BLOCK_ENTITY.get(), pos, blockState);
    }

    //called when the player adds this to the world
    public void onPlaced()
    {
        assert level != null;
        if(!level.isClientSide)
        {
            WispDataCache wispDataCache = WispDataCache.GetCache(level);
            node = wispDataCache.GetOrCreateWispNode(level.dimension(), getBlockPos());
            node.LinkToBlockEntity(this);
        }
    }

    //called when player remove this from the world
    public void onRemoved()
    {
        assert level != null;
        if(!level.isClientSide && node != null)
        {
            WispDataCache wispDataCache = WispDataCache.GetCache(level);
            wispDataCache.RemoveWispNode(node);
            node.UnlinkFromBlockEntity(this);
            node = null;
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider lookupProvider)
    {
        super.saveAdditional(tag, lookupProvider);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider lookupProvider)
    {
        super.loadAdditional(tag, lookupProvider);
        entityFromLoad = true;
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        assert level != null;
        if(!level.isClientSide && entityFromLoad)
        {
            WispDataCache wispDataCache = WispDataCache.GetCache(level);
            node = wispDataCache.GetOrCreateWispNode(level.dimension(), getBlockPos());
            node.LinkToBlockEntity(this);
        }
    }

    @Override
    public void onChunkUnloaded()
    {
        super.onChunkUnloaded();
        if(node != null)
        {
        }
    }

    //called when removed from world or unloaded
    @Override
    public void setRemoved()
    {
        super.setRemoved();
        if(node != null)
        {
            node.UnlinkFromBlockEntity(this);
        }
    }

    @Override
    public void clearRemoved()
    {
        super.clearRemoved();
        if(node != null)
        {
            node.LinkToBlockEntity(this);
        }
    }
}
