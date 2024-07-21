package settop.IgnesFatui.Testing;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.WispNetwork.Resource.ResourceManager;
import settop.IgnesFatui.WispNetwork.Resource.ResourceSink;
import settop.IgnesFatui.WispNetwork.Resource.ResourceSource;
import settop.IgnesFatui.WispNetwork.Upgrades.ProviderUpgrade;
import settop.IgnesFatui.WispNetwork.WispNetwork;
import settop.IgnesFatui.WispNetwork.WispNode;

@GameTestHolder(IgnesFatui.MOD_ID)
public class WispNetworkTester
{
    @GameTest(batch = "WispNetwork", template = "forge:empty3x3x3")
    public static void WispNetworkTest_ConnectNodes(@NotNull GameTestHelper helper)
    {
        WispNetwork wispNetwork = new WispNetwork(helper.getLevel().dimension(), new BlockPos(0, 0, 0));

        WispNode node1 = new WispNode(helper.getLevel().dimension(), new BlockPos(10, 0, 0));
        WispNode node2 = new WispNode(helper.getLevel().dimension(), new BlockPos(0, 0, 10));
        WispNode node3 = new WispNode(helper.getLevel().dimension(), new BlockPos(10, 0, 10));

        helper.assertTrue(wispNetwork.TryConnectNodeToNetwork(node1), "Failed to connect node1 to network");
        helper.assertValueEqual(wispNetwork, node1.GetConnectedNetwork(), "node1 connectNetwork");
        helper.assertValueEqual(node1.GetConnectedNodes().size(), 1, "node1 connections");

        helper.assertTrue(node2.TryConnectToNode(node3), "Could not connect node2 and node3");
        helper.assertValueEqual(node2.GetConnectedNodes().size(), 1, "node2 connections");
        helper.assertValueEqual(node3.GetConnectedNodes().size(), 1, "node3 connections");

        helper.assertTrue(wispNetwork.TryConnectNodeToNetwork(node2), "Could not connect node2 to network");
        helper.assertValueEqual(wispNetwork, node2.GetConnectedNetwork(), "node2 connectedNetwork");
        helper.assertValueEqual(wispNetwork, node3.GetConnectedNetwork(), "node2 connectedNetwork");
        helper.assertValueEqual(node2.GetConnectedNodes().size(), 2, "node2 connections");
        helper.assertValueEqual(node3.GetConnectedNodes().size(), 1, "node3 connections");

        helper.assertTrue(node1.TryConnectToNode(node3), "Could not connect node1 and node3");
        helper.assertValueEqual(node1.GetConnectedNodes().size(), 2, "node2 connections");
        helper.assertValueEqual(node3.GetConnectedNodes().size(), 2, "node3 connections");

        helper.succeed();
    }

