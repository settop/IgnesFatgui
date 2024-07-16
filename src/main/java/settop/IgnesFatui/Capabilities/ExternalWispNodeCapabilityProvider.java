package settop.IgnesFatui.Capabilities;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Utils.Utils;
import settop.IgnesFatui.WispNetwork.WispDataCache;
import settop.IgnesFatui.WispNetwork.WispNode;

public class ExternalWispNodeCapabilityProvider implements ICapabilitySerializable<CompoundTag>
{
    public static boolean IsValidItem(@NotNull ItemStack itemStack)
    {
        return itemStack.getItem() == IgnesFatui.Items.WISP_EXTERNAL_NODE.get();
    }
    public static class Cap
    {
        //have to store as the tag, as the save/load don't provide the registryAccess
        private Tag externalNodeItemData;
        private WispNode node;

        public boolean CanSetItem(@NotNull ItemStack itemStack)
        {
            return externalNodeItemData == null && itemStack.getItem() == IgnesFatui.Items.WISP_EXTERNAL_NODE.get();
        }

        public void SetItem(@NotNull HolderLookup.Provider registryAccess, @NotNull ItemStack itemStack)
        {
            externalNodeItemData = itemStack.save(registryAccess);
        }

        public ItemStack GetItem(@NotNull HolderLookup.Provider registryAccess)
        {
            if(externalNodeItemData == null)
            {
                return ItemStack.EMPTY;
            }
            return ItemStack.parse(registryAccess, externalNodeItemData).orElse(ItemStack.EMPTY);
        }

        public boolean HasItem()
        {
            return externalNodeItemData != null;
        }

        public void CreateNode(@NotNull WispDataCache cache, @NotNull BlockEntity attachedToEntity)
        {
            if(node != null)
            {
                IgnesFatui.LOGGER.warn("Creating external wisp node for block entity when it already exists, BlockEntity: {}", attachedToEntity.toString());
            }
            node = cache.GetOrCreateWispNode(attachedToEntity.getLevel().dimension(), attachedToEntity.getBlockPos());
            node.LinkToBlockEntity(attachedToEntity);
        }

        public WispNode GetNode()
        {
            return node;
        }
    }

    LazyOptional<Cap> cap = LazyOptional.of(Cap::new);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction)
    {
        if (capability == IgnesFatui.Capabilities.EXTERNAL_WISP_NODE_HANDLER)
        {
            return cap.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag capTag = new CompoundTag();
        cap.ifPresent((c)->
        {
            if(c.externalNodeItemData == null)
            {
                return;
            }
            CompoundTag nodeNBT = new CompoundTag();
            capTag.put("external_wisp_node", nodeNBT);

            nodeNBT.put("external_item", c.externalNodeItemData);
        });
        return capTag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        if(nbt.contains("external_wisp_node"))
        {
            cap.ifPresent((c)->
            {
                CompoundTag nodeNBT = nbt.getCompound("external_wisp_node");
                c.externalNodeItemData = nodeNBT.get("external_item");
            });
        }
    }

    @SubscribeEvent
    public static void AttachBlockEntityCaps(AttachCapabilitiesEvent<BlockEntity> attachCapabilities)
    {
        BlockEntityType<?> blockEntityType = attachCapabilities.getObject().getType();
        if (!IgnesFatui.BlockEntities.IsPartOfMod(blockEntityType))
        {
            attachCapabilities.addCapability(new ResourceLocation(IgnesFatui.MOD_ID, "external_wisp_node_handler"), new ExternalWispNodeCapabilityProvider());
        }
    }

    @SubscribeEvent
    public static void OnChunkLoad(ChunkEvent.Load chunkLoad)
    {
        Level level = (Level) chunkLoad.getLevel();
        if(level.isClientSide())
        {
            return;
        }
        ResourceKey<Level> dimension = level.dimension();
        WispDataCache wispCache = WispDataCache.GetCache(level);
        for(BlockPos pos : chunkLoad.getChunk().getBlockEntitiesPos())
        {
            BlockEntity be = chunkLoad.getChunk().getBlockEntity(pos);
            if(be == null)
            {
                continue;
            }
            be.getCapability(IgnesFatui.Capabilities.EXTERNAL_WISP_NODE_HANDLER).ifPresent((cap)->
            {
                if(cap.HasItem())
                {
                    cap.node = wispCache.GetOrCreateWispNode(dimension, pos);
                    cap.node.LinkToBlockEntity(be);
                }
            });
        }
    }

    @SubscribeEvent
    public static void OnChunkUnload(ChunkEvent.Unload chunkUnload)
    {
        if(chunkUnload.getLevel().isClientSide())
        {
            return;
        }
        for(BlockPos pos : chunkUnload.getChunk().getBlockEntitiesPos())
        {
            BlockEntity be = chunkUnload.getChunk().getBlockEntity(pos);
            if(be == null)
            {
                continue;
            }
            be.getCapability(IgnesFatui.Capabilities.EXTERNAL_WISP_NODE_HANDLER).ifPresent((cap)->
            {
                if(cap.node != null)
                {
                    cap.node.UnlinkFromBlockEntity(be);
                    cap.node = null;
                }
            });
        }
    }

    @SubscribeEvent
    public static void BlockBroken(BlockEvent.BreakEvent breakEvent)
    {
        if(breakEvent.getLevel().isClientSide())
        {
            return;
        }
        Level level = (Level) breakEvent.getLevel();
        BlockEntity brokenEntity = breakEvent.getLevel().getBlockEntity(breakEvent.getPos());
        if(brokenEntity == null)
        {
            return;
        }
        LazyOptional<Cap> capLazyOptional = brokenEntity.getCapability(IgnesFatui.Capabilities.EXTERNAL_WISP_NODE_HANDLER);
        if(!capLazyOptional.isPresent())
        {
            return;
        }
        Cap cap = capLazyOptional.resolve().get();
        if(cap.HasItem())
        {
            Utils.SpawnAsEntity(level, breakEvent.getPos(), cap.GetItem(level.registryAccess()));
            cap.externalNodeItemData = null;
        }
        if(cap.node != null)
        {
            WispDataCache.GetCache(level).RemoveWispNode(cap.node);
            cap.node = null;
        }
    }
}
