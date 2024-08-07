package settop.IgnesFatui.WispNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.IgnesFatui;

import java.util.ArrayList;
import java.util.HashMap;

public class WispNode
{
    //path cost scaled for a node of speed 1
    public record DestinationData(WispNode nextPathNode, float pathCost) {}
    public static class PathData
    {
        public final HashMap<WispNode, DestinationData> nextNodeToDestinations = new HashMap<>();
        public WispNode nodeTowardsNetwork;
    }

    public final ResourceKey<Level> dimension;
    public final BlockPos pos;
    private WispNetwork connectedNetwork;
    private final ArrayList<WispNode> connectedNodes = new ArrayList<>();
    private final ArrayList<WispNodeUpgrade> upgrades = new ArrayList<>();
    private final PathData pathData = new PathData();
    private BlockEntity linkedBlockEntity;

    public WispNode(ResourceKey<Level> dimension, BlockPos pos)
    {
        this.dimension = dimension;
        this.pos = pos;
    }

    public ResourceKey<Level> GetDimension() { return dimension; }
    public BlockPos GetPos() { return pos; }
    public PathData GetPathData() { return pathData; }
    public float GetSpeed() { return 1.f; }

    public WispNetwork GetConnectedNetwork()
    {
        return connectedNetwork;
    }

    public ArrayList<WispNode> GetConnectedNodes()
    {
        return connectedNodes;
    }

    public BlockEntity GetLinkedBlockEntity()
    {
        return linkedBlockEntity;
    }

    public void OnConnectToNetwork(@NotNull WispNetwork _network)
    {
        connectedNetwork = _network;
        for(WispNodeUpgrade upgrade : upgrades)
        {
            upgrade.OnParentNodeConnectToNetwork();
        }
    }

    public void OnDisconnectFromNetwork(@NotNull WispNetwork _network)
    {
        for(WispNodeUpgrade upgrade : upgrades)
        {
            upgrade.OnParentNodeDisconnectFromNetwork();
        }
        connectedNetwork = null;
    }

    public boolean TryConnectToNode(@NotNull WispNode node)
    {
        if(connectedNetwork != null && node.connectedNetwork != null && connectedNetwork != node.connectedNetwork)
        {
            return false;
        }

        if(connectedNodes.size() < node.connectedNodes.size())
        {
            if(connectedNodes.contains(node))
            {
                return false;
            }
        }
        else
        {
            if(node.connectedNodes.contains(this))
            {
                return false;
            }
        }

        connectedNodes.add(node);
        node.connectedNodes.add(this);

        if(connectedNetwork != null)
        {
            connectedNetwork.OnNodesConnected(this, node);
        }
        else if(node.connectedNetwork != null)
        {
            node.connectedNetwork.OnNodesConnected(this, node);
        }

        return true;
    }

    public void DisconnectFromNode(@NotNull WispNode node)
    {
        if(!connectedNodes.contains(node))
        {
            return;
        }
        if(connectedNetwork != null)
        {
            connectedNetwork.OnNodesDisconnected(this, node);
        }

        connectedNodes.remove(node);
        node.connectedNodes.remove(this);

        if(connectedNetwork != null)
        {
            connectedNetwork.UpdateNodeNetworkConnection(this);
            connectedNetwork.UpdateNodeNetworkConnection(node);
        }
    }

    public void DisconnectAll()
    {
        if(connectedNetwork != null)
        {
            connectedNetwork.OnNodeDisconnectedFromAll(this);
        }
        //clear nodes after disconnecting from network
        for(WispNode node : connectedNodes)
        {
            node.connectedNodes.remove(this);
        }
        if(connectedNetwork != null)
        {
            for (WispNode node : connectedNodes)
            {
                connectedNetwork.UpdateNodeNetworkConnection(node);
            }
        }
        connectedNodes.clear();
    }

    public void AddUpgrade(@NotNull WispNodeUpgrade upgrade)
    {
        upgrades.add(upgrade);
        upgrade.OnAddToNode(this);
    }

    public void RemoveUpgrade(@NotNull WispNodeUpgrade upgrade)
    {
        if(upgrades.remove(upgrade))
        {
            upgrade.OnRemoveFromNode(this);
        }
    }

    public boolean IsLinkedToBlockEntity()
    {
        return linkedBlockEntity != null;
    }

    public void LinkToBlockEntity(@NotNull BlockEntity blockEntity)
    {
        if(linkedBlockEntity == null)
        {
            linkedBlockEntity = blockEntity;
            for(WispNodeUpgrade upgrade : upgrades)
            {
                upgrade.OnParentNodeLinkedToBlockEntity();
            }
        }
        else
        {
            IgnesFatui.LOGGER.error("Trying to link wisp node to a block entity while it is already linked. Dim: %s Pos: %s Linked block entity: %s New block entity: %s".formatted
            (
                    dimension.toString(),
                    pos.toString(),
                    linkedBlockEntity.toString(),
                    blockEntity.toString()
            ));
        }
    }

    public void UnlinkFromBlockEntity(@NotNull BlockEntity blockEntity)
    {
        if(linkedBlockEntity == blockEntity)
        {
            for(WispNodeUpgrade upgrade : upgrades)
            {
                upgrade.OnParentNodeUnlinkedFromBlockEntity();
            }
            linkedBlockEntity = null;
        }
        else
        {
            IgnesFatui.LOGGER.error("Trying to unlink wisp node from a block entity when it is linked to a different entity. Dim: %s Pos: %s Linked block entity: %s Unlinking block entity: %s".formatted
            (
                    dimension.toString(),
                    pos.toString(),
                    linkedBlockEntity == null ? "null" : linkedBlockEntity.toString(),
                    blockEntity.toString()
            ));
        }
    }
}
