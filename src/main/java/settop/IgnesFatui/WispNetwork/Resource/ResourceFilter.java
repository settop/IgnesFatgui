package settop.IgnesFatui.WispNetwork.Resource;

import org.jetbrains.annotations.NotNull;

public abstract class ResourceFilter<T>
{
    private final Class<?> stackClass;

    protected ResourceFilter(Class<?> stackClass)
    {
        this.stackClass = stackClass;
    }

    public Class<?> GetStackClass()
    {
        return stackClass;
    }

    public abstract boolean Matches(@NotNull T resource);
}
