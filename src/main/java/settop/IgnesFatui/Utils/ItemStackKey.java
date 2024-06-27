package settop.IgnesFatui.Utils;

import net.minecraft.world.item.ItemStack;

public class ItemStackKey
{
    private final ItemStack stack;

    public ItemStackKey(ItemStack stack)
    {
        this.stack = stack.copyWithCount(1);
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof ItemStackKey)
        {
            return ItemStack.isSameItemSameComponents(stack, ((ItemStackKey) obj).stack);
        }
        else
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        return ItemStack.hashItemAndComponents(stack);
    }
}
