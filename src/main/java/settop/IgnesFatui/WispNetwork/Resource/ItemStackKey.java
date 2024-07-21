package settop.IgnesFatui.WispNetwork.Resource;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemStackKey extends ResourceKey
{
    private final ItemStack stack;

    public ItemStackKey(@Nonnull ItemStack stack)
    {
        super(ItemStack.class);
        this.stack = stack.copyWithCount(1);
    }

    public ItemStack GetItemStack()
    {
        return stack;
    }

    @Override
    public Object GetStack()
    {
        return stack;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ItemStackKey otherStackKey)
        {
            return ItemStack.isSameItemSameComponents(stack, otherStackKey.stack);
        }
        else if (obj instanceof ItemStack otherStack)
        {
            return ItemStack.isSameItemSameComponents(stack, otherStack);
        }
        else
        {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(stack);
    }
}
