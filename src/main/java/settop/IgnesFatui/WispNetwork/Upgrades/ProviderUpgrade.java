package settop.IgnesFatui.WispNetwork.Upgrades;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import settop.IgnesFatui.Utils.ItemStackKey;
import settop.IgnesFatui.WispNetwork.*;
import settop.IgnesFatui.WispNetwork.Resource.ItemResourceManager;
import settop.IgnesFatui.WispNetwork.Resource.ResourceSource;

import java.util.HashMap;
import java.util.Iterator;

public class ProviderUpgrade extends WispNodeUpgrade
{
    private class UpdateTask extends Task
    {
        int updatesWithoutChange = 0;

        @Override
        public int Tick(int extraTicks)
        {
            if(IsFinished())
            {
                return 0;
            }
            if(UpdateItemsInNetwork())
            {
                updatesWithoutChange = 0;
                return fastestUpdateTime - extraTicks;
            }
            else
            {
                ++updatesWithoutChange;
                //note that sleep multiplier level takes longer to reach as the updates become slower
                int sleepMultiplier = 1 << (updatesWithoutChange / 4);
                return Integer.min(fastestUpdateTime * sleepMultiplier, maxUpdateTime) - extraTicks;
            }
        }
    }

    private Container linkedInventory;
    private final HashMap<ItemStackKey, ResourceSource<ItemStack>> itemSources = new HashMap<>();
    private UpdateTask updateTask;
    private int fastestUpdateTime = 5;
    private int maxUpdateTime = 20 * 5;

    public ProviderUpgrade()
    {
        this(5, 20 * 5);
    }
    public ProviderUpgrade(int fastestUpdateTime, int maxUpdateTime)
    {
        this.fastestUpdateTime = fastestUpdateTime;
        this.maxUpdateTime = maxUpdateTime;
    }

    @Override
    public void OnParentNodeConnectToNetwork()
    {
        if(linkedInventory != null)
        {
            AddItemsToNetwork();
        }
    }

    @Override
    public void OnParentNodeDisconnectFromNetwork()
    {
        if(linkedInventory != null)
        {
            RemoveItemsFromNetwork();
        }
    }

    public void LinkToInventory(Container inventory)
    {
        linkedInventory = inventory;
        if(GetParentNode() != null && GetParentNode().GetConnectedNetwork() != null)
        {
            AddItemsToNetwork();
        }
    }

    public void UnlinkFromInventory()
    {
        linkedInventory = null;
        if(GetParentNode() != null && GetParentNode().GetConnectedNetwork() != null)
        {
            RemoveItemsFromNetwork();
        }
    }

    private void AddItemsToNetwork()
    {
        WispNetwork network = GetParentNode().GetConnectedNetwork();
        updateTask = new UpdateTask();
        network.AddTask(updateTask);

        final int numSlots = linkedInventory.getContainerSize();
        for(int i = 0; i < numSlots; ++i)
        {
            ItemStack slotStack = linkedInventory.getItem(i);
            if(slotStack.isEmpty())
            {
                continue;
            }
            ItemStackKey key = new ItemStackKey(slotStack);
            ResourceSource<ItemStack> itemStackSource = itemSources.computeIfAbsent(key, k -> new ResourceSource<ItemStack>(0, 0));
            itemStackSource.AccumulateNumAvailable(slotStack.getCount());
        }
        ItemResourceManager itemResourceManager = network.GetItemResourceManager();
        itemSources.forEach((k, v)->
        {
            v.Update();
            itemResourceManager.AddSource(k.stack(), v);
        });
    }

    private void RemoveItemsFromNetwork()
    {
        updateTask.SetSuccessful();

        itemSources.forEach((k, v)->v.SetInvalid());
        itemSources.clear();
    }

    private boolean UpdateItemsInNetwork()
    {
        ItemResourceManager itemResourceManager = GetParentNode().GetConnectedNetwork().GetItemResourceManager();
        final int numSlots = linkedInventory.getContainerSize();
        for(int i = 0; i < numSlots; ++i)
        {
            ItemStack slotStack = linkedInventory.getItem(i);
            if (slotStack.isEmpty())
            {
                continue;
            }
            ItemStackKey key = new ItemStackKey(slotStack);
            ResourceSource<ItemStack> itemStackSource = itemSources.computeIfAbsent(key, k ->
            {
                ResourceSource<ItemStack> source = new ResourceSource<ItemStack>(0, 0);
                itemResourceManager.AddSource(k.stack(), source);
                return source;
            });
            itemStackSource.AccumulateNumAvailable(slotStack.getCount());
        }

        boolean anyUpdates = false;
        for(Iterator<HashMap.Entry<ItemStackKey, ResourceSource<ItemStack>>> it = itemSources.entrySet().iterator(); it.hasNext();)
        {
            HashMap.Entry<ItemStackKey, ResourceSource<ItemStack>> entry = it.next();
            anyUpdates |= entry.getValue().Update();
            if(entry.getValue().GetNumAvailable() == 0)
            {
                anyUpdates = true;
                entry.getValue().SetInvalid();
                it.remove();
            }
        }
        return anyUpdates;
    }

    public boolean IsActive()
    {
        return updateTask != null && !updateTask.IsFinished();
    }
}
