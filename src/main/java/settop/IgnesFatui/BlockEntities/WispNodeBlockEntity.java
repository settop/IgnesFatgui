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
import net.minecraftforge.event.level.ChunkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import settop.IgnesFatui.IgnesFatguiServerEvents;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Menu.WispNodeMenu;
import settop.IgnesFatui.Utils.Utils;
import settop.IgnesFatui.WispNetwork.WispDataCache;
import settop.IgnesFatui.WispNetwork.WispNode;

import java.util.ArrayList;

public class WispNodeBlockEntity extends BlockEntity implements IgnesFatguiServerEvents.ChunkListener, MenuProvider
{
    public enum CannotConnectReason
    {
        NO_BOUND_POSITION,
        BOUND_POSITION_IS_NOT_VALID,
        OUT_OF_RANGE,
        LINE_OF_SLIGHT_BLOCKED,
        NODE_CONNECTED_ELSEWHERE;

        public int BitField() { return 1 << ordinal(); }
    }
    static class ExternalNodeConnection
    {
        ItemStack item = ItemStack.EMPTY;
        WispNode connectedNode;
        int cannotConnectReasons = 0;

        void RemoveNode(WispDataCache wispCache)
        {
            if(connectedNode != null)
            {
                wispCache.RemoveWispNode(connectedNode);
                connectedNode = null;
            }
        }
    }
    public class NodeInventory implements Container
    {
        private final int size;
        private final ArrayList<ExternalNodeConnection> items;

        public static boolean CanPlaceItem(@NotNull ItemStack stack)
        {
            return stack.getItem() == IgnesFatui.Items.WISP_EXTERNAL_NODE.get();
        }

        public NodeInventory(int size)
        {
            this.size = size;
            this.items = new ArrayList<>(size);
            for(int i = 0; i < size; ++i)
            {
                this.items.add(new ExternalNodeConnection());
            }
        }

        @Override
        public boolean canPlaceItem(int slot, @NotNull ItemStack stack)
        {
            return CanPlaceItem(stack);
        }

        @Override
        public int getContainerSize()
        {
            return size;
        }

        @Override
        public boolean isEmpty()
        {
            for(ExternalNodeConnection connection : items)
            {
                if(!connection.item.isEmpty())
                {
                    return false;
                }
            }
            return true;
        }