    @GameTest(batch = "WispNetwork", template = "forge:empty3x3x3")
    public static void WispNetworkTest_DisconnectNodes(@NotNull GameTestHelper helper)
    {
        WispNetwork wispNetwork = new WispNetwork(helper.getLevel().dimension(), new BlockPos(3, 0, 0));

        // A -- B -- C -- N
        // |    |
        // D -- E

        WispNode A = new WispNode(helper.getLevel().dimension(), new BlockPos(0, 0, 0));
        WispNode B = new WispNode(helper.getLevel().dimension(), new BlockPos(1, 0, 0));
        WispNode C = new WispNode(helper.getLevel().dimension(), new BlockPos(2, 0, 0));
        WispNode D = new WispNode(helper.getLevel().dimension(), new BlockPos(0, 0, 1));
        WispNode E = new WispNode(helper.getLevel().dimension(), new BlockPos(1, 0, 1));

        A.TryConnectToNode(B);
        A.TryConnectToNode(D);
        B.TryConnectToNode(C);
        B.TryConnectToNode(E);
        D.TryConnectToNode(E);
        wispNetwork.TryConnectNodeToNetwork(C);

        helper.assertTrue(A.GetConnectedNetwork() == wispNetwork, "Expect A connected to network");
        helper.assertTrue(B.GetConnectedNetwork() == wispNetwork, "Expect B connected to network");
        helper.assertTrue(C.GetConnectedNetwork() == wispNetwork, "Expect C connected to network");
        helper.assertTrue(D.GetConnectedNetwork() == wispNetwork, "Expect D connected to network");
        helper.assertTrue(E.GetConnectedNetwork() == wispNetwork, "Expect E connected to network");

        wispNetwork.TryBuildPathfindingBetweenNodes(E, C);
        wispNetwork.TryBuildPathfindingBetweenNodes(A, C);
        wispNetwork.TryBuildPathfindingBetweenNodes(D, C);

        helper.assertTrue(A.GetPathData().nextNodeToDestinations.containsKey(C) && A.GetPathData().nextNodeToDestinations.get(C).nextPathNode() == B, "Expect A path to C to go via B");
        helper.assertTrue(B.GetPathData().nextNodeToDestinations.containsKey(C) && B.GetPathData().nextNodeToDestinations.get(C).nextPathNode() == C, "Expect B path to C to go via C");
        helper.assertTrue(D.GetPathData().nextNodeToDestinations.containsKey(C), "Expect D path to C");
        helper.assertTrue(E.GetPathData().nextNodeToDestinations.containsKey(C) && E.GetPathData().nextNodeToDestinations.get(C).nextPathNode() == B, "Expect E path to C to go via B");

        B.DisconnectFromNode(A);

        //should all still be connected
        helper.assertTrue(A.GetConnectedNetwork() == wispNetwork, "Expect A connected to network");
        helper.assertTrue(B.GetConnectedNetwork() == wispNetwork, "Expect B connected to network");
        helper.assertTrue(C.GetConnectedNetwork() == wispNetwork, "Expect C connected to network");
        helper.assertTrue(D.GetConnectedNetwork() == wispNetwork, "Expect D connected to network");
        helper.assertTrue(E.GetConnectedNetwork() == wispNetwork, "Expect E connected to network");

        helper.assertTrue(!A.GetPathData().nextNodeToDestinations.containsKey(C), "Expect A path to C to be cleared");
        helper.assertTrue(B.GetPathData().nextNodeToDestinations.containsKey(C), "Expect B path to C to still exist");
        //d may have been cleared depending on which direction it went
        helper.assertTrue(E.GetPathData().nextNodeToDestinations.containsKey(C), "Expect E path to C to still exist");

        B.DisconnectFromNode(E);

        //only B and C should be connected
        helper.assertTrue(A.GetConnectedNetwork() == null, "Expect A not connected to network");
        helper.assertTrue(B.GetConnectedNetwork() == wispNetwork, "Expect B connected to network");
        helper.assertTrue(C.GetConnectedNetwork() == wispNetwork, "Expect C connected to network");
        helper.assertTrue(D.GetConnectedNetwork() == null, "Expect D not connected to network");
        helper.assertTrue(E.GetConnectedNetwork() == null, "Expect E not connected to network");

        helper.assertTrue(!A.GetPathData().nextNodeToDestinations.containsKey(C), "Expect A path to C to be cleared");
        helper.assertTrue(B.GetPathData().nextNodeToDestinations.containsKey(C), "Expect B path to C to still exist");
        helper.assertTrue(!D.GetPathData().nextNodeToDestinations.containsKey(C), "Expect D path to C to be cleared");
        helper.assertTrue(!E.GetPathData().nextNodeToDestinations.containsKey(C), "Expect E path to C to be cleared");

        helper.succeed();
    }

