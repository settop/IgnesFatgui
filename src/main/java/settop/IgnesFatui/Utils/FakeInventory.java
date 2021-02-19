package settop.IgnesFatui.Utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;

public class FakeInventory implements IInventory
{
    public static final int MAX_STACK = 1 << 30;

    private NonNullList<ItemStack> stacks;
    public final boolean includeCounts;

    public FakeInventory(int size, boolean includeCounts)
    {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        this.includeCounts = includeCounts;
    }

    @Override
    public int getSizeInventory()
    {
        return stacks.size();
    }

    @Override
    public boolean isEmpty()
    {
        for(ItemStack item : stacks)
        {
            if(!item.isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        if(index < stacks.size())
        {
            return stacks.get(index);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        if(index < stacks.size())
        {
            stacks.set(index, ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if(index < stacks.size())
        {
            stacks.set(index, stack.copy());
            if (!includeCounts)
            {
                stacks.get(index).setCount(1);
            }
        }
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
        for(int i = 0; i < stacks.size(); ++i)
        {
            stacks.set(i, ItemStack.EMPTY);
        }
    }

    public CompoundNBT Save(CompoundNBT tag)
    {
        return ItemStackHelper.saveAllItems(tag, stacks);
    }

    public void Load(CompoundNBT tag)
    {
        ItemStackHelper.loadAllItems(tag, stacks);
    }
}
