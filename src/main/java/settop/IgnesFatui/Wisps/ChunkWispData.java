package settop.IgnesFatui.Wisps;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Utils.Utils;
import settop.IgnesFatui.WispNetwork.WispNetwork;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;

@Mod.EventBusSubscriber( modid = IgnesFatui.MOD_ID)
public class ChunkWispData
{
    private static class DimensionData
    {
        public HashMap<ChunkPos, ChunkWisps> chunkData = new HashMap<>();
        //public ArrayList<WeakReference<IWispAddListener>> addListeners = new ArrayList<>();
        public ArrayList<WispNode> queuedAddedNode = new ArrayList<>();
        public ArrayList<WispNode> queuedRemovedNode = new ArrayList<>();

        //No need for this to by synchronised since it should already be handled by calling function
        public ChunkWisps EnsureChunkWisps(BlockPos blockPos)
        {
            ChunkPos chunkPos = Utils.GetChunkPos(blockPos);
            return chunkData.computeIfAbsent(chunkPos, (key)->new ChunkWisps());
        }
/*
        public void NotifyListenersOfAdd(World world, BlockPos pos, Object obj)
        {
            Iterator<WeakReference<IWispAddListener>> it = addListeners.iterator();

            while(it.hasNext())
            {
                WeakReference<IWispAddListener> addListenerRef = it.next();

                IWispAddListener addListener = addListenerRef.get();
                if (addListener == null)
                {
                    it.remove();
                    continue;
                }
                else if(addListener == obj)
                {
                    continue;
                }

                double rangeSq = addListener.GetRange() * addListener.GetRange() + 0.01;
                if (addListener.GetPos().distanceSq(pos) < rangeSq)
                {
                    addListener.WispObjAdded(world, pos, obj);
                }
            }
        }

 */
    }
    private static final HashMap<ResourceLocation, DimensionData> dimensionData = new HashMap<>();

    @Nonnull
    private static synchronized DimensionData EnsureDimensionData(World inWorld)
    {
        if(inWorld.isRemote())
        {
            throw new RuntimeException("Trying to get chunk data on client");
        }

        ResourceLocation dimension = inWorld.getDimensionKey().getLocation();
        return dimensionData.computeIfAbsent(dimension, (key)->new DimensionData());
    }

    @Nullable
    private static synchronized DimensionData GetDimensionData(World inWorld)
    {
        if(inWorld.isRemote())
        {
            throw new RuntimeException("Trying to get chunk data on client");
        }

        ResourceLocation dimension = inWorld.getDimensionKey().getLocation();
        return dimensionData.get(dimension);
    }

    /**
     * Get's the wisp at the given position in the world if there is one
     * Else creates a new wisp of the supplied type
     *
     * Return's the wisp and a boolean indicating if the wisp is a newly created one
     **/
    public static synchronized Tuple<WispBase, Boolean> GetOrCreateWisp(String type, World inWorld, BlockPos inPos, CompoundNBT tagData)
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

        ResourceLocation dimension = inWorld.getDimensionKey().getLocation();
        DimensionData dimData = dimensionData.computeIfAbsent(dimension, (key)->new DimensionData());
        ChunkWisps chunkWisps = dimData.chunkData.computeIfAbsent(chunk.getPos(), (key)->new ChunkWisps());

        Tuple<WispBase, Boolean> wisp = chunkWisps.GetOrCreateWisp(type, chunk, inPos, tagData);

        if(wisp.getB())
        {
            //a new wisp was added
            //dimData.NotifyListenersOfAdd(inWorld, wisp.getA().GetPos(), wisp.getA());
        }

