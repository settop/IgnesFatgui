package settop.IgnesFatui.Wisps;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.items.ItemStackHandler;

public class BasicWispContents implements IInventory
{
    private final ItemStackHandler wispContents;

    public BasicWispContents(int size)
    {
        wispContents = new ItemStackHandler(size);
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
        return wispContents.extractItem(index, count, false);
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        int maxPossibleItemStackSize = wispContents.getSlotLimit(index);
        return wispContents.extractItem(index, maxPossibleItemStackSize, false);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
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
        }
    }
}
