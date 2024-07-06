package settop.IgnesFatui.Menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LockedSlot extends Slot
{
    public LockedSlot(Container container, int slotIndex, int x, int y)
    {
        super(container, slotIndex, x, y);
    }
    public LockedSlot(Slot baseSlot)
    {
        this(baseSlot.container, baseSlot.getContainerSlot(), baseSlot.x, baseSlot.y);
        index = baseSlot.index;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack itemStack)
    {
        return false;
    }

    @Override
    public boolean mayPickup(@NotNull Player player)
    {
        return false;
    }
}
