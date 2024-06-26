package settop.IgnesFatui.WispNetwork;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.ListIterator;

public class TaskManager
{
    private static class TaskData
    {
        public Task task;
        public int nextTickTime = 0;
    }
    //tasks are sorted with the lowest nextTickTime at the end
    private ArrayList<TaskData> tasks = new ArrayList<>();
    private int currentTick = 0;

    public void AddTask(Task task)
    {
        TaskData taskData = new TaskData();
        taskData.task = task;
        taskData.nextTickTime = currentTick;

        final int numTasks = tasks.size();
        for(int i = 0; i < numTasks; ++i)
        {
            final int index = numTasks - 1 - i;
            if(taskData.nextTickTime <= tasks.get(index).nextTickTime)
            {
                tasks.add(index + 1, taskData);
                return;
            }
        }
        tasks.addFirst(taskData);
    }

    public boolean HasAnyTasks()
    {
        return !tasks.isEmpty();
    }

    public int Tick(float maxProportionTasksToTick)
    {
        return Tick(1, maxProportionTasksToTick);
    }

    public int Tick(int numTicks, float maxProportionTasksToTick)
    {
        if(numTicks < 0)
        {
            throw new InvalidParameterException("numTicks needs to be >= 0");
        }
        currentTick += numTicks;
        final int numTasks = tasks.size();
        if(numTasks == 0)
        {
            return 0;
        }
        int numTasksToTick = (int) (numTasks * maxProportionTasksToTick);
        if(numTasksToTick < 1)
        {
            numTasksToTick = 1;
        }
        else if(numTasksToTick > numTasks)
        {
            numTasksToTick = numTasks;
        }

        int largestFinishedTaskRemainder = Integer.MIN_VALUE;
        ListIterator<TaskData> it = tasks.listIterator(numTasks);
        for(int i = 0; i < numTasksToTick; ++i)
        {
            TaskData taskData = it.previous();
            if(currentTick >= taskData.nextTickTime)
            {
                int timeUntilNextTick = taskData.task.Tick(currentTick - taskData.nextTickTime);
                if(taskData.task.IsFinished())
                {
                    largestFinishedTaskRemainder = Integer.max(largestFinishedTaskRemainder, timeUntilNextTick);
                    it.remove();
                }
                else
                {
                    taskData.nextTickTime = currentTick + timeUntilNextTick;
                }
            }
            else
            {
                break;
            }
        }
        // can potentially improve by using a merge sort
        // since we know we can split into two range the existing sorted range and the unsorted range of the tasks that were ticked
        tasks.sort((l, r) -> r.nextTickTime - l.nextTickTime);
        if(tasks.isEmpty())
        {
            return largestFinishedTaskRemainder == Integer.MIN_VALUE ? 0 : largestFinishedTaskRemainder;
        }
        else
        {
            return tasks.getLast().nextTickTime - currentTick;
        }
    }
}
