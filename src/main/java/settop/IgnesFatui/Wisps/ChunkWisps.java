package settop.IgnesFatui.Wisps;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import settop.IgnesFatui.Blocks.WispConnectionNode;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.WispNetwork.WispNetwork;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ChunkWisps
{
    public HashMap<BlockPos, WispBase> wispsInChunk = new HashMap<>();
    public HashMap<BlockPos, WispNode> wispConnectionNodes = new HashMap<>();
    public ArrayList<WispNetwork> chunkNetworks = new ArrayList<>();

    public CompoundNBT save()
    {
        if(wispsInChunk.isEmpty())
        {
            return null;
        }
        CompoundNBT nbt = new CompoundNBT();

        for( HashMap.Entry<BlockPos, WispBase> entry : wispsInChunk.entrySet() )
        {
            BlockPos pos = entry.getKey();
            String stringKey = String.format("%d,%d,%d",pos.getX(), pos.getY(), pos.getZ() );

            CompoundNBT wispNBT = entry.getValue().Save();
            nbt.put(stringKey, wispNBT);
        }

        return nbt;
    }

    public void load(IChunk inChunk, CompoundNBT nbt)
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
            WispBase loadedWisp = WispFactory.LoadWisp(inChunk, nbt.getCompound(stringKey));
            wispsInChunk.put(key, loadedWisp);
        }
    }

    public Tuple<WispBase, Boolean> GetOrCreateWisp(String type, IChunk inChunk, BlockPos inPos, CompoundNBT tagData)
    {
        WispBase existingWisp = wispsInChunk.get(inPos);
        if(existingWisp != null)
        {
            return new Tuple(existingWisp, false);
        }
        else
        {
            WispBase newWisp = WispFactory.CreateNewWisp(type, inChunk, inPos);
            newWisp.InitFromTagData(tagData);
            wispsInChunk.put(inPos, newWisp);
            return new Tuple(newWisp, true);
        }
    }

    public void RegisterNode(WispNode node)
    {
        wispConnectionNodes.put(node.pos, node);
    }

    public void UnregisterNode(WispNode node)
    {
        wispConnectionNodes.remove(node.pos, node);
    }

    public void RegisterWispNetwork(WispNetwork network)
    {
        for(WispNetwork existingNetwork : chunkNetworks)
        {
            if(existingNetwork.pos.equals(network.pos))
            {
                throw new AssertionError("Adding a wisp network, but one already exists in the same pos");
            }
        }

        chunkNetworks.add(network);
    }

    public void UnregisterWispNetwork(WispNetwork network)
    {
        chunkNetworks.remove(network);
    }

    private static void SetupNodeConnection(WispNode nodeA, WispNode nodeB, WispNode.eConnectionType type)
    {
        nodeA.connectedNodes.add(new WispNode.Connection(nodeB, type));
        nodeB.connectedNodes.add(new WispNode.Connection(nodeA, type));
    }

    private static void SetupWispConnection(WispNode node, WispBase wisp)
    {
        node.connectedWisps.add(wisp);
        wisp.connections.add(node);

        WispNetwork network = node.GetConnectedNetwork();
        network.AddWispNodeConnection(node, wisp);
    }

    private static boolean AreNodesConnected(WispNode nodeA, WispNode nodeB)
    {
        //if connected they should both have a reference to the other
        //so just check the one with less connections
        WispNode node1 = nodeA;
        WispNode node2 = nodeB;

        if(nodeA.connectedNodes.size() > nodeB.connectedNodes.size())
        {
            node1 = nodeB;
            node2 = nodeA;
        }

        for(WispNode.Connection connection : node1.connectedNodes)
        {
            if(connection.node == node2)
            {
                return true;
            }
        }
        return false;
    }

    public boolean TryConnectNodeToNetwork(World inWorld, WispNode node)
    {
        for(WispNetwork network : chunkNetworks)
        {
            BlockPos testPos = network.GetClosestPos(node.pos);

            if(node.CanConnectToPos(inWorld, testPos, node.autoConnectRange))
            {
                node.connectedNetwork = new WeakReference<>(network);
                network.AddNode(node);
                return true;
            }
        }

        for(HashMap.Entry<BlockPos, WispNode> otherNode : wispConnectionNodes.entrySet())
        {
            if(node == otherNode.getValue())
            {
                continue;
            }
            WispNetwork network = otherNode.getValue().GetConnectedNetwork();
            int autoConnectRange = Math.max(node.autoConnectRange, otherNode.getValue().autoConnectRange);
            if(network != null && node.CanConnectToPos(inWorld, otherNode.getKey(), autoConnectRange))
            {
                node.connectedNetwork = otherNode.getValue().connectedNetwork;
                network.AddNode(node);

                SetupNodeConnection(node, otherNode.getValue(), WispNode.eConnectionType.AutoConnect);
                node.networkConnection = node.connectedNodes.get(node.connectedNodes.size() - 1);

                return true;
            }
        }

        return false;
    }

    public void AutoConnectNodeToNodes(World inWorld, WispNode node, HashSet<WispNode> newlyNetworkedNodes)
    {
        WispNetwork network = node.GetConnectedNetwork();
        for(HashMap.Entry<BlockPos, WispNode> otherNode : wispConnectionNodes.entrySet())
        {
            if(node == otherNode.getValue())
            {
                continue;
            }
            WispNetwork otherNetwork = otherNode.getValue().GetConnectedNetwork();
            int autoConnectRange = Math.max(node.autoConnectRange, otherNode.getValue().autoConnectRange);
            if((otherNetwork == network || otherNetwork == null) && !AreNodesConnected(node, otherNode.getValue()) && node.CanConnectToPos(inWorld, otherNode.getKey(), autoConnectRange))
            {
                SetupNodeConnection(node, otherNode.getValue(), WispNode.eConnectionType.AutoConnect);

                if (otherNetwork == null)
                {
                    otherNode.getValue().connectedNetwork = node.connectedNetwork;
                    network.AddNode(otherNode.getValue());
                    otherNode.getValue().networkConnection = otherNode.getValue().connectedNodes.get(otherNode.getValue().connectedNodes.size() - 1);
                    newlyNetworkedNodes.add(otherNode.getValue());
                }
            }
        }
    }

    public void AutoConnectNodeToWisps(World inWorld, WispNode node)
    {
        WispNetwork network = node.GetConnectedNetwork();
        for(HashMap.Entry<BlockPos, WispBase> wisp : wispsInChunk.entrySet())
        {
            if(node.CanConnectToPos(inWorld, wisp.getKey(), node.autoConnectRange))
            {
                SetupWispConnection(node, wisp.getValue());
            }
        }
    }

    public void BlockDestroyed(IWorld inWorld, BlockPos inPos)
    {
        WispBase existingWisp = wispsInChunk.get(inPos);
        if(existingWisp != null)
        {
            existingWisp.RemoveFromWorld();
            existingWisp.DropItemStackIntoWorld(inWorld);
            wispsInChunk.remove(inPos);
        }

    }


    @OnlyIn(Dist.CLIENT)
    public void RenderConnections(RenderWorldLastEvent evt, MatrixStack mat, IVertexBuilder builder)
    {
        mat.push();
        mat.translate(0.5f, 0.5f, 0.5f);
        Matrix4f matrix = mat.getLast().getMatrix();
        wispConnectionNodes.forEach((pos, node)->
        {
            for(WispNode.Connection connection : node.connectedNodes)
            {
                float rgb[] = { 0.f, 1.f, 0.f };
                if(connection.connectionType == WispNode.eConnectionType.Link)
                {
                    rgb[0] = 0.f;
                    rgb[1] = 0.f;
                    rgb[2] = 1.f;
                }

                builder.pos(matrix, node.pos.getX(), node.pos.getY(), node.pos.getZ())
                        .color(rgb[0], rgb[1], rgb[2], 0.8f)
                        .overlay(OverlayTexture.NO_OVERLAY)
                        .lightmap(15728880)
                        .endVertex();

                builder.pos(matrix, connection.node.pos.getX(), connection.node.pos.getY(), connection.node.pos.getZ())
                        .color(rgb[0], rgb[1], rgb[2], 0.8f)
                        .overlay(OverlayTexture.NO_OVERLAY)
                        .lightmap(15728880)
                        .endVertex();

            }

            for(WispBase wisp : node.connectedWisps)
            {
                builder.pos(matrix, node.pos.getX(), node.pos.getY(), node.pos.getZ())
                        .color(1.f, 0.f, 1.f, 0.8f)
                        .overlay(OverlayTexture.NO_OVERLAY)
                        .lightmap(15728880)
                        .endVertex();

                builder.pos(matrix, wisp.GetPos().getX(), wisp.GetPos().getY(), wisp.GetPos().getZ())
                        .color(1.f, 0.f, 1.f, 0.8f)
                        .overlay(OverlayTexture.NO_OVERLAY)
                        .lightmap(15728880)
                        .endVertex();

            }

            WispNetwork connectedNetwork = node.connectedNetwork != null ? node.connectedNetwork.get() : null;
            if(connectedNetwork != null)
            {
                builder.pos(matrix, node.pos.getX(), node.pos.getY(), node.pos.getZ())
                        .color(0.4f, 0.4f, 0.4f, 0.8f)
                        .overlay(OverlayTexture.NO_OVERLAY)
                        .lightmap(15728880)
                        .endVertex();

                builder.pos(matrix, connectedNetwork.pos.getX(), connectedNetwork.pos.getY(), connectedNetwork.pos.getZ())
                        .color(0.4f, 0.4f, 0.4f, 0.8f)
                        .overlay(OverlayTexture.NO_OVERLAY)
                        .lightmap(15728880)
                        .endVertex();
            }
        });
        mat.pop();
    }
}
