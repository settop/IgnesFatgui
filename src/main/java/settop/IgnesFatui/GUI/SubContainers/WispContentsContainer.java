package settop.IgnesFatui.GUI.SubContainers;

import settop.IgnesFatui.Client.Client;
import settop.IgnesFatui.Client.Screens.MultiScreen;
import settop.IgnesFatui.Client.Screens.SubScreens.SubScreen;
import settop.IgnesFatui.Client.Screens.SubScreens.WispContentsSubScreen;
import settop.IgnesFatui.GUI.WispEnhancementSlot;
import settop.IgnesFatui.Wisps.BasicWispContents;

public class WispContentsContainer extends SubContainer
{
    public WispContentsContainer(BasicWispContents inWispContents, int xPos, int yPos)
    {
        super(xPos, yPos);
        for(int i = 0; i < inWispContents.getSizeInventory(); ++i)
        {
            inventorySlots.add(new WispEnhancementSlot(inWispContents, i, xPos + Client.SLOT_X_SPACING * i, yPos));
        }
    }

    @Override
    public SubScreen CreateScreen(MultiScreen<?> parentScreen)
    {
        return new WispContentsSubScreen(this, parentScreen);
    }
}
