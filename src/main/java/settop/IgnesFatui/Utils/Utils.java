package settop.IgnesFatui.Utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Utils
{
    public static void SpawnAsEntity(Level worldIn, BlockPos pos, ItemStack stack)
    {
        if (!worldIn.isClientSide && !stack.isEmpty())
        {
            float f = 0.5F;
            double d0 = (double)(worldIn.random.nextFloat() * 0.5F) + 0.25D;
            double d1 = (double)(worldIn.random.nextFloat() * 0.5F) + 0.25D;
            double d2 = (double)(worldIn.random.nextFloat() * 0.5F) + 0.25D;
            ItemEntity itementity = new ItemEntity(worldIn, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, stack);
            itementity.setDefaultPickUpDelay();
            worldIn.addFreshEntity(itementity);
        }
    }

    public static <T> void IteratePermutations(ArrayList<T> iterable, Consumer<ArrayList<T>> callback)
    {
        //make use of Heap's algorithm
        IteratePermutations(iterable, iterable.size(), callback);
    }

    public static <T> void IteratePermutations(ArrayList<T> iterable, int k, Consumer<ArrayList<T>> callback)
    {
        if(k <= 1)
        {
            callback.accept(iterable);
            return;
        }

        IteratePermutations(iterable, k - 1, callback);
        for(int i = 0; i < k - 1; ++i)
        {
            int swapPos1;
            int swapPos2;
            if(k % 2 == 0)
            {
                swapPos1 = i;
                swapPos2 = k-1;
            }
            else
            {
                swapPos1 = 0;
                swapPos2 = k-1;
            }
            T temp = iterable.get(swapPos1);
            iterable.set(swapPos1, iterable.get(swapPos2));
            iterable.set(swapPos2, temp);

            IteratePermutations(iterable, k - 1, callback);
        }
    }
}
