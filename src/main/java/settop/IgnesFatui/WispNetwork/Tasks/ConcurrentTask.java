package settop.IgnesFatui.WispNetwork.Tasks;

import settop.IgnesFatui.WispNetwork.Task;
import settop.IgnesFatui.WispNetwork.TaskManager;

import java.util.ArrayList;

public class ConcurrentTask extends Task
{
    private final TaskManager taskManager = new TaskManager();
    private int lastTickWaitTime = 0;

    public void AddTask(Task task)
    {
        taskManager.AddTask(task);
    }

    @Override
    public int Tick(int extraTicks)
    {
        int nextWaitTime = taskManager.Tick(lastTickWaitTime + extraTicks, 1.f);
        lastTickWaitTime = Integer.max(nextWaitTime, 0);
        if(!taskManager.HasAnyTasks())
        {
            SetSuccessful();
        }
        return nextWaitTime;
    }
}
