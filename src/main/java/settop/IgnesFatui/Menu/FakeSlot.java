package settop.IgnesFatui.Menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;
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
    public boolean isFake()
    {
        return true;
    }

    @Override
    public void SetActive(boolean active)
    {
        isActive = active;
    }

    @Override
    public boolean isActive()
    {
        return isActive;
    }

    @Override
    public int getMaxStackSize()
    {
        return includeCounts ? FakeInventory.MAX_STACK : 1;
    }

    @Override
    public boolean mayPickup(@NotNull Player playerIn)
    {
        return false;
    }
}
