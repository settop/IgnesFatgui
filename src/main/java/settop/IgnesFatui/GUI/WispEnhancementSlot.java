package settop.IgnesFatui.GUI;

import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Items.WispEnhancementItem;
import settop.IgnesFatui.Wisps.BasicWispContents;

public class WispEnhancementSlot extends Slot implements IActivatableSlot
{
    private boolean isActive = true;

    public WispEnhancementSlot(BasicWispContents contentsIn, int index, int xPosition, int yPosition)
    {
        super(contentsIn, index, xPosition, yPosition);
     }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        for(int i = 0; i < inventory.getSizeInventory(); ++i)
        {
            if(inventory.getStackInSlot(i).getItem() == stack.getItem())
            {
                //only one of each enhancement per wisp contents
                return false;
            }
        }
        return stack.getItem() instanceof WispEnhancementItem;
    }

    @Override
    public int getSlotStackLimit()
    {
        return 1;
    }

    @Override
    public boolean isEnabled()
    {
        return isActive;
    }

    @Override
    public void SetActive(boolean active)
    {
        isActive = active;
    }
}
