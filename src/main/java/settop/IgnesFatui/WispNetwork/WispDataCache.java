package settop.IgnesFatui.WispNetwork;

import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.IgnesFatui;

import java.util.HashMap;
import java.util.Optional;

public class WispDataCache extends SavedData
{
    private static class DimensionData
    {
        private final HashMap<BlockPos, WispNetwork> networks = new HashMap<>();
        private final HashMap<BlockPos, WispNode> nodes = new HashMap<>();
    }

    private static final SavedData.Factory<WispDataCache> factory = new Factory<>(WispDataCache::Create, WispDataCache::Load, null);
    private static final String DATA_NAME = "sif1_wisp_data";

    private final HashMap<ResourceKey<Level>, DimensionData> dimensionDataMap = new HashMap<>();

    private static WispDataCache Create()
    {
        return new WispDataCache();
    }

    private static WispDataCache Load(@NotNull CompoundTag compoundTag, @NotNull HolderLookup.Provider provider)
    {
        WispDataCache cache = new WispDataCache();

        ListTag dimensionDataListTag = compoundTag.getList("dimension_data", Tag.TAG_COMPOUND);
        for(Tag dimensionTagElement : dimensionDataListTag)
        {
            CompoundTag dimensionTag = (CompoundTag)dimensionTagElement;
            try
            {
                ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.read(dimensionTag.getString("dimension_key")).getOrThrow());
                DimensionData dimensionData = new DimensionData();
                cache.dimensionDataMap.put(dim, dimensionData);

                ListTag nodesTag = dimensionTag.getList("nodes", Tag.TAG_COMPOUND);
                for(Tag nodeTagElement : nodesTag)
                {
                    CompoundTag nodeTag = (CompoundTag)nodeTagElement;
                    Optional<BlockPos> pos = NbtUtils.readBlockPos(nodeTag, "pos");
                    if(pos.isEmpty())
                    {
                        IgnesFatui.LOGGER.error("Failed to read block pos for a wisp node");
                        continue;
                    }
                    WispNode node = new WispNode(dim, pos.get());
                    dimensionData.nodes.put(pos.get(), node);
                }
            }
            catch (RuntimeException ex)
            {
                IgnesFatui.LOGGER.error("Error loading wisp dimension data %s".formatted(ex.getMessage()));
            }
        }

        return cache;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag, @NotNull HolderLookup.Provider provider)
    {
        ListTag dimensionDataListTag = new ListTag();
        for (var dimensionEntry : dimensionDataMap.entrySet())
        {
            CompoundTag dimensionTag = new CompoundTag();

            dimensionTag.putString("dimension_key", dimensionEntry.getKey().location().toString());

            ListTag nodesTag = new ListTag();
            for(var nodeEntry : dimensionEntry.getValue().nodes.entrySet())
            {
                CompoundTag nodeTag = new CompoundTag();
                nodeTag.put("pos", NbtUtils.writeBlockPos(nodeEntry.getKey()));
                nodesTag.add(nodeTag);
            }
            dimensionTag.put("nodes", nodesTag);

            dimensionDataListTag.add(dimensionTag);
        }

        compoundTag.put("dimension_data", dimensionDataListTag);
        return compoundTag;
    }

    @Override
    public boolean isDirty()
    {
        return true;
    }

    static public WispDataCache GetCache(@NotNull Level level)
    {
        //ensure that the cache is always loade by putting it all on the overworld
        return level.getServer().overworld().getDataStorage().computeIfAbsent(factory, DATA_NAME);
    }

    public WispNode GetOrCreateWispNode(@NotNull ResourceKey<Level> dim, @NotNull BlockPos pos)
    {
        DimensionData dimensionData = dimensionDataMap.computeIfAbsent(dim, (k)->new DimensionData());
        return dimensionData.nodes.computeIfAbsent(pos, (k)->new WispNode(dim, pos));
    }

    public void RemoveWispNode(@NotNull WispNode node)
    {
        node.DisconnectAll();
        DimensionData dimensionData = dimensionDataMap.get(node.GetDimension());
        if(dimensionData == null)
        {
            return;
        }
        dimensionData.nodes.remove(node.GetPos());
    }
}
