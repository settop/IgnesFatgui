package settop.IgnesFatui.WispNetwork.Resource;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import settop.IgnesFatui.Utils.FakeInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class ItemFilter extends ResourceFilter<ItemStack>
{
    public enum eFilterType
    {
        Item,
        Tag
    }

    private boolean whitelistEnabled = false;
    private boolean ignoreNBT = true;
    private final FakeInventory filter;
    private final HashMap<Item, HashSet<DataComponentMap>> itemsMap = new HashMap<>();
    private ArrayList<TagKey<Item>> tagFilter;
    private eFilterType filterType = eFilterType.Item;

    public ItemFilter(int filterSize)
    {
        super(ItemStack.class);
        filter = new FakeInventory(filterSize, false)
        {
            @Override
            public void setChanged()
            {
                UpdateItemFilter();
            }
        };
    }

    private void UpdateItemFilter()
    {
        itemsMap.clear();
        for (int i = 0; i < filter.getContainerSize(); ++i)
        {
            ItemStack stack = filter.getItem(i);
            if(stack.isEmpty())
            {
                continue;
            }
            itemsMap.compute(stack.getItem(), (k, v)->
            {
                if(v == null)
                {
                    v = new HashSet<>();
                    if(!ignoreNBT)
                    {
                        v.add(stack.getComponents());
                    }
                    return v;
                }
                else if(ignoreNBT)
                {
                    return v;
                }
                else
                {
                    v.add(stack.getComponents());
                    return v;
                }
            });
        }
    }

    public boolean IsWhitelistEnabled() { return whitelistEnabled; }
    public void SetWhitelistEnabled( boolean enabled ) { whitelistEnabled = enabled; }

    public boolean IsIgnoringNBT() { return ignoreNBT; }
    public void SetIgnoreNBT( boolean ignoreNBT ) { this.ignoreNBT = ignoreNBT; }

    public FakeInventory GetFilter() { return filter; }
    public HashMap<Item, HashSet<DataComponentMap>> GetItemMapFilter() { return itemsMap; }

    //ForgeRegistries.ITEMS.tags().getTagNames().anyMatch((tagKey) -> tagKey.location())
    public ArrayList<TagKey<Item>> GetTagFilters() { return tagFilter; }
    public void SetTagFilters(ArrayList<TagKey<Item>> tags) { tagFilter = tags; }

    public eFilterType GetFilterType() { return filterType; }
    public void SetFilterType(eFilterType filterType) { this.filterType = filterType; }

    @Override
    public boolean Matches(ItemStack item)
    {
        boolean matchesFilters = switch(filterType)
        {
            case Item ->
            {
                HashSet<DataComponentMap> components = itemsMap.get(item.getItem());
                if(components == null)
                {
                     yield false;
                }
                if(ignoreNBT)
                {
                    yield true;
                }
                yield components.contains(item.getComponents());
            }
            case Tag -> item.getTags().anyMatch((tag)->tagFilter.contains(tag));
        };

        return matchesFilters == whitelistEnabled;
    }
}
