package settop.IgnesFatui.Wisps;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import settop.IgnesFatui.IgnesFatui;

import java.util.HashMap;

public class ChunkWisps
{
    private HashMap<BlockPos, IWisp> wispsInChunk = new HashMap<>();

    public synchronized CompoundNBT save()
    {
        if(wispsInChunk.isEmpty())
        {
            return null;
        }
        CompoundNBT nbt = new CompoundNBT();

        for( HashMap.Entry<BlockPos, IWisp> entry : wispsInChunk.entrySet() )
        {
            BlockPos pos = entry.getKey();
            String stringKey = String.format("%d,%d,%d",pos.getX(), pos.getY(), pos.getZ() );

            CompoundNBT wispNBT = entry.getValue().Save();
            nbt.put(stringKey, wispNBT);
        }

        return nbt;
    }

    public synchronized void load(IChunk inChunk, CompoundNBT nbt)
    {
        for(String stringKey : nbt.keySet())
        {
            String keyParts[] = stringKey.split(",");
            if(keyParts.length != 3)
            {
                IgnesFatui.LOGGER.warn("Error loading chunk wisp nbt: Key does not have 3 parts");
                continue;
            }
            BlockPos key = new BlockPos
                    (
                            Integer.parseInt(keyParts[0]),
                            Integer.parseInt(keyParts[1]),
                            Integer.parseInt(keyParts[2])
                    );
            IWisp loadedWisp = WispFactory.LoadWisp(inChunk, nbt.getCompound(stringKey));
            wispsInChunk.put(key, loadedWisp);
        }
    }

    public synchronized Tuple<IWisp, Boolean> GetOrCreateWisp(String type, IChunk inChunk, BlockPos inPos)
    {
        IWisp existingWisp = wispsInChunk.get(inPos);
        if(existingWisp != null)
        {
            return new Tuple(existingWisp, false);
        }
        else
        {
            IWisp newWisp = WispFactory.CreateNewWisp(type, inChunk, inPos);
            wispsInChunk.put(inPos, newWisp);
            return new Tuple(newWisp, true);
        }
    }

    public synchronized void BlockDestroyed(IWorld inWorld, BlockPos inPos)
    {
        IWisp existingWisp = wispsInChunk.get(inPos);
        if(existingWisp == null) return;

        existingWisp.DropItemStackIntoWorld(inWorld);
        wispsInChunk.remove(inPos);
    }
}
