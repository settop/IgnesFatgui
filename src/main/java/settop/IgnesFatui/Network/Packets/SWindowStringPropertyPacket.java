package settop.IgnesFatui.Network.Packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.nio.charset.Charset;

public class SWindowStringPropertyPacket
{
    private final int containerID;
    private final int propertyID;
    private final String value;

    public SWindowStringPropertyPacket(int containerID, int propertyID, String value)
    {
        this.containerID = containerID;
        this.propertyID = propertyID;
        this.value = value;
    }

    public static SWindowStringPropertyPacket decode(FriendlyByteBuf buf)
    {
        int windowID = buf.readInt();
        int propertyID = buf.readInt();
        String value = buf.readUtf();

        return new SWindowStringPropertyPacket(windowID, propertyID, value);
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeInt(containerID);
        buf.writeInt(propertyID);
        buf.writeUtf(value);
    }

    public void Handle(CustomPayloadEvent.Context context)
    {
        context.setPacketHandled(true);
    }

    public int GetContainerID()
    {
        return containerID;
    }
    public int GetPropertyID()
    {
        return propertyID;
    }
    public String GetValue()
    {
        return value;
    }
}