        return wisp;
    }

    public static synchronized void RegisterWispNetwork(World world, WispNetwork network)
    {
        DimensionData dimData = EnsureDimensionData(world);
        ChunkWisps chunkWisps = dimData.EnsureChunkWisps(network.pos);

        chunkWisps.RegisterWispNetwork(network);

        /*
        for(WeakReference<IWispAddListener> addListenerRef : dimData.addListeners)
        {
            IWispAddListener addListener = addListenerRef.get();
            if(addListener != null && addListener != network)
            {
                double rangeSq = addListener.GetRange() * addListener.GetRange() + 0.01;
                BlockPos networkPos = network.GetClosestPos(addListener.GetPos());
                if(addListener.GetPos().distanceSq(networkPos) < rangeSq)
                {
                    addListener.WispObjAdded(world, networkPos, network);
                }
            }
        }
         */
    }

    public static synchronized void UnregisterWispNetwork(World world, WispNetwork network)
    {
        DimensionData dimData = EnsureDimensionData(world);
        ChunkWisps chunkWisps = dimData.EnsureChunkWisps(network.pos);

        chunkWisps.UnregisterWispNetwork(network);
    }

    private static synchronized boolean TryConnectNodeToNetwork(World inWorld, DimensionData dimData, WispNode node)
    {
        if(node.autoConnectRange <= 0)
        {
            return false;
        }

        int minChunkX = (node.pos.getX() - node.autoConnectRange) >> 4;
        int maxChunkX = (node.pos.getX() + node.autoConnectRange) >> 4;
        int minChunkZ = (node.pos.getZ() - node.autoConnectRange) >> 4;
        int maxChunkZ = (node.pos.getZ() + node.autoConnectRange) >> 4;

        for(int chunkX = minChunkX; chunkX <= maxChunkX; ++chunkX)
        {
            for(int chunkZ = minChunkZ; chunkZ <= maxChunkZ; ++chunkZ)
            {
                ChunkPos chunkPos = new ChunkPos( chunkX, chunkZ );
                ChunkWisps chunkWisps = dimData.chunkData.get(chunkPos);
                if(chunkWisps == null)
                {
                    continue;
                }
                if(chunkWisps.TryConnectNodeToNetwork(inWorld, node))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static synchronized void RecursiveAutoConnectToNodesAndWisps(World inWorld, DimensionData dimData, WispNode inNode)
    {
        HashSet<WispNode> newlyNetworkedNodes = new HashSet<>();
        newlyNetworkedNodes.add(inNode);

        while(!newlyNetworkedNodes.isEmpty())
        {
            Iterator<WispNode> it = newlyNetworkedNodes.iterator();
            WispNode node = it.next();
            it.remove();

            if (node.autoConnectRange <= 0)
            {
                continue;
            }

            int minChunkX = (node.pos.getX() - node.autoConnectRange) >> 4;
            int maxChunkX = (node.pos.getX() + node.autoConnectRange) >> 4;
            int minChunkZ = (node.pos.getZ() - node.autoConnectRange) >> 4;
            int maxChunkZ = (node.pos.getZ() + node.autoConnectRange) >> 4;

            for (int chunkX = minChunkX; chunkX <= maxChunkX; ++chunkX)
            {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; ++chunkZ)
                {
                    ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
                    ChunkWisps chunkWisps = dimData.chunkData.get(chunkPos);
                    if (chunkWisps == null)
                    {
                        continue;
                    }
                    chunkWisps.AutoConnectNodeToNodes(inWorld, node, newlyNetworkedNodes);
                    chunkWisps.AutoConnectNodeToWisps(inWorld, node);
                }
            }
        }
    }

    private static boolean CheckIfValidNetworkConnection(WispNode sourceNode, WispNode testNode)
    {
        for(WispNode n = testNode; ; n = n.networkConnection.node)
        {
            if(n.connectedNetwork != null && n.networkConnection == null)
            {
                //n is connected directly to the network
                return true;
            }
            else if(n.connectedNetwork == null)
            {
                return false;
            }
            else if(n.networkConnection.node == sourceNode)
            {
                return false;
            }
        }
    }

    private static synchronized void ProcessNodeRemoval(World inWorld, DimensionData dimData, WispNode inNode)
    {
        WispNetwork network = inNode.GetConnectedNetwork();

        ArrayList<WispNode> orphanedNodes = new ArrayList<>();

        for(WispNode.Connection connection : inNode.connectedNodes)
        {
            connection.node.connectedNodes.removeIf((otherConnection)->otherConnection.node == inNode);
            if(connection.node.networkConnection != null && connection.node.networkConnection.node == inNode)
            {
                orphanedNodes.add(connection.node);
                connection.node.connectedNetwork = null;
                connection.node.networkConnection = null;
            }
        }
        inNode.connectedNodes.clear();

        if(network != null)
        {
            network.RemoveNode(inNode);
        }

        boolean updated = true;
        while(updated)
        {
            updated = false;
            for (int i = 0; i < orphanedNodes.size(); ++i)
            {
                WispNode orphanedNode = orphanedNodes.get(i);
                //first check if we can connect via another connected node
                for (WispNode.Connection connection : orphanedNode.connectedNodes)
                {
                    if (CheckIfValidNetworkConnection(orphanedNode, connection.node))
                    {
                        orphanedNode.connectedNetwork = connection.node.connectedNetwork;
                        orphanedNode.networkConnection = connection;
                        break;
                    }
                }
                if (orphanedNode.connectedNetwork != null)
                {
                    //we managed to re-connect this node to the network
                    //remove it from the orphaned list
                    updated = true;
                    orphanedNodes.remove(i);
                    --i;
                    continue;
                }
                else
                {
                    //spread the orphanness
                    for (WispNode.Connection connection : orphanedNode.connectedNodes)
                    {
                        if (connection.node.networkConnection != null && connection.node.networkConnection.node == orphanedNode)
                        {
                            connection.node.networkConnection = null;
                            connection.node.connectedNetwork = null;
                            orphanedNodes.add(connection.node);
                        }
                    }
                }
            }
        }

        //any remaining orphaned nodes couldn't be re-connected to the network
        for(WispNode orphanedNode : orphanedNodes)
        {
            if(network != null)
            {
                network.RemoveNode(orphanedNode);
            }

            for(Iterator<WispNode.Connection> it = orphanedNode.connectedNodes.iterator(); it.hasNext();)
            {
                WispNode.Connection connection = it.next();
                if(connection.connectionType == WispNode.eConnectionType.AutoConnect)
                {
                    connection.node.connectedNodes.removeIf((otherConnection) -> otherConnection.node == orphanedNode);
                    it.remove();
                }
            }

            for(WispBase wisp : orphanedNode.connectedWisps)
            {
                wisp.connections.remove(orphanedNode);
            }
            orphanedNode.connectedWisps.clear();
        }
    }

    public static synchronized void RegisterNode(World inWorld, WispNode node)
    {
        DimensionData dimData = EnsureDimensionData(inWorld);
        ChunkWisps chunkWisps = dimData.EnsureChunkWisps(node.pos);

        dimData.queuedAddedNode.add(node);
        chunkWisps.RegisterNode(node);
    }

    public static synchronized void UnregisterNode(World inWorld, WispNode node)
    {
        DimensionData dimData = EnsureDimensionData(inWorld);
        ChunkWisps chunkWisps = dimData.EnsureChunkWisps(node.pos);

        if(!dimData.queuedAddedNode.remove(node))
        {
            //node has been added, so need to remove it
            dimData.queuedRemovedNode.add(node);
        }
        //else node, wasn't added yet

        chunkWisps.UnregisterNode(node);
    }

    @SubscribeEvent
    public static synchronized void OnTick(TickEvent.WorldTickEvent tickEvent)
    {
        if(tickEvent.side == LogicalSide.CLIENT)
        {
            return;
        }

        DimensionData dimData = GetDimensionData(tickEvent.world);
        if(dimData != null)
        {
            for (WispNode node : dimData.queuedRemovedNode)
            {
                ProcessNodeRemoval(tickEvent.world, dimData, node);
            }
            dimData.queuedRemovedNode.clear();

            for (WispNode node : dimData.queuedAddedNode)
            {
                if(TryConnectNodeToNetwork(tickEvent.world, dimData, node))
                {
                    RecursiveAutoConnectToNodesAndWisps(tickEvent.world, dimData, node);
                }
            }
            dimData.queuedAddedNode.clear();
        }

    }

    @SubscribeEvent
    public static synchronized void OnChunkLoad(ChunkDataEvent.Load loadEvent)
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

        DimensionData dimData = EnsureDimensionData((World)loadEvent.getWorld());
        dimData.chunkData.put(loadEvent.getChunk().getPos(), newChunkWisps);

        /*
        newChunkWisps.wispConnectionNodes.forEach((key, node)->
        {
            dimData.NotifyListenersOfAdd((World)loadEvent.getWorld(), key, node);
            dimData.addListeners.add(new WeakReference<>(node));
        });
        newChunkWisps.wispsInChunk.forEach((key, wisp)->
        {
            dimData.NotifyListenersOfAdd((World)loadEvent.getWorld(), key, wisp);
        });

         */
    }

    @SubscribeEvent
    public static synchronized void OnChunkSave(ChunkDataEvent.Save saveEvent)
    {
        if(saveEvent.getWorld().isRemote()) return;

        DimensionData dimData = EnsureDimensionData((World)saveEvent.getWorld());

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
    public static synchronized void OnWorldUnload(WorldEvent.Unload unloadEvent)
    {
        if(unloadEvent.getWorld().isRemote()) return;

        ResourceLocation dimension =  ((World)unloadEvent.getWorld()).getDimensionKey().getLocation();
        dimensionData.remove(dimension);
    }

    @SubscribeEvent
    public static synchronized void OnNeighborUpdate(BlockEvent.NeighborNotifyEvent updateEvent)
    {
        if(updateEvent.getWorld().isRemote()) return;

        if(updateEvent.getState().getBlock() != Blocks.AIR)
        {
            //we only care about blocks being removed, i.e. set to air
            return;
        }

        ResourceLocation dimension =  ((World)updateEvent.getWorld()).getDimensionKey().getLocation();
        DimensionData dimData = dimensionData.get(dimension);
        if(dimData == null) return;

        BlockPos pos = updateEvent.getPos();
        ChunkPos chunkPos = Utils.GetChunkPos(pos);

        ChunkWisps chunkWisps = dimData.chunkData.get( chunkPos );
        if(chunkWisps == null) return;

        chunkWisps.BlockDestroyed(updateEvent.getWorld(), pos);
    }

    @OnlyIn(Dist.CLIENT)
    public static synchronized void RenderConnections(RenderWorldLastEvent evt)
    {
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        IVertexBuilder builder = buffer.getBuffer(RenderType.LINES);

        Vector3d view = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();

        MatrixStack matrix = evt.getMatrixStack();
        matrix.push();
        matrix.translate(-view.getX(), -view.getY(), -view.getZ());

        dimensionData.forEach((dim, dimData)->dimData.chunkData.forEach(((pos, chunkWisps)->
        {
            chunkWisps.RenderConnections(evt, matrix, builder);
        })));

        matrix.pop();
        buffer.finish(RenderType.LINES);
    }
}
