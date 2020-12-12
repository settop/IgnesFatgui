package settop.IgnesFatui.GUI.Network.Packets;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;

public class ProviderContainerDirectionChange
{
    private final int windowID;
    private final Direction direction;
    private final boolean isSet;

    public ProviderContainerDirectionChange(int inWindowID, Direction inDirection, boolean inIsSet)
    {
        windowID = inWindowID;
        direction = inDirection;
        isSet = inIsSet;
    }

    public static ProviderContainerDirectionChange decode(PacketBuffer buf)
    {
        int windowID = buf.readInt();
        int direction = buf.readInt();
        boolean isSet = buf.readBoolean();

        ProviderContainerDirectionChange retval = new ProviderContainerDirectionChange(windowID, Direction.byIndex(direction), isSet);

        return retval;
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeInt(windowID);
        buf.writeInt(direction.ordinal());
        buf.writeBoolean(isSet);
    }

    public int GetWindowID()
    {
        return windowID;
    }
    public Direction GetDirection()
    {
        return direction;
    }
    public boolean GetIsSet()
    {
        return isSet;
    }
}
