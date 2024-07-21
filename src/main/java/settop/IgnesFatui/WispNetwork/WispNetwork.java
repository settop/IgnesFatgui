package settop.IgnesFatui.WispNetwork;

import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;


import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.WispNetwork.Resource.ItemResourceManager;
import settop.IgnesFatui.WispNetwork.Resource.ResourceManager;
import settop.IgnesFatui.WispNetwork.Resource.ResourcesManager;

public class WispNetwork
{
    private WispNode rootNode;
    private final TaskManager taskManager = new TaskManager();
    private final HashSet<WispNode> nodes = new HashSet<>();

    private final ResourcesManager resourcesManager = new ResourcesManager();

    public WispNetwork(ResourceKey<Level> dimension, BlockPos pos)
    {
        this();
        this.rootNode = new WispNode(dimension, pos);
        nodes.add(rootNode);
        rootNode.OnConnectToNetwork(this);
        rootNode.GetPathData().nodeTowardsNetwork = rootNode;
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

    public boolean TryConnectNodeToNetwork(@NotNull WispNode node)
    {
        return rootNode.TryConnectToNode(node);
    }

    public void OnNodesConnected(@NotNull WispNode a, @NotNull WispNode b)
    {
        if(a.GetConnectedNetwork() != null && a.GetConnectedNetwork() != this)
        {
            throw new RuntimeException("Notifying network of a connected node that is already connected to another network");
        }
        if(b.GetConnectedNetwork() != null && b.GetConnectedNetwork() != this)
        {
            throw new RuntimeException("Notifying network of a connected node that is already connected to another network");
        }

        if(a.GetConnectedNetwork() == null)
        {
            a.GetPathData().nodeTowardsNetwork = b;
            AddNode(a);
        }
        if(b.GetConnectedNetwork() == null)
        {
            b.GetPathData().nodeTowardsNetwork = a;
            AddNode(b);
        }
        //ToDo: Need to figure out which cached paths need to be invalidated
    }

    private void AddNode(@NotNull WispNode node)
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
                connectedNode.GetPathData().nodeTowardsNetwork = node;
                AddNode(connectedNode);
            }
        }
    }

    public void OnNodesDisconnected(@NotNull WispNode a, @NotNull WispNode b)
    {
        if(a.GetConnectedNetwork() != this)
        {
            throw new RuntimeException("Notifying network of a disconnected node that is connected to another network");
        }
        if(b.GetConnectedNetwork() != this)
        {
            throw new RuntimeException("Notifying network of a disconnected node that is connected to another network");
        }

        InvalidateCachedPaths(a, b);
        InvalidateCachedPaths(b, a);
    }

    public void OnNodeDisconnectedFromAll(@NotNull WispNode node)
    {
        if(node.GetConnectedNetwork() != this)
        {
            throw new RuntimeException("Notifying network of a disconnected node that is connected to another network");
        }
        for(WispNode connectedNode : node.GetConnectedNodes())
        {
            InvalidateCachedPaths(node, connectedNode);
            InvalidateCachedPaths(connectedNode, node);
        }
        RemoveNode(node);
    }

    public void UpdateNodeNetworkConnection(WispNode node)
    {
        if(node.GetPathData().nodeTowardsNetwork == null)
        {
            if(!TryBuildConnectionToNetwork(node))
            {
                //need to remove all the connected nodes from the network
                RecursivelyRemoveNode(node);
            }
        }
    }

    private void InvalidateCachedPaths(@NotNull WispNode from, @NotNull WispNode to)
    {
        ArrayList<WispNode> destinationsToInvalidate = new ArrayList<>();
        for(var it = from.GetPathData().nextNodeToDestinations.entrySet().iterator(); it.hasNext();)
        {
            var entry = it.next();
            if(entry.getValue().nextPathNode() == to)
            {
                destinationsToInvalidate.add(entry.getKey());
                it.remove();
            }
        }
        boolean invalidateWispNetworkConnection = from.GetPathData().nodeTowardsNetwork == to;
        if(invalidateWispNetworkConnection)
        {
            from.GetPathData().nodeTowardsNetwork = null;
        }

        if(destinationsToInvalidate.isEmpty() && !invalidateWispNetworkConnection)
        {
            return;
        }

        for(WispNode connectedNode : from.GetConnectedNodes())
        {
            if(connectedNode == to)
            {
                continue;
            }
            InvalidateCachedPaths(connectedNode, from, destinationsToInvalidate, invalidateWispNetworkConnection);
        }
    }

    private void InvalidateCachedPaths(@NotNull WispNode from, @NotNull WispNode to, @NotNull List<WispNode> destinationsToInvalidate, boolean invalidateWispNetworkConnection)
    {
        if(invalidateWispNetworkConnection)
        {
            if(from.GetPathData().nodeTowardsNetwork == to)
            {
                from.GetPathData().nodeTowardsNetwork = null;
            }
            else
            {
                invalidateWispNetworkConnection = false;
            }
        }

        int end = destinationsToInvalidate.size();
        for(int i = 0; i < end;)
        {
            WispNode.DestinationData nodeToDestination = from.GetPathData().nextNodeToDestinations.get(destinationsToInvalidate.get(i));
            if(nodeToDestination != null && nodeToDestination.nextPathNode() == to)
            {
                from.GetPathData().nextNodeToDestinations.remove(destinationsToInvalidate.get(i));
                ++i;
            }
            else
            {
                --end;
                if(i != end)
                {
                    WispNode temp = destinationsToInvalidate.get(end);
                    destinationsToInvalidate.set(end, destinationsToInvalidate.get(i));
                    destinationsToInvalidate.set(i, temp);
                }
            }
        }

        if(end == 0 && !invalidateWispNetworkConnection)
        {
            //nothing to propagate
            return;
        }

        List<WispNode> destinationsToInvalidateForConnectedNodes = destinationsToInvalidate.subList(0, end);
        for(WispNode connectedNode : from.GetConnectedNodes())
        {
            if(connectedNode == to)
            {
                continue;
            }
            InvalidateCachedPaths(connectedNode, from, destinationsToInvalidateForConnectedNodes, invalidateWispNetworkConnection);
        }
    }

    float EstimateRemainingCostForNetworkConnection(WispNode from, WispNode to)
    {
        if(from.GetPathData().nodeTowardsNetwork != null)
        {
            return 0.f;
        }
        else
        {
            return (float)Math.sqrt(from.GetPos().distSqr(to.GetPos()));
        }
    }

    private boolean TryBuildConnectionToNetwork(WispNode node)
    {
        return TryBuildPathfindingBetweenNodesImpl
        (
            node,
            rootNode,
            this::CalculateTravelCostWithoutSpeed,
            this::EstimateRemainingCostForNetworkConnection,
            (PathNode p)->
            {
                if(p.node == rootNode || p.node.GetPathData().nodeTowardsNetwork != null)
                {
                    //done
                    PathNode nextNode = p;
                    PathNode previous = nextNode.previousNode;
                    while(previous != null)
                    {
                        WispNode.PathData previousPathData = previous.node.GetPathData();
                        previousPathData.nodeTowardsNetwork = nextNode.node;
                        nextNode = previous;
                        previous = nextNode.previousNode;
                    }
                    return true;
                }
                return false;
            }
        );
    }

    private void RemoveNode(@NotNull WispNode node)
    {
        if(!nodes.remove(node))
        {
            return;
        }
        node.OnDisconnectFromNetwork(this);
    }

    private void RecursivelyRemoveNode(@NotNull WispNode node)
    {
        if(!nodes.remove(node))
        {
            return;
        }
        node.OnDisconnectFromNetwork(this);

        for(WispNode connectedNode : node.GetConnectedNodes())
        {
            RecursivelyRemoveNode(connectedNode);
        }
    }

    public void AddTask(@NotNull Task task)
    {
        taskManager.AddTask(task);
    }

    public ResourcesManager GetResourcesManager()
    {
        return resourcesManager;
    }

    float CalculateTravelCostWithoutSpeed(WispNode from, WispNode to)
    {
        return (float)Math.sqrt(from.GetPos().distSqr(to.GetPos()));
    }

    float CalculateTravelCostWithSpeed(WispNode from, WispNode to)
    {
        return CalculateTravelCostWithoutSpeed(from, to) / from.GetSpeed();
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

    static class PathNode implements Comparable<PathNode>
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

    private boolean TryBuildPathfindingBetweenNodesImpl
    (
            WispNode from,
            WispNode to,
            BiFunction<WispNode, WispNode, Float> travelCostCalculator,
            BiFunction<WispNode, WispNode, Float> costEstimator,
            Function<PathNode, Boolean> checkAndHandleDestinationReached
    )
    {
        if(from.GetConnectedNetwork() != this || to.GetConnectedNetwork() != this)
        {
            throw new RuntimeException("Can't pathfind between nodes that are not part of this network");
        }

        if(from.GetDimension() != to.GetDimension())
        {
            //ToDo need to handle different dimensions, likely keep a list of nodes with cross-dimensional connections
            return false;
        }


        PathNode firstNode = new PathNode();
        firstNode.node = from;
        firstNode.travelCost = 0.f;
        firstNode.estimatedRemainingCost = costEstimator.apply(from, to);

        HashMap<WispNode, PathNode> visitedNodes = new HashMap<>();
        PriorityQueue<PathNode> queue = new PriorityQueue<>();
        visitedNodes.put(from, firstNode);
        queue.add(firstNode);
        while (!queue.isEmpty())
        {
            final PathNode currentNode = queue.poll();
            if(checkAndHandleDestinationReached.apply(currentNode))
            {
                return true;
            }

            currentNode.inQueue = false;
            for(WispNode connectedNode : currentNode.node.GetConnectedNodes())
            {
                float travelCost = currentNode.travelCost + travelCostCalculator.apply(currentNode.node, connectedNode);
                float estimatedRemainingCost = costEstimator.apply(connectedNode, to);

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

    public boolean TryBuildPathfindingBetweenNodes(WispNode from, WispNode to)
    {
        //max speed that can ever be on the network
        final float maxSpeed = 1.f;
        return TryBuildPathfindingBetweenNodesImpl
        (
            from,
            to,
            this::CalculateTravelCostWithSpeed,
            (WispNode f, WispNode t)-> EstimateRemainingCost(f, t, maxSpeed),
            (PathNode p)->
            {
                WispNode.DestinationData currentNodeDestinationData = p.node.GetPathData().nextNodeToDestinations.get(to);
                if(p.node == to || currentNodeDestinationData != null)
                {
                    //done
                    PathNode nextNode = p;
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
                return false;
            }
        );
    }
}
