package settop.IgnesFatui.Wisps;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import settop.IgnesFatui.IgnesFatui;

import java.util.concurrent.ConcurrentHashMap;

public class ChunkWispData
{
    private static class DimensionData
    {
        public ConcurrentHashMap<ChunkPos, ChunkWisps> chunkData = new ConcurrentHashMap<>();
    }
    private static ConcurrentHashMap<Integer, DimensionData> dimensionData = new ConcurrentHashMap<>();

    public static ChunkWisps GetChunkWisps(IWorld inWorld, BlockPos blockPos)
    {
        if(inWorld.isRemote())
        {
            throw new RuntimeException("Trying to get chunk data on client");
        }
        IChunk chunk = inWorld.getChunk(blockPos);
        if(chunk == null)
        {
            throw new RuntimeException("Trying to get an unloaded chunk");
        }

        int dimension = inWorld.func_234938_ad_();
        DimensionData dimData = dimensionData.computeIfAbsent(dimension, (key)->new DimensionData());
        return dimData.chunkData.computeIfAbsent(chunk.getPos(), (key)->new ChunkWisps());
    }

    /**
     * Get's the wisp at the given position in the world if there is one
     * Else creates a new wisp of the supplied type
     *
     * Return's the wisp and a boolean indicating if the wisp is a newly created one
     **/
    public static Tuple<IWisp, Boolean> GetOrCreateWisp(String type, World inWorld, BlockPos inPos, CompoundNBT tagData)
    {
        if(inWorld.isRemote())
        {
            throw new RuntimeException("Trying to get chunk data on client");
        }
        IChunk chunk = inWorld.getChunk(inPos);
        if(chunk == null)
        {
            throw new RuntimeException("Trying to get an unloaded chunk");
        }

        int dimension = inWorld.func_234938_ad_();
        DimensionData dimData = dimensionData.computeIfAbsent(dimension, (key)->new DimensionData());
        ChunkWisps chunkWisps = dimData.chunkData.computeIfAbsent(chunk.getPos(), (key)->new ChunkWisps());

        return chunkWisps.GetOrCreateWisp(type, chunk, inPos, tagData);
    }

    @Mod.EventBusSubscriber( modid = IgnesFatui.MOD_ID)
    public static class EventListener
    {
        @SubscribeEvent
        public static void OnChunkLoad(ChunkDataEvent.Load loadEvent)
        {
            if(loadEvent.getStatus() != ChunkStatus.Type.LEVELCHUNK) return;
            if(loadEvent.getWorld().isRemote()) return;
            if(!loadEvent.getData().contains(IgnesFatui.MOD_ID))
            {
                return;
            }
            CompoundNBT chunkLoad = loadEvent.getData().getCompound(IgnesFatui.MOD_ID);

            ChunkWisps newChunkWisps = new ChunkWisps();
            newChunkWisps.load(loadEvent.getChunk(), chunkLoad);

            int dimension = loadEvent.getWorld().func_234938_ad_();
            DimensionData dimData = dimensionData.computeIfAbsent(dimension, (key)->new DimensionData());
            dimData.chunkData.put(loadEvent.getChunk().getPos(), newChunkWisps);
        }

        @SubscribeEvent
        public static void OnChunkSave(ChunkDataEvent.Save saveEvent)
        {
            if(saveEvent.getWorld().isRemote()) return;

            int dimension = saveEvent.getWorld().func_234938_ad_();
            DimensionData dimData = dimensionData.get(dimension);

            if(dimData != null)
            {
                ChunkWisps chunkWisps = dimData.chunkData.get(saveEvent.getChunk().getPos());
                if(chunkWisps != null)
                {
                    CompoundNBT data = saveEvent.getData();
                    CompoundNBT chunkSave = chunkWisps.save();
                    if(chunkSave != null)
                    {
                        data.put(IgnesFatui.MOD_ID, chunkSave);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void OnWorldUnload(WorldEvent.Unload unloadEvent)
        {
            if(unloadEvent.getWorld().isRemote()) return;

            int dimension = unloadEvent.getWorld().func_234938_ad_();
            DimensionData dimData = dimensionData.get(dimension);

            if(dimData != null)
            {
                dimensionData.remove(dimension);
            }
        }

        @SubscribeEvent
        public static void OnNeighborUpdate(BlockEvent.NeighborNotifyEvent updateEvent)
        {
            if(updateEvent.getWorld().isRemote()) return;

            if(updateEvent.getState().getBlock() != Blocks.AIR)
            {
                //we only care about blocks being removed, i.e. set to air
                return;
            }

            int dimension = updateEvent.getWorld().func_234938_ad_();
            DimensionData dimData = dimensionData.get(dimension);
            if(dimData == null) return;

            BlockPos pos = updateEvent.getPos();
            ChunkPos chunkPos = new ChunkPos( pos.getX() >> 4, pos.getZ() >> 4 );

            ChunkWisps chunkWisps = dimData.chunkData.get( chunkPos );
            if(chunkWisps == null) return;

            chunkWisps.BlockDestroyed(updateEvent.getWorld(), pos);
        }
    }
}
