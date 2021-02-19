package settop.IgnesFatui.GUI.Network.Packets;

import net.minecraft.network.PacketBuffer;

public class SWindowStringPropertyPacket
{
    private final int windowID;
    private final int propertyID;
    private final String value;

    public SWindowStringPropertyPacket(int windowID, int propertyID, String value)
    {
        this.windowID = windowID;
        this.propertyID = propertyID;
        this.value = value;
    }

    public static SWindowStringPropertyPacket decode(PacketBuffer buf)
    {
        int windowID = buf.readInt();
        int propertyID = buf.readInt();
        String value = buf.readString();

        SWindowStringPropertyPacket retval = new SWindowStringPropertyPacket(windowID, propertyID, value);

        return retval;
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeInt(windowID);
        buf.writeInt(propertyID);
        buf.writeString(value);
    }

    public int GetWindowID()
    {
        return windowID;
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
