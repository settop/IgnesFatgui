package settop.IgnesFatui.WispNetwork.Resource;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class SimulatedInventory implements IItemHandler
{
    private final HashMap<Integer, ItemStack> modifiedSlots = new HashMap<>();
    private final IItemHandler wrappedItemHandler;

    public SimulatedInventory( @NotNull IItemHandler wrappedItemHandler)
    {
        this.wrappedItemHandler = wrappedItemHandler;
    }

    public void Reset()
    {
        modifiedSlots.clear();
    }

    @Override
    public int getSlots()
    {
        return wrappedItemHandler.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot)
    {
        ItemStack modifiedStack = modifiedSlots.get(slot);
        if(modifiedStack != null)
        {
            return modifiedStack;
        }
        else
        {
            return wrappedItemHandler.getStackInSlot(slot);
        }
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate)
    {
        if(stack.isEmpty())
        {
            return ItemStack.EMPTY;
        }
        if(modifiedSlots.containsKey(slot))
        {
            ItemStack modifiedStack = modifiedSlots.get(slot);

            int limit = Math.min(wrappedItemHandler.getSlotLimit(slot), stack.getMaxStackSize());
            if(!modifiedStack.isEmpty())
            {
                if (!ItemStack.isSameItemSameComponents(modifiedStack, stack))
                {
                    return stack;
                }
                limit -= modifiedStack.getCount();
            }
            if(limit <= 0)
            {
                return ItemStack.EMPTY;
            }

            boolean reachedLimit = stack.getCount() > limit;
            if (!simulate)
            {
                if(modifiedStack.isEmpty())
                {
                    modifiedSlots.put(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
                }
                else
                {
                    modifiedStack.grow(reachedLimit ? limit : stack.getCount());
                }
            }

            return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
        }
        else
        {
            //not been modified yet, check this is valid
            ItemStack remainder = wrappedItemHandler.insertItem(slot, stack, true);
            if(remainder.getCount() == stack.getCount())
            {
                //wasn't able to insert in the base, so don't in the simulated
                return remainder;
            }
            if(!simulate)
            {
                ItemStack modifiedStack = wrappedItemHandler.getStackInSlot(slot).copy();
                modifiedStack.grow(stack.getCount() - remainder.getCount());
                modifiedSlots.put(slot, modifiedStack);
            }
            return remainder;
        }
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if(modifiedSlots.containsKey(slot))
        {
            ItemStack modifiedStack = modifiedSlots.get(slot);
            int toExtract = Math.min(amount, modifiedStack.getMaxStackSize());
            if (modifiedStack.getCount() <= toExtract)
            {
                if (!simulate)
                {
                    modifiedSlots.put(slot, ItemStack.EMPTY);
                    return modifiedStack;
                }
                else
                {
                    return modifiedStack.copy();
                }
            }
            else
            {
                if (!simulate)
                {
                    modifiedSlots.put(slot, ItemHandlerHelper.copyStackWithSize(modifiedStack, modifiedStack.getCount() - toExtract));
                }

                return ItemHandlerHelper.copyStackWithSize(modifiedStack, toExtract);
            }
        }
        else
        {
            return wrappedItemHandler.extractItem(slot, amount, true);
        }
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return wrappedItemHandler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack)
    {
        return wrappedItemHandler.isItemValid(slot, stack);
    }
}
