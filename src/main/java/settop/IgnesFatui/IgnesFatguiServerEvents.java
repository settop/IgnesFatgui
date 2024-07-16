package settop.IgnesFatui;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import settop.IgnesFatui.Capabilities.ExternalWispNodeCapabilityProvider;
import settop.IgnesFatui.WispNetwork.WispDataCache;

import java.util.ArrayList;
import java.util.HashMap;

public class IgnesFatguiServerEvents
{
    public interface ChunkListener
    {
        void OnChunkLoad(ChunkEvent.Load chunkLoad);
        void OnChunkUnload(ChunkEvent.Unload chunkUnload);
    }

    private record RecentChunkLoad(ResourceKey<Level> dim, ChunkPos chunkPos) {

    }

    private static final HashMap<ResourceKey<Level>, HashMap<ChunkPos, ArrayList<ChunkListener>>> chunkListeners = new HashMap<>();
    private static final ArrayList<RecentChunkLoad> recentChunkLoads = new ArrayList<>();
    private static int timeSinceLastChunkLoad = 0;

    public static void AddChunkListener(ResourceKey<Level> dimension, BlockPos pos, int range, ChunkListener listener)
    {
        int minChunkX = SectionPos.blockToSectionCoord(pos.getX() - range);
        int maxChunkX = SectionPos.blockToSectionCoord(pos.getX() + range);
        int minChunkZ = SectionPos.blockToSectionCoord(pos.getZ() - range);
        int maxChunkZ = SectionPos.blockToSectionCoord(pos.getZ() + range);

        for(int x = minChunkX; x <= maxChunkX; ++x)
        {
            for(int z = minChunkZ; z <= maxChunkZ; ++z)
            {
                ChunkPos chunkPos = new ChunkPos(x, z);
                var listeners = chunkListeners.computeIfAbsent(dimension, (k)->new HashMap<>()).computeIfAbsent(chunkPos, (k)-> new ArrayList<>());
                if(!listeners.contains(listener))
                {
                    listeners.add(listener);
                }
            }
        }
    }

    public static void RemoveChunkListener(ResourceKey<Level> dimension, BlockPos pos, int range, ChunkListener listener)
    {
        int minChunkX = SectionPos.blockToSectionCoord(pos.getX() - range);
        int maxChunkX = SectionPos.blockToSectionCoord(pos.getX() + range);
        int minChunkZ = SectionPos.blockToSectionCoord(pos.getZ() - range);
        int maxChunkZ = SectionPos.blockToSectionCoord(pos.getZ() + range);

        for(int x = minChunkX; x <= maxChunkX; ++x)
        {
            for(int z = minChunkZ; z <= maxChunkZ; ++z)
            {
                ChunkPos chunkPos = new ChunkPos(x, z);
                chunkListeners.computeIfPresent(dimension, (k, listenersMap)->
                {
                    listenersMap.computeIfPresent(chunkPos, (k2, listeners)->
                    {
                        listeners.remove(listener);
                        return listeners.isEmpty() ? null : listeners;
                    });
                    return listenersMap.isEmpty() ? null : listenersMap;
                });
            }
        }
    }

    @SubscribeEvent
    public static void OnChunkLoad(ChunkEvent.Load chunkLoad)
    {
        if(chunkLoad.getLevel().isClientSide())
        {
            return;
        }
        Level level = (Level) chunkLoad.getLevel();
        ResourceKey<Level> dimension = level.dimension();
        ChunkPos loadedChunkPos = chunkLoad.getChunk().getPos();
        var listenersMap = chunkListeners.get(dimension);
        if(listenersMap != null)
        {
            ArrayList<ChunkListener> listeners = listenersMap.get(loadedChunkPos);
            if(listeners != null)
            {
                for (ChunkListener listener : listeners)
                {
                    listener.OnChunkLoad(chunkLoad);
                }
            }
        }
        recentChunkLoads.add(new RecentChunkLoad(dimension, chunkLoad.getChunk().getPos()));
        timeSinceLastChunkLoad = 0;
    }

    @SubscribeEvent
    public static void OnChunkUnload(ChunkEvent.Unload chunkUnload)
    {
        if(chunkUnload.getLevel().isClientSide())
        {
            return;
        }
        Level level = (Level) chunkUnload.getLevel();
        ResourceKey<Level> dimension = level.dimension();
        ChunkPos loadedChunkPos = chunkUnload.getChunk().getPos();
        var listenersMap = chunkListeners.get(dimension);
        if(listenersMap != null)
        {
            ArrayList<ChunkListener> listeners = listenersMap.get(loadedChunkPos);
            if(listeners != null)
            {
                for (ChunkListener listener : listeners)
                {
                    listener.OnChunkUnload(chunkUnload);
                }
            }
        }
    }

    @SubscribeEvent
    public static void ServerTick(TickEvent.ServerTickEvent tickEvent)
    {
        if(recentChunkLoads.isEmpty())
        {
            return;
        }
        ++timeSinceLastChunkLoad;
        if(timeSinceLastChunkLoad < 2)
        {
            //want to delay 2 ticks to give time for block entities to load and claim their nodes which happens on the first tick
            return;
        }
        WispDataCache wispCache = WispDataCache.GetCache(tickEvent.getServer().overworld());
        for(RecentChunkLoad recentChunk : recentChunkLoads)
        {
            wispCache.RemoveUnlinkedNodesFromChunk(recentChunk.dim, recentChunk.chunkPos);
        }
        recentChunkLoads.clear();
    }


}
