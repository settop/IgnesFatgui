package settop.IgnesFatui.WispNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

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

    public WispNode(ResourceKey<Level> dimension, BlockPos pos)
    {
        this.dimension = dimension;
        this.pos = pos;
    }

    public ResourceKey<Level> GetDimension() { return dimension; }
    public BlockPos GetPos() { return pos; }
    public PathData GetPathData() { return pathData; }
    public float GetSpeed() { return 1.f; }

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

    public WispNetwork GetConnectedNetwork()
    {
        return connectedNetwork;
    }

    public ArrayList<WispNode> GetConnectedNodes()
    {
        return connectedNodes;
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
        if(connectedNetwork != null)
        {
            upgrade.OnParentNodeConnectToNetwork();
        }
    }

    public void RemoveUpgrade(@NotNull WispNodeUpgrade upgrade)
    {
        if(upgrades.remove(upgrade))
        {
            if(connectedNetwork != null)
            {
                upgrade.OnParentNodeDisconnectFromNetwork();
            }
            upgrade.OnRemoveFromNode(this);
        }
    }
}
