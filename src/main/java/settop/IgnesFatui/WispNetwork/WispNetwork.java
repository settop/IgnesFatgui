package settop.IgnesFatui.WispNetwork;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import settop.IgnesFatui.Wisps.IWisp;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class WispNetwork
{
    private class ItemSources
    {
        private int countCache = 0;
        private boolean craftable = false;
        private boolean isDirty = false;

        private class WispItemSource
        {
            public WeakReference<IWisp> sourceWisp;
            public int wispCountCache = 0;
            public boolean wistCraftCache = false;
        }

        private ArrayList<WispItemSource> itemSources;
    }

    private HashMap<ItemStack, ItemSources> itemSourceMap;



}