    @GameTest(batch = "WispNetwork", template = "forge:empty3x3x3")
    public static void WispNetworkTest_Pathfinding(@NotNull GameTestHelper helper)
    {
        // A -- B -- C -- N
        // |    |
        // D    E -- F
        // |         |
        // G -- H -- I

        WispNode A = new WispNode(helper.getLevel().dimension(), new BlockPos(0, 0, 0));
        WispNode B = new WispNode(helper.getLevel().dimension(), new BlockPos(1, 0, 0));
        WispNode C = new WispNode(helper.getLevel().dimension(), new BlockPos(2, 0, 0));
        WispNode D = new WispNode(helper.getLevel().dimension(), new BlockPos(0, 0, 1));
        WispNode E = new WispNode(helper.getLevel().dimension(), new BlockPos(1, 0, 1));
        WispNode F = new WispNode(helper.getLevel().dimension(), new BlockPos(2, 0, 1));
        WispNode G = new WispNode(helper.getLevel().dimension(), new BlockPos(0, 0, 2));
        WispNode H = new WispNode(helper.getLevel().dimension(), new BlockPos(1, 0, 2));
        WispNode I = new WispNode(helper.getLevel().dimension(), new BlockPos(2, 0, 2));
        WispNetwork wispNetwork = new WispNetwork(helper.getLevel().dimension(), new BlockPos(3, 0, 0));

        A.TryConnectToNode(B);
        A.TryConnectToNode(D);
        B.TryConnectToNode(C);
        B.TryConnectToNode(E);
        D.TryConnectToNode(G);
        E.TryConnectToNode(F);
        F.TryConnectToNode(I);
        G.TryConnectToNode(H);
        H.TryConnectToNode(I);

        wispNetwork.TryConnectNodeToNetwork(C);

        helper.assertTrue(wispNetwork.TryBuildPathfindingBetweenNodes(A, C), "Failed building path from A to C");
        helper.assertTrue(A.GetPathData().nextNodeToDestinations.get(C).nextPathNode() == B, "Expect the path to from A to C to go to B");
        helper.assertTrue(B.GetPathData().nextNodeToDestinations.get(C).nextPathNode() == C, "Expect the path to from B to C to go to C");

        helper.assertTrue(wispNetwork.TryBuildPathfindingBetweenNodes(G, C), "Failed building path from G to C");
        helper.assertTrue(G.GetPathData().nextNodeToDestinations.get(C).nextPathNode() == D, "Expect the path to from G to C to go to D");
        helper.assertTrue(D.GetPathData().nextNodeToDestinations.get(C).nextPathNode() == A, "Expect the path to from D to C to go to A");

        helper.assertTrue(wispNetwork.TryBuildPathfindingBetweenNodes(I, C), "Failed building path from I to C");
        helper.assertTrue(I.GetPathData().nextNodeToDestinations.get(C).nextPathNode() == F, "Expect the path to from I to C to go to F");
        helper.assertTrue(F.GetPathData().nextNodeToDestinations.get(C).nextPathNode() == E, "Expect the path to from F to C to go to E");
        helper.assertTrue(E.GetPathData().nextNodeToDestinations.get(C).nextPathNode() == B, "Expect the path to from E to C to go to B");

        helper.succeed();
    }

