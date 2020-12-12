package settop.IgnesFatui.GUI.Network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import settop.IgnesFatui.GUI.BasicWispContainer;
import settop.IgnesFatui.GUI.MultiScreenContainer;
import settop.IgnesFatui.GUI.Network.Packets.ContainerTabSelected;
import settop.IgnesFatui.GUI.Network.Packets.ProviderContainerDirectionChange;
import settop.IgnesFatui.GUI.SubContainers.ProviderEnhancementSubContainer;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Wisps.Enhancements.EnhancementTypes;

import java.util.function.Supplier;

public class GUIServerMessageHandler
{
    public static void OnMessageReceived(final ContainerTabSelected message, Supplier<NetworkEvent.Context> ctxSupplier)
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

    private static void ProcessMessage(final ContainerTabSelected message, ServerPlayerEntity sendingPlayer)
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

    public static void OnMessageReceived(final ProviderContainerDirectionChange message, Supplier<NetworkEvent.Context> ctxSupplier)
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

    private static void ProcessMessage(final ProviderContainerDirectionChange message, ServerPlayerEntity sendingPlayer)
    {
        if(sendingPlayer.openContainer == null)
        {
            IgnesFatui.LOGGER.warn("Player does not have open container for ProviderContainerDirectionChange");
            return;
        }

        if(sendingPlayer.openContainer.windowId != message.GetWindowID())
        {
            IgnesFatui.LOGGER.warn("Player open container does not match window id for ProviderContainerDirectionChange");
            return;
        }

        if(sendingPlayer.openContainer instanceof BasicWispContainer)
        {
            BasicWispContainer basicWispContainer = (BasicWispContainer)sendingPlayer.openContainer;
            ProviderEnhancementSubContainer providerContainer = (ProviderEnhancementSubContainer)basicWispContainer.GetEnhancementSubContainer( EnhancementTypes.PROVIDER );
            providerContainer.SetDirectionProvided(message.GetDirection(), message.GetIsSet());
        }
        else
        {
            IgnesFatui.LOGGER.error("Player open container is not a known container type for OpenEnhancementSubContainerPacket");
        }
    }
}
