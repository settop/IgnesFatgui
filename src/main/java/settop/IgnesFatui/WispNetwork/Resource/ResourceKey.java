package settop.IgnesFatui.WispNetwork.Resource;

import net.minecraft.world.item.ItemStack;

public abstract class ResourceKey
{
    private final Class<?> stackClass;

    protected ResourceKey(Class<?> stackClass)
    {
        this.stackClass = stackClass;
    }

    public final Class<?> GetStackClass()
    {
        return stackClass;
    }

    public abstract Object GetStack();

    @Override
    public abstract int hashCode();
    @Override
    public abstract boolean equals(Object obj);
}
