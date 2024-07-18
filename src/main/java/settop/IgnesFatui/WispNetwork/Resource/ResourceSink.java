package settop.IgnesFatui.WispNetwork.Resource;

import net.minecraft.core.Direction;
import settop.IgnesFatui.Utils.Invalidable;

public abstract class ResourceSink<T> extends Invalidable
{
    public final int priority;
    public final ResourceFilter<T> filter;

    public ResourceSink(int priority)
    {
        this.priority = priority;
        this.filter = null;
    }

    public ResourceSink(ResourceFilter<T> filter, int priority)
    {
        this.priority = priority;
        this.filter = filter;
    }

    //returns true if the insert is partial as well
    abstract public boolean CanInsert(T stack);

    //returns the reservation or null is no reservation was made
    //abstract public Reservation ReserveInsert(T stack);
    //abstract public T Insert(Reservation reservation, T stack);
    //abstract public WispInteractionNodeBase GetAttachedInteractionNode();
    //abstract public Direction GetInsertDirection();
}
