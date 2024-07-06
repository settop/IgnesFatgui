package settop.IgnesFatui.Network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Network.Packets.SWindowStringPropertyPacket;

public class PacketHandler
{
    private static final int GUI_CHANNEL_VERSION = 1;
    private static final SimpleChannel GUI_CHANNEL = ChannelBuilder.named(new ResourceLocation(IgnesFatui.MOD_ID, "gui_channel"))
            .networkProtocolVersion(GUI_CHANNEL_VERSION)
            .clientAcceptedVersions(((status, version) -> GUI_CHANNEL_VERSION == version))
            .serverAcceptedVersions(((status, version) -> GUI_CHANNEL_VERSION == version))
            .simpleChannel();

    public static void Register()
    {
        GUI_CHANNEL.messageBuilder(SWindowStringPropertyPacket.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SWindowStringPropertyPacket::encode)
                .decoder(SWindowStringPropertyPacket::decode)
                .consumerMainThread(SWindowStringPropertyPacket::Handle);
    }


    @OnlyIn(Dist.DEDICATED_SERVER)
    public static void SendStringUpdate(ServerPlayer targetPlayer, int windowID, int propertyID, String value)
    {
        GUI_CHANNEL.send(new SWindowStringPropertyPacket(windowID, propertyID, value), PacketDistributor.PLAYER.with(targetPlayer));
    }
}
