package settop.IgnesFatui.WispNetwork.Resource;

public interface ResourceFilter<T>
{
    boolean Matches(T resource);
}
