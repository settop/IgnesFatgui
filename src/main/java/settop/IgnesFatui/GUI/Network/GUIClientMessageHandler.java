package settop.IgnesFatui.GUI.Network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import settop.IgnesFatui.Client.Screens.MultiScreen;
import settop.IgnesFatui.GUI.MultiScreenContainer;
import settop.IgnesFatui.GUI.Network.Packets.CSubWindowStringPropertyUpdatePacket;
import settop.IgnesFatui.GUI.Network.Packets.SWindowStringPropertyPacket;
import settop.IgnesFatui.GUI.SubContainers.SubContainer;
import settop.IgnesFatui.IgnesFatui;

import java.util.List;
import java.util.function.Supplier;

public class GUIClientMessageHandler
{

    public static void OnMessageReceived(final SWindowStringPropertyPacket message, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);

        if (sideReceived != LogicalSide.CLIENT)
        {
            IgnesFatui.LOGGER.warn("SWindowStringPropertyPacket received on wrong side:" + ctx.getDirection().getReceptionSide());
            return;
        }

        ctx.enqueueWork(() -> ProcessMessage(message));
    }

    private static void ProcessMessage(final SWindowStringPropertyPacket message)
    {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if(player.openContainer == null)
        {
            IgnesFatui.LOGGER.warn("Player does not have open container for SWindowStringPropertyPacket");
            return;
        }

        if(player.openContainer.windowId != message.GetWindowID())
        {
            IgnesFatui.LOGGER.warn("Player open container does not match window id for SWindowStringPropertyPacket");
            return;
        }

        if(!(player.openContainer instanceof MultiScreenContainer))
        {
            IgnesFatui.LOGGER.error("Player open container is not a known container type for SWindowStringPropertyPacket");
            return;
        }

        MultiScreenContainer multiScreenContainer = (MultiScreenContainer)player.openContainer;

        multiScreenContainer.updateTrackedString(message.GetPropertyID(), message.GetValue());
    }
}
