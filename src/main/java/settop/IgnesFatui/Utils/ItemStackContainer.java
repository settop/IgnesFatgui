package settop.IgnesFatui.Utils;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class ItemStackContainer extends SimpleContainer
{
    private final ItemStack itemStack;

    public ItemStackContainer(@NonNull ItemStack itemStack, int numSlots)
    {
        super(numSlots);
        this.itemStack = itemStack;
        ItemContainerContents itemContainer = itemStack.get(DataComponents.CONTAINER);
        if(itemContainer != null)
        {
            itemContainer.copyInto(getItems());
        }
    }

    @Override
    public void setChanged()
    {
        super.setChanged();
        itemStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(getItems()));
    }
}
