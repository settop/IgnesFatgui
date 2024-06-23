package settop.IgnesFatui.Wisps;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.WispNetwork.WispNetwork;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class WispNode implements IWispAddListener
{
    public enum eConnectionType
    {
        Link,
        AutoConnect
    }

    public static class Connection
    {
        public Connection(WispNode inNode, eConnectionType inConnection)
        {
            node = inNode;
            connectionType = inConnection;
            active = true;
        }

        public WispNode node;
        public eConnectionType connectionType;
        public boolean active;
    }

    public final BlockPos pos;
    public ArrayList<Connection> connectedNodes;
    public ArrayList<WispBase> connectedWisps;
    public final int autoConnectRange;

    //networkConnection points to the node it is connected to the network with
    //it being null implies a direction connection to the network
    public WeakReference<WispNetwork> connectedNetwork;
    public Connection networkConnection;

    public WispNode(BlockPos pos, int autoConnectRange)
    {
        this.pos = pos;
        this.autoConnectRange = autoConnectRange;
        connectedNodes = new ArrayList<>();
        connectedWisps = new ArrayList<>();
    }

    @Override
    public BlockPos GetPos()
    {
        return pos;
    }

    @Override
    public int GetRange()
    {
        return autoConnectRange;
    }

    @Override
    public void WispObjAdded(Level level, BlockPos pos, Object obj)
    {
        if(CanConnectToPos(level, pos, autoConnectRange))
        {
            if(obj instanceof WispNode)
            {
            }
            else if(obj instanceof WispBase)
            {
                ConnectToWisp((WispBase)obj);
            }
            else if(obj instanceof WispNetwork)
            {
            }
            else
            {
                IgnesFatui.LOGGER.warn("WispNode: Unknown wisp obj added type");
            }
        }
    }

    public WispNetwork GetConnectedNetwork()
    {
        if(connectedNetwork != null)
        {
            return connectedNetwork.get();
        }
        else
        {
            return null;
        }
    }

    public void ConnectToWisp(WispBase wisp)
    {
        if(connectedWisps.contains(wisp)) return;

        connectedWisps.add(wisp);
        wisp.connections.add(this);
        if(connectedNetwork != null && connectedNetwork.get() != null)
        {
            //connectedNetwork.get().AddWispNodeConnection(this, wisp);
        }
    }

    public void DisonnectWisp(WispBase wisp)
    {
        connectedWisps.remove(wisp);
        wisp.connections.remove(this);
        if(connectedNetwork != null && connectedNetwork.get() != null)
        {
            //connectedNetwork.get().RemoveWispNodeConnection(this, wisp);
        }
    }

    public boolean CanConnectToPos(Level level, BlockPos target, int connectionRange)
    {
        /*
        double range = (connectionRange * connectionRange) + 0.01;

        if(target.distSqr(pos) > range)
        {
            //too far
            return false;
        }
        Vector3d startPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        Vector3d endPos = new Vector3d(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
        //ToDo: Do this correctly with the proper model
        Vector3d direction = endPos.sub(startPos).normalize();
        Vector3d thisStartPos = startPos.add(direction);

        BlockRayTraceResult result = level.rayTraceBlocks(new RayTraceContext(thisStartPos, endPos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null));

        if(result.getType() == RayTraceResult.Type.MISS || result.getPos().equals(target))
        {
            return true;
        }

         */

        return false;
    }
}
