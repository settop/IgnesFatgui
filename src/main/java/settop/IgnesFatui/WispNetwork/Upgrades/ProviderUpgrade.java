package settop.IgnesFatui.WispNetwork.Upgrades;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.WispNetwork.Resource.ItemStackKey;
import settop.IgnesFatui.WispNetwork.*;
import settop.IgnesFatui.WispNetwork.Resource.ItemResourceManager;
import settop.IgnesFatui.WispNetwork.Resource.ResourceSource;
import settop.IgnesFatui.WispNetwork.Tasks.PeriodicTask;

import java.util.HashMap;
import java.util.Iterator;

public class ProviderUpgrade extends WispNodeUpgrade
{
    private class UpdateTask extends PeriodicTask
    {
        protected UpdateTask()
        {
            super(fastestUpdateTime);
        }

        @Override
        public boolean TryDoWork()
        {
            return UpdateItemsInNetwork();
        }
    }

    private IItemHandler linkedInventory;
    private final HashMap<ItemStackKey, ResourceSource<ItemStack>> itemSources = new HashMap<>();
    private UpdateTask updateTask;
    private final int fastestUpdateTime;

    public ProviderUpgrade()
    {
        this(5);
    }
    public ProviderUpgrade(int fastestUpdateTime)
    {
        this.fastestUpdateTime = fastestUpdateTime;
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
        RemoveItemsFromNetwork();
    }

    @Override
    public void OnParentNodeLinkedToBlockEntity()
    {
        BlockEntity linkedBlockEntity = GetParentNode().GetLinkedBlockEntity();

        LazyOptional<IItemHandler> itemHandlerLazyOpt = linkedBlockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER);
        IItemHandler itemHandler = itemHandlerLazyOpt.resolve().orElse(null);
        if(itemHandler == null)
        {
            return;
        }

        LinkToInventory(itemHandler);
    }

    @Override
    public void OnParentNodeUnlinkedFromBlockEntity()
    {
        UnlinkFromInventory();
    }

    @Override
    public void OnAddToNode(@NotNull WispNode parentNode)
    {
        super.OnAddToNode(parentNode);
        if(parentNode.GetConnectedNetwork() != null && parentNode.GetLinkedBlockEntity() != null)
        {
            OnParentNodeLinkedToBlockEntity();
        }
    }

    @Override
    public void OnRemoveFromNode(@NotNull WispNode parentNode)
    {
        UnlinkFromInventory();
        super.OnRemoveFromNode(parentNode);
    }

    public void LinkToInventory(IItemHandler inventory)
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
        RemoveItemsFromNetwork();
    }

    private void AddItemsToNetwork()
    {
        WispNetwork network = GetParentNode().GetConnectedNetwork();
        updateTask = new UpdateTask();
        network.AddTask(updateTask);

        final int numSlots = linkedInventory.getSlots();
        for(int i = 0; i < numSlots; ++i)
        {
            ItemStack slotStack = linkedInventory.getStackInSlot(i);
            if(slotStack.isEmpty())
            {
                continue;
            }
            ItemStackKey key = new ItemStackKey(slotStack);
            ResourceSource<ItemStack> itemStackSource = itemSources.computeIfAbsent(key, k -> new ResourceSource<ItemStack>(0, 0));
            itemStackSource.AccumulateNumAvailable(slotStack.getCount());
        }
        ItemResourceManager itemResourceManager = network.GetResourcesManager().GetItemResourceManager();
        itemSources.forEach((k, v)->
        {
            v.Update();
            itemResourceManager.AddSource(k.GetItemStack(), v);
        });
    }

    private void RemoveItemsFromNetwork()
    {
        if(updateTask != null)
        {
            updateTask.SetSuccessful();
        }

        itemSources.forEach((k, v)->v.SetInvalid());
        itemSources.clear();
    }

    private boolean UpdateItemsInNetwork()
    {
        ItemResourceManager itemResourceManager = GetParentNode().GetConnectedNetwork().GetResourcesManager().GetItemResourceManager();
        final int numSlots = linkedInventory.getSlots();
        for(int i = 0; i < numSlots; ++i)
        {
            ItemStack slotStack = linkedInventory.getStackInSlot(i);
            if (slotStack.isEmpty())
            {
                continue;
            }
            ItemStackKey key = new ItemStackKey(slotStack);
            ResourceSource<ItemStack> itemStackSource = itemSources.computeIfAbsent(key, k ->
            {
                ResourceSource<ItemStack> source = new ResourceSource<ItemStack>(0, 0);
                itemResourceManager.AddSource(k.GetItemStack(), source);
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
