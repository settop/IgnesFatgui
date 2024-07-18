package settop.IgnesFatui.WispNetwork.Resource;

import net.minecraft.core.Direction;
import settop.IgnesFatui.Utils.Invalidable;

import java.util.ArrayList;

public class ResourceSource<T> extends Invalidable
{
    public interface Listener
    {
        void CountChanged(int change);
    }
    public final int priority;
    private int numAvailable;
    private int nextNumAvailable;
    private ArrayList<Listener> listeners = new ArrayList<>();

    public ResourceSource(int priority, int numAvailable)
    {
        assert numAvailable >= 0;
        this.priority = priority;
        this.numAvailable = numAvailable;
        this.nextNumAvailable = 0;
    }

    public void AddListener(Listener listener)
    {
        listeners.add(listener);
    }

    public int GetNumAvailable()
    {
        return numAvailable;
    }

    public void AccumulateNumAvailable(int newCount)
    {
        assert newCount >= 0;
        nextNumAvailable += newCount;
    }

    public boolean Update()
    {
        boolean updated = false;
        if(nextNumAvailable != numAvailable)
        {
            int change = nextNumAvailable - numAvailable;
            for (Listener listener : listeners)
            {
                listener.CountChanged(change);
            }
            numAvailable = nextNumAvailable;
            updated = true;
        }
        nextNumAvailable = 0;
        return updated;
    }

    public boolean HasChanged()
    {
        return nextNumAvailable != numAvailable;
    }

    @Override
    public void SetInvalid()
    {
        super.SetInvalid();
        for (Listener listener : listeners)
        {
            listener.CountChanged(-numAvailable);
        }
        numAvailable = 0;
    }

    //abstract public Reservation ReserveExtract(StoreableResourceMatcher<T> extractionMatcher, int count);
    //abstract public T Extract(Reservation reservation, StoreableResourceMatcher<T> extractionMatcher, int count);
    //abstract public WispInteractionNodeBase GetAttachedInteractionNode();
    //abstract public Direction GetExtractionDirection();
}
