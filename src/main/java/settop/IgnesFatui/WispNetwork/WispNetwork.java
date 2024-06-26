package settop.IgnesFatui.WispNetwork;

import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;


import settop.IgnesFatui.Utils.ItemStackKey;

public class WispNetwork
{
    private WispNode rootNode;
    private final TaskManager taskManager = new TaskManager();
    private final HashSet<WispNode> nodes = new HashSet<>();

    private final HashMap<ItemStackKey, WispNetworkItemSources> itemSourceMap = new HashMap<ItemStackKey, WispNetworkItemSources>();

    public WispNetwork(ResourceKey<Level> dimension, BlockPos pos)
    {
        this.rootNode = new WispNode(dimension, pos);
        nodes.add(rootNode);
        rootNode.OnConnectToNetwork(this);
    }

    private WispNetwork()
    {
        this.rootNode = null;
    }

    //public BlockPos GetClosestPos(BlockPos inPos)
    //{
    //    //the network is a 3x3x3 multiblock
    //    //so want to test to the closest block of the multiblock
    //    BlockPos offset = inPos.subtract(pos);
    //
    //    offset = new BlockPos(
    //            Mth.clamp(offset.getX(), -1, 1),
    //            Mth.clamp(offset.getY(), -1, 1),
    //            Mth.clamp(offset.getZ(), -1, 1)
    //    );
    //     return pos.offset(offset);
    //}

    public boolean TryConnectNodeToNetwork(WispNode node)
    {
        return rootNode.TryConnectToNode(node);
    }

    public void AddNode(WispNode node)
    {
        if(!nodes.add(node))
        {
            return;
        }
        node.OnConnectToNetwork(this);

        for(WispNode connectedNode : node.GetConnectedNodes())
        {
            if(connectedNode.GetConnectedNetwork() == null)
            {
                AddNode(connectedNode);
            }
        }
    }

    public void RemoveNode(WispNode node)
    {
        if(!nodes.remove(node))
        {
            return;
        }
        node.OnDisconnectFromNetwork(this);
    }

    public void AddTask(Task task)
    {
        taskManager.AddTask(task);
    }

    public void AddItemInventorySource(ItemStackKey stackKey, WispNetworkItemSources.InventoryItemSource itemSource)
    {
        WispNetworkItemSources itemSources = itemSourceMap.computeIfAbsent(stackKey, k->new WispNetworkItemSources());
        itemSources.AddItemSource(itemSource);
    }

    public void RemoveItemSource(ItemStackKey stackKey, WispNetworkItemSources.InventoryItemSource source)
    {
        WispNetworkItemSources sources = itemSourceMap.get(stackKey);
        sources.RemoveItemSource(source);
    }
}
