package settop.IgnesFatui.WispNetwork;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import settop.IgnesFatui.Wisps.WispBase;
import settop.IgnesFatui.Wisps.WispNode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class WispNetwork
{
    private class ItemSources
    {
        private int countCache = 0;
        private boolean craftable = false;
        private boolean isDirty = false;

        private class WispItemSource
        {
            public WeakReference<WispBase> sourceWisp;
            public int wispCountCache = 0;
            public boolean wispCraftCache = false;
        }

        private ArrayList<WispItemSource> itemSources;
    }

    public final BlockPos pos;

    private HashMap<ItemStack, ItemSources> itemSourceMap;

    private HashSet<WispNode> nodes = new HashSet<>();
    private HashMap<WispBase, Integer> wisps = new HashMap<>();

    public WispNetwork(BlockPos pos)
    {
        this.pos = pos;
    }

    public BlockPos GetClosestPos(BlockPos inPos)
    {
        //the network is a 3x3x3 multiblock
        //so want to test to the closest block of the multiblock
        BlockPos offset = inPos.subtract(pos);

        offset = new BlockPos(
                MathHelper.clamp(offset.getX(), -1, 1),
                MathHelper.clamp(offset.getY(), -1, 1),
                MathHelper.clamp(offset.getZ(), -1, 1)
        );
        return pos.add(offset);
    }

    public void AddNode(WispNode node)
    {
        if(!nodes.add(node))
        {
            return;
        }
        for(WispBase wisp : node.connectedWisps)
        {
            wisps.compute(wisp, (key, v)-> v != null ? (v + 1) : 1);
        }
    }

    public void AddWispNodeConnection(WispNode node, WispBase wisp)
    {
        wisps.compute(wisp, (key, v)-> v != null ? (v + 1) : 1);
    }

    public void RemoveWispNodeConnection(WispNode node, WispBase wisp)
    {
        wisps.compute(wisp, (key, v)-> v > 1 ? v - 1 : null);
    }

    public void RemoveNode(WispNode node)
    {
        nodes.remove(node);

        for(WispBase wisp : node.connectedWisps)
        {
            wisps.compute(wisp, (key, v)-> v > 1 ? v - 1 : null);
        }
    }
}
