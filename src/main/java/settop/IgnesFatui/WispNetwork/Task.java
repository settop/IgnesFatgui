package settop.IgnesFatui.WispNetwork;

public abstract class Task
{
    public enum State
    {
        ACTIVE,
        SUCCESS,
        FAILED
    }
    private State state = State.ACTIVE;
    //Tick the task
    //extraTicks - the number of extra ticks this task had to wait before it got ticked
    //returns the number of ticks to wait until the task should next be ticked, can be negative(return 1 to tick once per frame)
    abstract public int Tick(int extraTicks);

    public boolean IsFinished()
    {
        return state != State.ACTIVE;
    }

    public boolean IsSuccessful()
    {
        return state == State.SUCCESS;
    }

    public boolean IsFailed()
    {
        return state == State.FAILED;
    }

    public void SetSuccessful()
    {
        state = State.SUCCESS;
    }

    public void SetFailed()
    {
        state = State.FAILED;
    }
}
