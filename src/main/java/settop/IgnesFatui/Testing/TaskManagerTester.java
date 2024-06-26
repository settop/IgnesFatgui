package settop.IgnesFatui.Testing;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestDontPrefix;
import net.minecraftforge.gametest.GameTestHolder;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.WispNetwork.Task;
import settop.IgnesFatui.WispNetwork.TaskManager;
import settop.IgnesFatui.WispNetwork.Tasks.ConcurrentTask;
import settop.IgnesFatui.WispNetwork.Tasks.SequentialTask;

@GameTestHolder(IgnesFatui.MOD_ID)
public class TaskManagerTester
{
    public static class WaitTask extends Task
    {
        final int initialWaitTime;
        boolean initialTick = true;

        public WaitTask(int initialWaitTime)
        {
            this.initialWaitTime = initialWaitTime;
        }

        @Override
        public int Tick(int extraTicks)
        {
            if(initialTick)
            {
                initialTick = false;
                int remaining = initialWaitTime - extraTicks;
                if(remaining <= 0)
                {
                    SetFinished();
                }
                return remaining;
            }
            else
            {
                SetFinished();
                return -extraTicks;
            }
        }
    }

    @GameTest(batch = "TaskManager", template = "forge:empty3x3x3", timeoutTicks = 15)
    public static void TaskManagerTest_SingleWait(@NotNull GameTestHelper helper)
    {
        TaskManager taskManager = new TaskManager();
        WaitTask waitTask = new WaitTask(10);

        taskManager.AddTask(waitTask);

        helper.onEachTick(()->taskManager.Tick(1.f));
        helper.succeedOnTickWhen( 9, ()->
        {
            helper.assertTrue(waitTask.IsFinished(), "wait task is not finished");
        } );
    }

    @GameTest(batch = "TaskManager", template = "forge:empty3x3x3", timeoutTicks = 30)
    public static void TaskManagerTest_Sequential(@NotNull GameTestHelper helper)
    {
        {
            SequentialTask sequentialTask = new SequentialTask();
            sequentialTask.QueueTask(new WaitTask(10));
            sequentialTask.QueueTask(new WaitTask(15));

            int nextTick = sequentialTask.Tick(0);
            helper.assertValueEqual(nextTick, 10, "nextTick");

            nextTick = sequentialTask.Tick(3);
            helper.assertValueEqual(nextTick, 12, "nextTick");

            nextTick = sequentialTask.Tick(2);
            helper.assertValueEqual(nextTick, -2, "nextTick");
            helper.assertTrue(sequentialTask.IsFinished(), "Expect sequential task to be finished after the third tick");
        }
        {
            TaskManager taskManager = new TaskManager();

            SequentialTask sequentialTask = new SequentialTask();
            sequentialTask.QueueTask(new WaitTask(10));
            sequentialTask.QueueTask(new WaitTask(15));

            taskManager.AddTask(sequentialTask);

            helper.onEachTick(() -> taskManager.Tick(1.f));
            helper.succeedOnTickWhen(24, () ->
            {
                helper.assertTrue(sequentialTask.IsFinished(), "wait task is not finished");
            });
        }
    }

    @GameTest(batch = "TaskManager", template = "forge:empty3x3x3", timeoutTicks = 30)
    public static void TaskManagerTest_Concurrent(@NotNull GameTestHelper helper)
    {
        {
            ConcurrentTask concurrentTask = new ConcurrentTask();

            concurrentTask.AddTask(new WaitTask(10));
            concurrentTask.AddTask(new WaitTask(15));

            int nextTick = concurrentTask.Tick(0);
            helper.assertValueEqual(nextTick, 10, "nextTick");

            nextTick = concurrentTask.Tick(3);
            helper.assertValueEqual(nextTick, 2, "nextTick");

            nextTick = concurrentTask.Tick(1);
            helper.assertValueEqual(nextTick, -1, "nextTick");
            helper.assertTrue(concurrentTask.IsFinished(), "Expect concurrent task to be finished after the third tick");
        }
        {
            TaskManager taskManager = new TaskManager();

            ConcurrentTask concurrentTask = new ConcurrentTask();

            concurrentTask.AddTask(new WaitTask(10));
            concurrentTask.AddTask(new WaitTask(15));

            taskManager.AddTask(concurrentTask);

            helper.onEachTick(() -> taskManager.Tick(1.f));
            helper.succeedOnTickWhen(14, () ->
            {
                helper.assertTrue(concurrentTask.IsFinished(), "wait task is not finished");
            });
        }
    }

    @GameTest(batch = "TaskManager", template = "forge:empty3x3x3", timeoutTicks = 70)
    public static void TaskManagerTest_Complex(@NotNull GameTestHelper helper)
    {
        TaskManager taskManager = new TaskManager();

        ConcurrentTask rootTask = new ConcurrentTask();

        rootTask.AddTask(new WaitTask(10));
        rootTask.AddTask(new WaitTask(15));

        SequentialTask sequentialTask = new SequentialTask();
        sequentialTask.QueueTask(new WaitTask(10));
        sequentialTask.QueueTask(new WaitTask(25));
        ConcurrentTask subConcurrentTask = new ConcurrentTask();
        subConcurrentTask.AddTask(new WaitTask(15));
        subConcurrentTask.AddTask(new WaitTask(30));
        sequentialTask.QueueTask(subConcurrentTask);
        rootTask.AddTask(sequentialTask);

        taskManager.AddTask(rootTask);

        helper.onEachTick(()->taskManager.Tick(1.f));
        helper.succeedOnTickWhen( 64, ()->
        {
            helper.assertTrue(rootTask.IsFinished(), "wait task is not finished");
        } );
    }
}
