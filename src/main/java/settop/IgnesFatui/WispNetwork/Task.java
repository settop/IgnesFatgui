package settop.IgnesFatui.WispNetwork;

public abstract class Task
{
    private boolean isFinished = false;
    //Tick the task
    //extraTicks - the number of extra ticks this task had to wait before it got ticked
    //returns the number of ticks to wait until the task should next be ticked, can be negative(return 1 to tick once per frame)
    abstract public int Tick(int extraTicks);

    public boolean IsFinished()
    {
        return isFinished;
    }

    public void SetFinished()
    {
        isFinished = true;
    }
}
