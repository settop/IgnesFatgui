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
    public static void WispNetworkTest_ProviderUpgrade(@NotNull GameTestHelper helper)
    {
        WispNetwork wispNetwork = new WispNetwork(helper.getLevel().dimension(), new BlockPos(0, 0, 0));

        WispNode node1 = new WispNode(helper.getLevel().dimension(), new BlockPos(10, 0, 0));
        WispNode node2 = new WispNode(helper.getLevel().dimension(), new BlockPos(0, 0, 10));

        ProviderUpgrade node1Upgrade = new ProviderUpgrade();
        SimpleContainer node1Container = new SimpleContainer(new ItemStack(Items.OAK_LOG, 32));
        node1Upgrade.LinkToInventory(node1Container);
        node1.AddUpgrade(node1Upgrade);

        helper.assertTrue(wispNetwork.TryConnectNodeToNetwork(node1), "Could not connect node1 to network");
        helper.assertValueEqual(node1Upgrade.GetParentNode(), node1, "Node1Upgrade parent node");
        helper.assertTrue(node1Upgrade.IsActive(), "Expected node1Upgrade to be active");

        ProviderUpgrade node2Upgrade = new ProviderUpgrade();
        SimpleContainer node2Container = new SimpleContainer(new ItemStack(Items.IRON_BLOCK, 64));
        helper.assertTrue(wispNetwork.TryConnectNodeToNetwork(node2), "Could not connect node2 to network");
        node2.AddUpgrade(node2Upgrade);
        node2Upgrade.LinkToInventory(node2Container);
        helper.assertValueEqual(node2Upgrade.GetParentNode(), node2, "Node2Upgrade parent node");
        helper.assertTrue(node2Upgrade.IsActive(), "Expected node2Upgrade to be active");


        helper.succeed();
    }
}
