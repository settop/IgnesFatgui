package settop.IgnesFatui.Testing;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.gametest.GameTestHolder;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Utils.ItemStackKey;
import settop.IgnesFatui.WispNetwork.Upgrades.ProviderUpgrade;
import settop.IgnesFatui.WispNetwork.WispNetwork;
import settop.IgnesFatui.WispNetwork.WispNetworkItemSources;
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
        SimpleContainer node1Container = new SimpleContainer(1);
        node1Container.addItem(new ItemStack(Items.OAK_LOG, 32));
        node1Upgrade.LinkToInventory(node1Container);
        node1.AddUpgrade(node1Upgrade);

        helper.assertTrue(wispNetwork.TryConnectNodeToNetwork(node1), "Could not connect node1 to network");
        helper.assertValueEqual(node1Upgrade.GetParentNode(), node1, "Node1Upgrade parent node");
        helper.assertTrue(node1Upgrade.IsActive(), "Expected node1Upgrade to be active");

        ProviderUpgrade node2Upgrade = new ProviderUpgrade();
        SimpleContainer node2Container = new SimpleContainer(3);
        node2Container.addItem(new ItemStack(Items.IRON_BLOCK, 64));
        helper.assertTrue(wispNetwork.TryConnectNodeToNetwork(node2), "Could not connect node2 to network");
        node2.AddUpgrade(node2Upgrade);
        node2Upgrade.LinkToInventory(node2Container);
        helper.assertValueEqual(node2Upgrade.GetParentNode(), node2, "Node2Upgrade parent node");
        helper.assertTrue(node2Upgrade.IsActive(), "Expected node2Upgrade to be active");

        WispNetworkItemSources oakLogSources = wispNetwork.FindItemSource(new ItemStackKey(new ItemStack(Items.OAK_LOG, 1)));
        WispNetworkItemSources ironBlockSources = wispNetwork.FindItemSource(new ItemStackKey(new ItemStack(Items.IRON_BLOCK, 1)));
        WispNetworkItemSources arrowSources = wispNetwork.FindItemSource(new ItemStackKey(new ItemStack(Items.ARROW, 1)));

        helper.assertTrue(oakLogSources != null, "Expect there to be oak logs in the network");
        helper.assertTrue(ironBlockSources != null, "Expect there to be iron blocks in the network");
        helper.assertTrue(arrowSources == null, "Expect there to be no arrows in the network");

        helper.assertValueEqual(oakLogSources.GetTotalCount(), 32, "wispNetwork oak log sources");
        helper.assertValueEqual(ironBlockSources.GetTotalCount(), 64, "wispNetwork iron block sources");

        node2Container.addItem(new ItemStack(Items.OAK_LOG, 64));
        node2Container.addItem(new ItemStack(Items.ARROW, 16));

        for(int i = 0; i < 100; ++i)
        {
            wispNetwork.Tick();
        }

        helper.assertValueEqual(oakLogSources.GetTotalCount(), 96, "wispNetwork oak log sources");
        helper.assertValueEqual(ironBlockSources.GetTotalCount(), 64, "wispNetwork iron block sources");

        arrowSources = wispNetwork.FindItemSource(new ItemStackKey(new ItemStack(Items.ARROW, 1)));

        helper.assertTrue(arrowSources != null, "Expect there to be arrows in the network");
        helper.assertValueEqual(arrowSources.GetTotalCount(), 16, "wispNetwork arrow sources");

        helper.succeed();
    }
}