    @GameTest(batch = "WispNetwork", template = "forge:empty3x3x3")
    public static void WispNetworkTest_ProviderUpgrade(@NotNull GameTestHelper helper)
    {
        WispNetwork wispNetwork = new WispNetwork(helper.getLevel().dimension(), new BlockPos(0, 0, 0));

        WispNode node1 = new WispNode(helper.getLevel().dimension(), new BlockPos(10, 0, 0));
        WispNode node2 = new WispNode(helper.getLevel().dimension(), new BlockPos(0, 0, 10));

        ProviderUpgrade node1Upgrade = new ProviderUpgrade();
        ItemStackHandler node1Container = new ItemStackHandler(1);
        node1Container.insertItem(0, new ItemStack(Items.OAK_LOG, 32), false);
        node1Upgrade.LinkToInventory(node1Container);
        node1.AddUpgrade(node1Upgrade);

        helper.assertTrue(wispNetwork.TryConnectNodeToNetwork(node1), "Could not connect node1 to network");
        helper.assertValueEqual(node1Upgrade.GetParentNode(), node1, "Node1Upgrade parent node");
        helper.assertTrue(node1Upgrade.IsActive(), "Expected node1Upgrade to be active");

        ProviderUpgrade node2Upgrade = new ProviderUpgrade();
        ItemStackHandler node2Container = new ItemStackHandler(3);
        node2Container.insertItem(0, new ItemStack(Items.IRON_BLOCK, 64), false);
        helper.assertTrue(wispNetwork.TryConnectNodeToNetwork(node2), "Could not connect node2 to network");
        node2.AddUpgrade(node2Upgrade);
        node2Upgrade.LinkToInventory(node2Container);
        helper.assertValueEqual(node2Upgrade.GetParentNode(), node2, "Node2Upgrade parent node");
        helper.assertTrue(node2Upgrade.IsActive(), "Expected node2Upgrade to be active");

        ResourceManager<ItemStack> resourceManager = wispNetwork.GetResourcesManager().GetResourceManager(ItemStack.class);

        ResourceSource<ItemStack> oakLogSource = resourceManager.FindBestSourceMatchingStack(Items.OAK_LOG.getDefaultInstance(), 0);
        ResourceSource<ItemStack> ironBlockSource = resourceManager.FindBestSourceMatchingStack(Items.IRON_BLOCK.getDefaultInstance(), 0);
        ResourceSource<ItemStack> arrowSource = resourceManager.FindBestSourceMatchingStack(Items.ARROW.getDefaultInstance(), 0);

        helper.assertTrue(oakLogSource != null, "Expect there to be oak logs in the network");
        helper.assertTrue(ironBlockSource != null, "Expect there to be iron blocks in the network");
        helper.assertTrue(arrowSource == null, "Expect there to be no arrows in the network");

        helper.assertValueEqual(oakLogSource.GetNumAvailable(), 32, "wispNetwork oak log sources");
        helper.assertValueEqual(ironBlockSource.GetNumAvailable(), 64, "wispNetwork iron block sources");

        helper.assertValueEqual(resourceManager.CountMatchingStacks(Items.OAK_LOG.getDefaultInstance()), 32, "wispNetwork oak log count");
        helper.assertValueEqual(resourceManager.CountMatchingStacks(Items.IRON_BLOCK.getDefaultInstance()), 64, "wispNetwork iron block count");
        helper.assertValueEqual(resourceManager.CountMatchingStacks(Items.ARROW.getDefaultInstance()), 0, "wispNetwork arrow count");

        node2Container.insertItem(1, new ItemStack(Items.OAK_LOG, 64), false);
        node2Container.insertItem(2, new ItemStack(Items.ARROW, 16), false);

        for(int i = 0; i < 100; ++i)
        {
            wispNetwork.Tick();
        }

        helper.assertValueEqual(oakLogSource.GetNumAvailable(), 32, "wispNetwork oak log sources");
        helper.assertValueEqual(ironBlockSource.GetNumAvailable(), 64, "wispNetwork iron block sources");

        arrowSource = resourceManager.FindBestSourceMatchingStack(Items.ARROW.getDefaultInstance(), 0);

        helper.assertTrue(arrowSource != null, "Expect there to be arrows in the network");
        helper.assertValueEqual(arrowSource.GetNumAvailable(), 16, "wispNetwork arrow sources");

        helper.assertValueEqual(resourceManager.CountMatchingStacks(Items.OAK_LOG.getDefaultInstance()), 96, "wispNetwork oak log count");
        helper.assertValueEqual(resourceManager.CountMatchingStacks(Items.IRON_BLOCK.getDefaultInstance()), 64, "wispNetwork iron block count");
        helper.assertValueEqual(resourceManager.CountMatchingStacks(Items.ARROW.getDefaultInstance()), 16, "wispNetwork arrow count");

        helper.succeed();
    }
}
