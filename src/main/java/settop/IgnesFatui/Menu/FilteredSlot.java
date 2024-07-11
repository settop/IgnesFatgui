package settop.IgnesFatui.Menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class FilteredSlot extends Slot
{
    private final Predicate<ItemStack> filter;

    public FilteredSlot(Container container, int slotIndex, int x, int y, Predicate<ItemStack> filter)
    {
        super(container, slotIndex, x, y);
        this.filter = filter;
    }
    public FilteredSlot(Slot baseSlot, Predicate<ItemStack> filter)
    {
        this(baseSlot.container, baseSlot.getContainerSlot(), baseSlot.x, baseSlot.y, filter);
        index = baseSlot.index;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack itemStack)
    {
        return filter.test(itemStack);
    }
}
