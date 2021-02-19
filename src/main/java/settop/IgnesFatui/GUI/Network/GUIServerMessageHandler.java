package settop.IgnesFatui.GUI.Network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import settop.IgnesFatui.GUI.BasicWispContainer;
import settop.IgnesFatui.GUI.MultiScreenContainer;
import settop.IgnesFatui.GUI.Network.Packets.*;
import settop.IgnesFatui.GUI.SubContainers.ProviderEnhancementSubContainer;
import settop.IgnesFatui.GUI.SubContainers.SubContainer;
import settop.IgnesFatui.IgnesFatui;

import java.util.List;
import java.util.function.Supplier;

public class GUIServerMessageHandler
{
    public static void OnMessageReceived(final CContainerTabSelected message, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);

        if (sideReceived != LogicalSide.SERVER)
        {
            IgnesFatui.LOGGER.warn("ContainerTabSelected received on wrong side:" + ctx.getDirection().getReceptionSide());
            return;
        }

        final ServerPlayerEntity sendingPlayer = ctx.getSender();
        if (sendingPlayer == null)
        {
            IgnesFatui.LOGGER.warn("EntityPlayerMP was null when ContainerTabSelected was received");
        }

        ctx.enqueueWork(() -> ProcessMessage(message, sendingPlayer));
    }

    private static void ProcessMessage(final CContainerTabSelected message, ServerPlayerEntity sendingPlayer)
    {
        if(sendingPlayer.openContainer == null)
        {
            IgnesFatui.LOGGER.warn("Player does not have open container for ContainerTabSelected");
            return;
        }

        if(sendingPlayer.openContainer.windowId != message.GetWindowID())
        {
            IgnesFatui.LOGGER.warn("Player open container does not match window id for ContainerTabSelected");
            return;
        }

        if(sendingPlayer.openContainer instanceof BasicWispContainer)
        {
            BasicWispContainer basicWispContainer = (BasicWispContainer)sendingPlayer.openContainer;
            basicWispContainer.SelectTab(message.GetTabID());
        }
        else
        {
            IgnesFatui.LOGGER.error("Player open container is not a known container type for OpenEnhancementSubContainerPacket");
        }
    }

    public static void OnMessageReceived(final CSubContainerDirectionChange message, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);

        if (sideReceived != LogicalSide.SERVER)
        {
            IgnesFatui.LOGGER.warn("ProviderContainerDirectionChange received on wrong side:" + ctx.getDirection().getReceptionSide());
            return;
        }

        final ServerPlayerEntity sendingPlayer = ctx.getSender();
        if (sendingPlayer == null)
        {
            IgnesFatui.LOGGER.warn("EntityPlayerMP was null when ProviderContainerDirectionChange was received");
        }

        ctx.enqueueWork(() -> ProcessMessage(message, sendingPlayer));
    }

    private static void ProcessMessage(final CSubContainerDirectionChange message, ServerPlayerEntity sendingPlayer)
    {
        if(sendingPlayer.openContainer == null)
        {
            IgnesFatui.LOGGER.warn("Player does not have open container for SubContainerDirectionChange");
            return;
        }

        if(sendingPlayer.openContainer.windowId != message.GetWindowID())
        {
            IgnesFatui.LOGGER.warn("Player open container does not match window id for SubContainerDirectionChange");
            return;
        }

        if(!(sendingPlayer.openContainer instanceof MultiScreenContainer))
        {
            IgnesFatui.LOGGER.error("Player open container is not a known container type for SubContainerDirectionChange");
            return;
        }

        MultiScreenContainer multiScreenContainer = (MultiScreenContainer)sendingPlayer.openContainer;
        List<SubContainer> subContainers = multiScreenContainer.GetSubContainers();

        if(message.GetSubWindowID() >= subContainers.size())
        {
            IgnesFatui.LOGGER.error("Invalid sub window id for SubContainerDirectionChange");
            return;
        }

        SubContainer subContainer = subContainers.get(message.GetSubWindowID());
        if(subContainer instanceof ProviderEnhancementSubContainer)
        {
            ProviderEnhancementSubContainer providerContainer = (ProviderEnhancementSubContainer)subContainer;
            providerContainer.SetDirectionProvided(message.GetDirection(), message.GetIsSet());
        }
        else
        {
            IgnesFatui.LOGGER.error("Unknown sub container type for SubContainerDirectionChange");
        }
    }

    public static void OnMessageReceived(final CScrollWindowPacket message, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);

        if (sideReceived != LogicalSide.SERVER)
        {
            IgnesFatui.LOGGER.warn("CScrollWindowPacket received on wrong side:" + ctx.getDirection().getReceptionSide());
            return;
        }

        final ServerPlayerEntity sendingPlayer = ctx.getSender();
        if (sendingPlayer == null)
        {
            IgnesFatui.LOGGER.warn("EntityPlayerMP was null when CScrollWindowPacket was received");
        }

        ctx.enqueueWork(() -> ProcessMessage(message, sendingPlayer));
    }

    private static void ProcessMessage(final CScrollWindowPacket message, ServerPlayerEntity sendingPlayer)
    {
        if(sendingPlayer.openContainer == null)
        {
            IgnesFatui.LOGGER.warn("Player does not have open container for CScrollWindowPacket");
            return;
        }

        if(sendingPlayer.openContainer.windowId != message.GetWindowID())
        {
            IgnesFatui.LOGGER.warn("Player open container does not match window id for CScrollWindowPacket");
            return;
        }

        if(!(sendingPlayer.openContainer instanceof MultiScreenContainer))
        {
            IgnesFatui.LOGGER.error("Player open container is not a known container type for CScrollWindowPacket");
            return;
        }

        MultiScreenContainer multiScreenContainer = (MultiScreenContainer)sendingPlayer.openContainer;
        multiScreenContainer.mouseScrolled(message.GetSlotID(), 0, 0, message.GetDelta());
    }


    public static void OnMessageReceived(final CSubWindowPropertyUpdatePacket message, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);

        if (sideReceived != LogicalSide.SERVER)
        {
            IgnesFatui.LOGGER.warn("SubWindowPropertyUpdatePacket received on wrong side:" + ctx.getDirection().getReceptionSide());
            return;
        }

        final ServerPlayerEntity sendingPlayer = ctx.getSender();
        if (sendingPlayer == null)
        {
            IgnesFatui.LOGGER.warn("EntityPlayerMP was null when SubWindowPropertyUpdatePacket was received");
        }

        ctx.enqueueWork(() -> ProcessMessage(message, sendingPlayer));
    }

    private static void ProcessMessage(final CSubWindowPropertyUpdatePacket message, ServerPlayerEntity sendingPlayer)
    {
        if(sendingPlayer.openContainer == null)
        {
            IgnesFatui.LOGGER.warn("Player does not have open container for SubWindowPropertyUpdatePacket");
            return;
        }

        if(sendingPlayer.openContainer.windowId != message.GetWindowID())
        {
            IgnesFatui.LOGGER.warn("Player open container does not match window id for SubWindowPropertyUpdatePacket");
            return;
        }

        if(!(sendingPlayer.openContainer instanceof MultiScreenContainer))
        {
            IgnesFatui.LOGGER.error("Player open container is not a known container type for SubWindowPropertyUpdatePacket");
            return;
        }

        MultiScreenContainer multiScreenContainer = (MultiScreenContainer)sendingPlayer.openContainer;
        List<SubContainer> subContainers = multiScreenContainer.GetSubContainers();

        if(message.GetSubWindowID() >= subContainers.size())
        {
            IgnesFatui.LOGGER.error("Invalid sub window id for SubWindowPropertyUpdatePacket");
            return;
        }

        SubContainer subContainer = subContainers.get(message.GetSubWindowID());
        if(subContainer == null)
        {
            IgnesFatui.LOGGER.error("Invalid sub window for SubWindowPropertyUpdatePacket");
            return;
        }

        subContainer.HandlePropertyUpdate(message.GetPropertyID(), message.GetValue());
    }


    public static void OnMessageReceived(final CSubWindowStringPropertyUpdatePacket message, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);

        if (sideReceived != LogicalSide.SERVER)
        {
            IgnesFatui.LOGGER.warn("SubWindowStringPropertyUpdatePacket received on wrong side:" + ctx.getDirection().getReceptionSide());
            return;
        }

        final ServerPlayerEntity sendingPlayer = ctx.getSender();
        if (sendingPlayer == null)
        {
            IgnesFatui.LOGGER.warn("EntityPlayerMP was null when SubWindowStringPropertyUpdatePacket was received");
        }

        ctx.enqueueWork(() -> ProcessMessage(message, sendingPlayer));
    }

    private static void ProcessMessage(final CSubWindowStringPropertyUpdatePacket message, ServerPlayerEntity sendingPlayer)
    {
        if(sendingPlayer.openContainer == null)
        {
            IgnesFatui.LOGGER.warn("Player does not have open container for SubWindowStringPropertyUpdatePacket");
            return;
        }

        if(sendingPlayer.openContainer.windowId != message.GetWindowID())
        {
            IgnesFatui.LOGGER.warn("Player open container does not match window id for SubWindowStringPropertyUpdatePacket");
            return;
        }

        if(!(sendingPlayer.openContainer instanceof MultiScreenContainer))
        {
            IgnesFatui.LOGGER.error("Player open container is not a known container type for SubWindowStringPropertyUpdatePacket");
            return;
        }

        MultiScreenContainer multiScreenContainer = (MultiScreenContainer)sendingPlayer.openContainer;
        List<SubContainer> subContainers = multiScreenContainer.GetSubContainers();

        if(message.GetSubWindowID() >= subContainers.size())
        {
            IgnesFatui.LOGGER.error("Invalid sub window id for SubWindowStringPropertyUpdatePacket");
            return;
        }

        SubContainer subContainer = subContainers.get(message.GetSubWindowID());
        if(subContainer == null)
        {
            IgnesFatui.LOGGER.error("Invalid sub window for SubWindowStringPropertyUpdatePacket");
            return;
        }

        subContainer.HandleStringPropertyUpdate(message.GetPropertyID(), message.GetValue());
    }
}
