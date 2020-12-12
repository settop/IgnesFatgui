package settop.IgnesFatui.Wisps;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Items.WispEnhancementItem;
import settop.IgnesFatui.Wisps.Enhancements.IEnhancement;

import java.util.ArrayList;

public class BasicWispContents implements IInventory
{
    public interface OnEnhancementChanged
    {
        void OnEnhancementChange(int index, IEnhancement previousEnhancement, IEnhancement nextEnhancement);
    }

    private final ItemStackHandler wispContents;
    private OnEnhancementChanged listener;

    public BasicWispContents(int size)
    {
        wispContents = new ItemStackHandler(size);
    }

    public void SetListener(OnEnhancementChanged changeListener)
    {
        listener = changeListener;
        if(listener != null)
        {
            for(int i = 0; i < wispContents.getSlots(); ++i)
            {
                ItemStack stack = wispContents.getStackInSlot(i);
                if(stack != null)
                {
                    LazyOptional<IEnhancement> enhancement = stack.getCapability(IgnesFatui.Capabilities.CAPABILITY_ENHANCEMENT);
                    if(enhancement.isPresent())
                    {
                        listener.OnEnhancementChange(i, null, enhancement.resolve().get());
                    }
                }
            }
        }
    }

    @Override
    public int getSizeInventory()
    {
        return wispContents.getSlots();
    }

    @Override
    public boolean isEmpty()
    {
        for (int i = 0; i < wispContents.getSlots(); ++i)
        {
            if (!wispContents.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return wispContents.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        ItemStack retItem = wispContents.extractItem(index, count, false);
        UpdateEnhancement(index, retItem, wispContents.getStackInSlot(index));
        return retItem;
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        UpdateEnhancement(index, wispContents.getStackInSlot(index), null);
        int maxPossibleItemStackSize = wispContents.getSlotLimit(index);
        return wispContents.extractItem(index, maxPossibleItemStackSize, false);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        UpdateEnhancement(index, wispContents.getStackInSlot(index), stack);
        wispContents.setStackInSlot(index, stack);
    }

    @Override
    public void markDirty()
    {

    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player)
    {
        return true;
    }

    @Override
    public void clear()
    {
        for (int i = 0; i < wispContents.getSlots(); ++i)
        {
            wispContents.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public ListNBT write()
    {
        ListNBT nbtTagList = new ListNBT();
        for(int i = 0; i < wispContents.getSlots(); ++i)
        {
            if (!wispContents.getStackInSlot(i).isEmpty())
            {
                CompoundNBT compoundnbt = new CompoundNBT();
                compoundnbt.putByte("Slot", (byte)i);
                wispContents.getStackInSlot(i).write(compoundnbt);
                nbtTagList.add(compoundnbt);
            }
        }
        return nbtTagList;
    }

    public void read(CompoundNBT nbt, String invName)
    {
        if(!nbt.contains(invName))
        {
            return;
        }
        ListNBT listNBT = nbt.getList(invName, nbt.getId());
        for(int i = 0; i < listNBT.size(); ++i)
        {
            CompoundNBT compoundnbt = listNBT.getCompound(i);
            int j = compoundnbt.getByte("Slot");
            ItemStack itemstack = ItemStack.read(compoundnbt);
            if (!itemstack.isEmpty())
            {
                wispContents.setStackInSlot(j, itemstack);
            }
            UpdateEnhancement(j, null, itemstack);
        }
    }

    private void UpdateEnhancement(int index, ItemStack previousStack, ItemStack newStack)
    {
        IEnhancement previousEnhancement = null;
        IEnhancement newEnhancement = null;
        if(previousStack != null  && previousStack.getItem() instanceof WispEnhancementItem)
        {
            previousEnhancement = previousStack.getCapability(IgnesFatui.Capabilities.CAPABILITY_ENHANCEMENT).resolve().get();
        }
        if(newStack != null && newStack.getItem() instanceof WispEnhancementItem)
        {
            newEnhancement = newStack.getCapability(IgnesFatui.Capabilities.CAPABILITY_ENHANCEMENT).resolve().get();
        }

        if(listener != null)
        {
            listener.OnEnhancementChange(index, previousEnhancement, newEnhancement);
        }
    }

    public boolean HasEnhancement(int slotIndex)
    {
        return slotIndex < wispContents.getSlots() && wispContents.getStackInSlot(slotIndex).getItem() instanceof WispEnhancementItem;
    }

    public IEnhancement GetEnhancement(int slotIndex)
    {
        if(!HasEnhancement(slotIndex))
        {
            return null;
        }

        ItemStack itemStack = wispContents.getStackInSlot(slotIndex);
        return itemStack.getCapability(IgnesFatui.Capabilities.CAPABILITY_ENHANCEMENT).resolve().get();
    }
}
