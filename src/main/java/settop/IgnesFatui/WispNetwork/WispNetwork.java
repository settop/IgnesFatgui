package settop.IgnesFatui.WispNetwork;

import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;


import org.jetbrains.annotations.NotNull;
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

    public void Tick()
    {
        //ToDo: make the maxProportionTasksToTick to be changeable at runtime
        taskManager.Tick(0.1f);
    }

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
        if(sources.IsEmpty())
        {
            itemSourceMap.remove(stackKey);
        }
    }

    //ToDo: Need to add a way to fetch items with a filter
    public WispNetworkItemSources FindItemSource(ItemStackKey stackKey)
    {
        return itemSourceMap.get(stackKey);
    }

    float CalculateTravelCost(WispNode from, WispNode to)
    {
        return (float)Math.sqrt(from.GetPos().distSqr(to.GetPos())) / from.GetSpeed();
    }

    float EstimateRemainingCost(WispNode from, WispNode to, float maxSpeed)
    {
        WispNode.DestinationData fromDestinationData = from.GetPathData().nextNodeToDestinations.get(to);
        if(fromDestinationData != null)
        {
            return fromDestinationData.pathCost();
        }
        else
        {
            return (float)Math.sqrt(from.GetPos().distSqr(to.GetPos())) / maxSpeed;
        }
    }

    public boolean TryBuildPathfindingBetweenNodes(WispNode from, WispNode to)
    {
        if(from.GetConnectedNetwork() != this || to.GetConnectedNetwork() != this)
        {
            throw new RuntimeException("Can't pathfind between nodes that are not part of this network");
        }
        class PathNode implements Comparable<PathNode>
        {
            public PathNode previousNode;
            public WispNode node;
            public float travelCost = 0.f;
            public float estimatedRemainingCost = 0.f;
            public boolean inQueue = true;

            @Override
            public int compareTo(@NotNull PathNode o)
            {
                float thisCost = travelCost + estimatedRemainingCost;
                float otherCost = o.travelCost + o.estimatedRemainingCost;
                return Float.compare(thisCost, otherCost);
            }
        }

        if(from.GetDimension() != to.GetDimension())
        {
            //ToDo need to handle different dimensions, likely keep a list of nodes with cross-dimensional connections
            return false;
        }

        //max speed that can ever be on the network
        final float maxSpeed = 1.f;

        PathNode firstNode = new PathNode();
        firstNode.node = from;
        firstNode.travelCost = 0.f;
        firstNode.estimatedRemainingCost = EstimateRemainingCost(from, to, maxSpeed);

        HashMap<WispNode, PathNode> visitedNodes = new HashMap<>();
        PriorityQueue<PathNode> queue = new PriorityQueue<>();
        visitedNodes.put(from, firstNode);
        queue.add(firstNode);
        while (!queue.isEmpty())
        {
            final PathNode currentNode = queue.poll();
            WispNode.DestinationData currentNodeDestinationData = currentNode.node.GetPathData().nextNodeToDestinations.get(to);
            if(currentNode.node == to || currentNodeDestinationData != null)
            {
                //done
                PathNode nextNode = currentNode;
                PathNode previous = nextNode.previousNode;
                float totalTravelCostToDestination = currentNodeDestinationData != null ? currentNodeDestinationData.pathCost() : 0.f;
                while(previous != null)
                {
                    WispNode.PathData previousPathData = previous.node.GetPathData();
                    totalTravelCostToDestination += nextNode.travelCost - previous.travelCost;
                    previousPathData.nextNodeToDestinations.put(to, new WispNode.DestinationData(nextNode.node, totalTravelCostToDestination));
                    nextNode = previous;
                    previous = nextNode.previousNode;
                }
                return true;
            }

            currentNode.inQueue = false;
            for(WispNode connectedNode : currentNode.node.GetConnectedNodes())
            {
                float travelCost = currentNode.travelCost + CalculateTravelCost(currentNode.node, connectedNode);
                float estimatedRemainingCost = EstimateRemainingCost(connectedNode, to, maxSpeed);

                visitedNodes.compute(connectedNode, (k, v)->
                {
                    if(v == null)
                    {
                        PathNode pathNode = new PathNode();
                        pathNode.previousNode = currentNode;
                        pathNode.node = connectedNode;
                        pathNode.travelCost = travelCost;
                        pathNode.estimatedRemainingCost = estimatedRemainingCost;
                        queue.add(pathNode);
                        return pathNode;
                    }
                    else
                    {
                        if(travelCost < v.travelCost)
                        {
                            if(v.inQueue)
                            {
                                queue.remove(v);
                            }
                            v.travelCost = travelCost;
                            v.inQueue = true;
                            queue.add(v);
                        }
                        return v;
                    }
                });
            }
        }
        //failed to find a path
        return false;
    }
}
