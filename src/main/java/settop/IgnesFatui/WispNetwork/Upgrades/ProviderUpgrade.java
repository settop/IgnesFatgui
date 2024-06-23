package settop.IgnesFatui.WispNetwork.Upgrades;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.Utils.ItemStackKey;
import settop.IgnesFatui.WispNetwork.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
                return fastestUpdateTime;
            }
            else
            {
                ++updatesWithoutChange;
                //note that sleep multiplier level takes longer to reach as the updates become slower
                int sleepMultiplier = 1 << (updatesWithoutChange / 4);
                return Integer.min(fastestUpdateTime * sleepMultiplier, maxUpdateTime);
            }
        }
    }

    private static class ItemStackSource extends WispNetworkItemSources.InventoryItemSource
    {
        public int updateCount = 0;
        public boolean isNew;

        public ItemStackSource(boolean isNew)
        {
            this.isNew = isNew;
        }
    }

    private Container linkedInventory;
    private final HashMap<ItemStackKey, ItemStackSource> itemSources = new HashMap<>();
    private UpdateTask updateTask;
    private int fastestUpdateTime = 5;
    private int maxUpdateTime = 20 * 5;

    public ProviderUpgrade(@NotNull WispNode parentNode, int fastestUpdateTime, int maxUpdateTime)
    {
        super(parentNode);
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
        if(GetParentNode().GetConnectedNetwork() != null)
        {
            AddItemsToNetwork();
        }
    }

    public void UnlinkFromInventory()
    {
        linkedInventory = null;
        if(GetParentNode().GetConnectedNetwork() != null)
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
            ItemStackSource itemStackSource = itemSources.computeIfAbsent(key, k -> new ItemStackSource(false));
            itemStackSource.UpdateCount(itemStackSource.GetCurrentCount() + slotStack.getCount());
        }
        itemSources.forEach(network::AddItemInventorySource);
    }

    private void RemoveItemsFromNetwork()
    {
        WispNetwork network = GetParentNode().GetConnectedNetwork();
        updateTask.SetFinished();

        itemSources.forEach(network::RemoveItemSource);
        itemSources.clear();
    }

    private boolean UpdateItemsInNetwork()
    {
        boolean anyUpdates = false;
        WispNetwork network = GetParentNode().GetConnectedNetwork();
        final int numSlots = linkedInventory.getContainerSize();
        for(int i = 0; i < numSlots; ++i)
        {
            ItemStack slotStack = linkedInventory.getItem(i);
            if (slotStack.isEmpty())
            {
                continue;
            }
            ItemStackKey key = new ItemStackKey(slotStack);
            ItemStackSource itemStackSource = itemSources.computeIfAbsent(key, k -> new ItemStackSource(true));
            itemStackSource.updateCount += slotStack.getCount();
        }

        for(Iterator<HashMap.Entry<ItemStackKey, ItemStackSource>> it = itemSources.entrySet().iterator(); it.hasNext();)
        {
            HashMap.Entry<ItemStackKey, ItemStackSource> entry = it.next();
            if(entry.getValue().updateCount == 0)
            {
                anyUpdates = true;
                network.RemoveItemSource(entry.getKey(), entry.getValue());
                it.remove();
            }
            else
            {
                anyUpdates |= entry.getValue().UpdateCount(entry.getValue().updateCount);
                entry.getValue().updateCount = 0;
                if(entry.getValue().isNew)
                {
                    network.AddItemInventorySource(entry.getKey(), entry.getValue());
                    entry.getValue().isNew = false;
                }
            }
        }
        return anyUpdates;
    }
}
