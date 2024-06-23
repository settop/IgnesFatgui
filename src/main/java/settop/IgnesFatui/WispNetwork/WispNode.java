package settop.IgnesFatui.WispNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class WispNode
{
    public final ResourceKey<Level> dimension;
    public final BlockPos pos;
    private WispNetwork connectedNetwork;
    private final ArrayList<WispNode> connectedNodes = new ArrayList<>();
    private final ArrayList<WispNodeUpgrade> upgrades = new ArrayList<>();

    public WispNode(ResourceKey<Level> dimension, BlockPos pos)
    {
        this.dimension = dimension;
        this.pos = pos;
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

    public WispNetwork GetConnectedNetwork()
    {
        return connectedNetwork;
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

        if(connectedNetwork == null && node.connectedNetwork != null)
        {
            node.connectedNetwork.AddNode(this);
        }
        else if(connectedNetwork != null && node.connectedNetwork == null)
        {
            connectedNetwork.AddNode(node);
        }
        connectedNodes.add(node);
        node.connectedNodes.add(this);
        return true;
    }

    public void DisconnectAll()
    {
        for(WispNode node : connectedNodes)
        {
            node.connectedNodes.remove(this);
        }
        connectedNodes.clear();

        if(connectedNetwork != null)
        {
            connectedNetwork.RemoveNode(this);
        }
    }

    public void AddUpgrade(WispNodeUpgrade upgrade)
    {
        upgrades.add(upgrade);
        if(connectedNetwork != null)
        {
            upgrade.OnParentNodeConnectToNetwork();
        }
    }

    public void RemoveUpgrade(WispNodeUpgrade upgrade)
    {
        if(upgrades.remove(upgrade))
        {
            if(connectedNetwork != null)
            {
                upgrade.OnParentNodeDisconnectFromNetwork();
            }
        }
    }
}
