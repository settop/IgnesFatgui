package settop.IgnesFatui.GUI;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import settop.IgnesFatui.Utils.FakeInventory;

public class FakeSlot extends Slot implements IActivatableSlot
{
    private boolean isActive = true;
    public final boolean includeCounts;

    public FakeSlot(FakeInventory inventory, int index, int xPosition, int yPosition)
    {
        super(inventory, index, xPosition, yPosition);
        this.includeCounts = inventory.includeCounts;
    }

    @Override
    public void SetActive(boolean active)
    {
        isActive = active;
    }

    @Override
    public boolean isEnabled()
    {
        return isActive;
    }

    @Override
    public int getSlotStackLimit()
    {
        return includeCounts ? FakeInventory.MAX_STACK : 1;
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn)
    {
        return false;
    }
}
