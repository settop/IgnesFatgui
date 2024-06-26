package settop.IgnesFatui.Utils;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class ItemFilter
{
    public enum eFilterType
    {
        Item,
        Tag
    }

    private boolean whitelistEnabled = false;
    private final FakeInventory filter;
    private ArrayList<TagKey<Item>> tagFilter;
    private eFilterType filterType = eFilterType.Item;

    public ItemFilter(int filterSize)
    {
        filter = new FakeInventory(filterSize, false);
    }

    public boolean IsWhitelistEnabled() { return whitelistEnabled; }
    public void SetWhitelistEnabled( boolean enabled ) { whitelistEnabled = enabled; }

    public FakeInventory GetFilter() { return filter; }

        //ForgeRegistries.ITEMS.tags().getTagNames().anyMatch((tagKey) -> tagKey.location())
    public ArrayList<TagKey<Item>> GetTagFilters() { return tagFilter; }
    public void SetTagFilters(ArrayList<TagKey<Item>> tags) { tagFilter = tags; }

    public eFilterType GetFilterType() { return filterType; }
    public void SetFilterType(eFilterType filterType) { this.filterType = filterType; }

    public boolean Matches(ItemStack item)
    {
        boolean matchesFilters = switch(filterType)
        {
            case Item -> filter.hasAnyMatching((filterStack)->ItemStack.isSameItemSameComponents(item, filterStack) );
            case Tag -> item.getTags().anyMatch((tag)->tagFilter.contains(tag));
        };

        return matchesFilters == whitelistEnabled;
    }
}
