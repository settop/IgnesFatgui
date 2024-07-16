package settop.IgnesFatui.BlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.level.ChunkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import settop.IgnesFatui.Capabilities.ExternalWispNodeCapabilityProvider;
import settop.IgnesFatui.IgnesFatguiServerEvents;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Menu.WispNodeMenu;
import settop.IgnesFatui.Utils.Utils;
import settop.IgnesFatui.WispNetwork.WispDataCache;
import settop.IgnesFatui.WispNetwork.WispNode;

import java.util.ArrayList;

public class WispNodeBlockEntity extends BlockEntity
{
    public enum CannotConnectReason
    {
        OUT_OF_RANGE,
        LINE_OF_SLIGHT_BLOCKED,
        INVALID_CONNECTION_TARGET,
        NODE_CONNECTED_ELSEWHERE;

        public int BitField() { return 1 << ordinal(); }
    }
    //ToDo: Add a way of connecting/disconnecting via a menu accessed via the node
    private WispNode node;
    private boolean entityFromLoad = false;
    private int invSize = 2;
    private int maxRange = 8;
    private boolean doneLoad = false;

    public WispNodeBlockEntity(BlockPos pos, BlockState blockState)
    {
        super(IgnesFatui.BlockEntities.WISP_NODE_BLOCK_ENTITY.get(), pos, blockState);
    }

    public WispNode GetWispNode() { return node; }

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

    //called when entity removed from the world(but not an unload)
    public void onRemoved()
    {
        assert level != null;
        if(!level.isClientSide)
        {
            WispDataCache wispDataCache = WispDataCache.GetCache(level);
            if(node != null)
            {
                wispDataCache.RemoveWispNode(node);
                node.UnlinkFromBlockEntity(this);
                node = null;
            }
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
        if(!level.isClientSide)
        {
            if(entityFromLoad)
            {
                WispDataCache wispDataCache = WispDataCache.GetCache(level);
                node = wispDataCache.GetOrCreateWispNode(level.dimension(), getBlockPos());
                node.LinkToBlockEntity(this);
            }
        }
        doneLoad = true;
    }

    //called when removed from world or unloaded
    @Override
    public void setRemoved()
    {
        super.setRemoved();
        if(!doneLoad)
        {
            return;
        }
        assert level != null;
        if(!level.isClientSide)
        {
            if (node != null)
            {
                node.UnlinkFromBlockEntity(this);
            }
        }
    }

    @Override
    public void clearRemoved()
    {
        super.clearRemoved();
        if(!doneLoad)
        {
            return;
        }
        assert level != null;
        if(!level.isClientSide)
        {
            if (node != null)
            {
                node.LinkToBlockEntity(this);
            }
        }
    }

    public int GetCannotConnectReasons(BlockEntity otherBLockEntity)
    {
        int cannotConnectReasons = 0;
        if(!level.dimension().equals(otherBLockEntity.getLevel().dimension()))
        {
            cannotConnectReasons |= CannotConnectReason.OUT_OF_RANGE.BitField();
        }
        else if(getBlockPos().distManhattan(otherBLockEntity.getBlockPos()) > maxRange)
        {
            cannotConnectReasons |= CannotConnectReason.OUT_OF_RANGE.BitField();
        }

        //ToDo: test line of sight
        LazyOptional<ExternalWispNodeCapabilityProvider.Cap> externalNodeCapLazyOptional = otherBLockEntity.getCapability(IgnesFatui.Capabilities.EXTERNAL_WISP_NODE_HANDLER);
        if(externalNodeCapLazyOptional.isPresent())
        {
            ExternalWispNodeCapabilityProvider.Cap externalNodeCap = externalNodeCapLazyOptional.resolve().get();
            if(externalNodeCap.GetNode() != null && !externalNodeCap.GetNode().GetConnectedNodes().isEmpty())
            {
                cannotConnectReasons |= CannotConnectReason.NODE_CONNECTED_ELSEWHERE.BitField();
            }
        }
        else
        {
            cannotConnectReasons |= CannotConnectReason.INVALID_CONNECTION_TARGET.BitField();
        }

        return cannotConnectReasons;
    }

    /*
    public void OpenMenu(ServerPlayer serverPlayer)
    {
        serverPlayer.openMenu(this, (buffer)-> WispNodeMenu.WriteToBuffer(buffer, inv.size));
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerID, @NotNull Inventory inventory, @NotNull Player player)
    {
        return WispNodeMenu.CreateMenuServer(containerID, inventory, inv);
    }

    @Override
    public @NotNull Component getDisplayName()
    {
        return Component.translatable("block.sif1.wisp_node");
    }

     */
}
