package settop.IgnesFatui.WispNetwork.Tasks;

import settop.IgnesFatui.WispNetwork.Task;

public abstract class PeriodicTask extends Task
{
    int updatesWithoutChange = 0;
    final int idealTickTime;
    static final int MAX_UPDATE_TIME = 20 * 5;

    protected PeriodicTask(int idealTickTime)
    {
        this.idealTickTime = idealTickTime;
    }

    @Override
    public int Tick(int extraTicks)
    {
        if(IsFinished())
        {
            return 0;
        }
        if(TryDoWork())
        {
            updatesWithoutChange = 0;
            return idealTickTime - extraTicks;
        }
        else
        {
            ++updatesWithoutChange;
            //note that sleep multiplier level takes longer to reach as the updates become slower
            int sleepMultiplier = 1 << (updatesWithoutChange / 4);
            return Integer.min(idealTickTime * sleepMultiplier, MAX_UPDATE_TIME) - extraTicks;
        }
    }

    public abstract boolean TryDoWork();
}
