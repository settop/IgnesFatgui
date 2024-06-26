package settop.IgnesFatui.WispNetwork.Tasks;

import settop.IgnesFatui.WispNetwork.Task;

import java.util.ArrayDeque;

public class SequentialTask extends Task
{
    private final ArrayDeque<Task> taskSequence = new ArrayDeque<>();

    public void QueueTask(Task task)
    {
        taskSequence.offer(task);
    }

    @Override
    public int Tick(int extraTicks)
    {
        Task nextTask = taskSequence.peek();
        while (nextTask != null)
        {
            int nextTickWait = nextTask.Tick(extraTicks);
            if(nextTask.IsFinished())
            {
                taskSequence.pop();
                nextTask = taskSequence.peek();
                if(nextTickWait <= 0)
                {
                    extraTicks = -nextTickWait;
                }
                else
                {
                    return nextTickWait;
                }
            }
            else
            {
                return nextTickWait;
            }
        }
        SetFinished();
        return -extraTicks;
    }
}
