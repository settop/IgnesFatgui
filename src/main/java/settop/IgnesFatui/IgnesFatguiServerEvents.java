package settop.IgnesFatui;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;

public class IgnesFatguiServerEvents
{
    public interface ChunkListener
    {
        void OnChunkLoad(ChunkEvent.Load chunkLoad);
        void OnChunkUnload(ChunkEvent.Unload chunkUnload);
    }

    private static final HashMap<ResourceKey<Level>, HashMap<ChunkPos, ArrayList<ChunkListener>>> chunkListeners = new HashMap<>();

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
        ResourceKey<Level> dimension = ((Level)chunkLoad.getLevel()).dimension();
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
    }

    @SubscribeEvent
    public static void OnChunkUnload(ChunkEvent.Unload chunkUnload)
    {
        if(chunkUnload.getLevel().isClientSide())
        {
            return;
        }
        ResourceKey<Level> dimension = ((Level)chunkUnload.getLevel()).dimension();
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
}