        @Override
        public @NotNull ItemStack getItem(int slot)
        {
            return slot >= 0 && slot < items.size() ? items.get(slot).item : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack removeItem(int slot, int amount)
        {
            if(slot >= 0 && slot < items.size())
            {
                ExternalNodeConnection connection = items.get(slot);
                ItemStack itemStack = connection.item;
                connection.item = ItemStack.EMPTY;
                setChanged();
                return itemStack;
            }

            return ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack removeItemNoUpdate(int slot)
        {
            if(slot >= 0 && slot < items.size())
            {
                ExternalNodeConnection connection = items.get(slot);
                ItemStack itemStack = connection.item;
                connection.item = ItemStack.EMPTY;
                return itemStack;
            }

            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, @NotNull ItemStack stack)
        {
            if(slot >= 0 && slot < items.size())
            {
                ExternalNodeConnection connection = items.get(slot);
                connection.item = stack.split(1);
                setChanged();
            }
        }

        @Override
        public void setChanged()
        {
            UpdateLinksFromContents();
            WispNodeBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(@NotNull Player player)
        {
            return Container.stillValidBlockEntity(WispNodeBlockEntity.this, player);
        }

        @Override
        public void clearContent()
        {
            for(ExternalNodeConnection connection : items)
            {
                connection.item = ItemStack.EMPTY;
            }
            setChanged();
        }

        @Override
        public int getMaxStackSize()
        {
            return 1;
        }

        public int GetCannotConnectReason(int slot)
        {
            return items.get(slot).cannotConnectReasons;
        }
    }

    private WispNode node;
    private boolean entityFromLoad = false;
    private int invSize = 2;
    private NodeInventory inv;//null on client side
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
            inv = new NodeInventory(invSize);
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
            for(ExternalNodeConnection connection : inv.items)
            {
                connection.RemoveNode(wispDataCache);
                if(!connection.item.isEmpty())
                {
                    Utils.SpawnAsEntity(level, getBlockPos(), connection.item);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider lookupProvider)
    {
        super.saveAdditional(tag, lookupProvider);
        ListTag invTag = new ListTag();
        tag.put("inv", invTag);
        for(ExternalNodeConnection connection : inv.items)
        {
            CompoundTag connectionTag =  new CompoundTag();
            invTag.add(connectionTag);
            if(!connection.item.isEmpty())
            {
                connectionTag.put("item", connection.item.save(lookupProvider));
            }
            connectionTag.putInt("cannot_connect_reasons", connection.cannotConnectReasons);
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider lookupProvider)
    {
        super.loadAdditional(tag, lookupProvider);
        entityFromLoad = true;
        ListTag invTag = tag.getList("inv", ListTag.TAG_COMPOUND);
        inv = new NodeInventory(invSize);
        for(int i = 0; i < inv.size; ++i)
        {
            if(i >= invTag.size())
            {
                break;
            }
            CompoundTag connectionTag = invTag.getCompound(i);
            ExternalNodeConnection connection = inv.items.get(i);
            if(connectionTag.contains("item"))
            {
                connection.item = ItemStack.parseOptional(lookupProvider, connectionTag.getCompound("item"));
            }
            connection.cannotConnectReasons = connectionTag.getInt("cannot_connect_reasons");
        }
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
                UpdateLinksFromContents();
            }
            IgnesFatguiServerEvents.AddChunkListener(level.dimension(), getBlockPos(), maxRange, this);
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
            IgnesFatguiServerEvents.RemoveChunkListener(level.dimension(), getBlockPos(), maxRange, this);
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
            IgnesFatguiServerEvents.AddChunkListener(level.dimension(), getBlockPos(), maxRange, this);
        }
    }

    public void OnChunkLoad(ChunkEvent.Load chunkLoad)
    {
        if(inv == null)
        {
            return;
        }
        UpdateLinksFromContents();
    }

    public void OnChunkUnload(ChunkEvent.Unload chunkUnload)
    {
        if(inv == null)
        {
            return;
        }
        for(ExternalNodeConnection connection : inv.items)
        {
            if(connection.connectedNode != null)
            {
                if(chunkUnload.getChunk().getPos().equals(new ChunkPos(connection.connectedNode.pos)))
                {
                    BlockEntity connectedEntity = chunkUnload.getChunk().getBlockEntity(connection.connectedNode.pos);
                    assert connectedEntity != null;
                    connection.connectedNode.UnlinkFromBlockEntity(connectedEntity);
                }
            }
        }
    }

    private void UpdateLinksFromContents()
    {
        if(level == null || level.isClientSide())
        {
            return;
        }

        boolean anyChanges = false;
        for(int i = 0; i < inv.size; ++i)
        {
            ExternalNodeConnection connection = inv.items.get(i);
            connection.cannotConnectReasons = 0;
            if(connection.item.isEmpty() && connection.connectedNode == null)
            {
                continue;
            }
            WispDataCache wispDataCache = WispDataCache.GetCache(level);
            if(connection.item.isEmpty())
            {
                wispDataCache.RemoveWispNode(connection.connectedNode);
                connection.connectedNode = null;
                continue;
            }
            GlobalPos boundPos = connection.item.get(IgnesFatui.DataComponents.BOUND_GLOBAL_POS.get());
            if(boundPos == null)
            {
                connection.cannotConnectReasons |= CannotConnectReason.NO_BOUND_POSITION.BitField();
                connection.RemoveNode(wispDataCache);
                continue;
            }
            if(connection.connectedNode != null &&
                    (!connection.connectedNode.dimension.equals(boundPos.dimension()) || !connection.connectedNode.pos.equals(boundPos.pos())))
            {
                //item has changed it's bound position, so clear the old node
                connection.RemoveNode(wispDataCache);
            }

            if(!level.dimension().equals(boundPos.dimension()))
            {
                connection.cannotConnectReasons |= CannotConnectReason.OUT_OF_RANGE.BitField();
            }
            else if(getBlockPos().distManhattan(boundPos.pos()) > maxRange)
            {
                connection.cannotConnectReasons |= CannotConnectReason.OUT_OF_RANGE.BitField();
            }

            BlockEntity boundBlockEntity = level.getBlockEntity(boundPos.pos());
            if(boundBlockEntity == null && level.hasChunkAt(boundPos.pos()))
            {
                connection.cannotConnectReasons |= CannotConnectReason.BOUND_POSITION_IS_NOT_VALID.BitField();
            }

            //ToDo: test line of sight

            if(connection.cannotConnectReasons != 0)
            {
                //blocked for some reason
                connection.RemoveNode(wispDataCache);
            }
            else if(connection.connectedNode == null)
            {
                //see if we can connect this node
                WispNode existingNode = wispDataCache.GetWispNode(boundPos.dimension(), boundPos.pos());
                if(existingNode != null)
                {
                    connection.cannotConnectReasons |= CannotConnectReason.NODE_CONNECTED_ELSEWHERE.BitField();
                }
                else
                {
                    connection.connectedNode = wispDataCache.GetOrCreateWispNode(boundPos.dimension(), boundPos.pos());
                    if(boundBlockEntity != null)
                    {
                        connection.connectedNode.LinkToBlockEntity(boundBlockEntity);
                    }
                    node.TryConnectToNode(connection.connectedNode);
                }
            }
            //else the existing node should still be valid
        }
    }

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
}
