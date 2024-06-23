package settop.IgnesFatui.WispNetwork;

import java.util.ArrayList;

public class WispNetworkItemSources
{
    public static class InventoryItemSource
    {
        private WispNetworkItemSources parent;
        private int inventoryItemCountCache = 0;

        public int GetCurrentCount() {
            return inventoryItemCountCache;
        }

        public boolean UpdateCount(int count)
        {
            if (inventoryItemCountCache != count)
            {
                int countChange = count - inventoryItemCountCache;
                inventoryItemCountCache = count;
                if(parent != null)
                {
                    parent.UpdateCount(countChange);
                }
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    public class CraftingItemSource
    {

    }

    private int totalCount = 0;

    private final ArrayList<InventoryItemSource> inventorySources = new ArrayList<>();
    private final ArrayList<CraftingItemSource> craftingSources = new ArrayList<>();

    private void UpdateCount(int countChange)
    {
        totalCount += countChange;
    }

    public void AddItemSource(InventoryItemSource itemSource)
    {
        if(itemSource.parent != null)
        {
            throw new RuntimeException("Adding an item source that is already part of an item source");
        }
        inventorySources.add(itemSource);
        itemSource.parent = this;
        totalCount += itemSource.GetCurrentCount();
    }

    public void RemoveItemSource(InventoryItemSource source)
    {
        if (source.parent != null && inventorySources.remove(source))
        {
            totalCount -= source.GetCurrentCount();
            source.parent = null;
        }
    }
}
