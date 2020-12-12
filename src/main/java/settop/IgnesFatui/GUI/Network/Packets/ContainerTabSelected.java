package settop.IgnesFatui.GUI.Network.Packets;

import net.minecraft.network.PacketBuffer;

public class ContainerTabSelected
{
    private final int windowID;
    private final int tabID;

    public ContainerTabSelected(int inWindowID, int inTabID)
    {
        windowID = inWindowID;
        tabID = inTabID;
    }

    public static ContainerTabSelected decode(PacketBuffer buf)
    {
        int windowID = buf.readInt();
        int tabID = buf.readInt();

        ContainerTabSelected retval = new ContainerTabSelected(windowID, tabID);

        return retval;
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeInt(windowID);
        buf.writeInt(tabID);
    }

    public int GetWindowID()
    {
        return windowID;
    }

    public int GetTabID()
    {
        return tabID;
    }
}
