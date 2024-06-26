package settop.IgnesFatui.Utils;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class FakeInventory implements Container
{
    public static final int MAX_STACK = 1 << 30;

    private final NonNullList<ItemStack> stacks;
    public final boolean includeCounts;

    public FakeInventory(int size, boolean includeCounts)
    {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        this.includeCounts = includeCounts;
    }

    @Override
    public int getContainerSize()
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
    public @NotNull ItemStack getItem(int index)
    {
        if(index < stacks.size())
        {
            return stacks.get(index);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItem(int index, int count)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int index)
    {
        if(index < stacks.size())
        {
            stacks.set(index, ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int index, @NotNull ItemStack stack)
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
    public void setChanged()
    {

    }

    @Override
    public boolean stillValid(@NotNull Player player)
    {
        return true;
    }

    @Override
    public void clearContent()
    {
        Collections.fill(stacks, ItemStack.EMPTY);
    }

    public void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup)
    {
        ContainerHelper.loadAllItems(tag, stacks, lookup);
    }

    public void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup)
    {
        ContainerHelper.saveAllItems(tag, stacks, lookup);
    }
}
