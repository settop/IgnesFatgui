package settop.IgnesFatui.WispNetwork.Resource;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class ItemResourceManager extends ResourceManager<ItemStack>
{
    private final HashMap<Item, ResourceSinkCollection<ItemStack>> specificItemSink = new HashMap<>();
    private final HashMap<TagKey<Item>, ResourceSinkCollection<ItemStack>> tagItemSink = new HashMap<>();

    private static class ItemSourceCollection
    {
        private final HashMap<DataComponentMap, ResourceSourceCollection<ItemStack>> dataItemCollections = new HashMap<>();
        private int itemTotalCount = 0;

        public int GetCount()
        {
            return itemTotalCount;
        }

        public void AddSource(DataComponentMap componentMap, ResourceSource<ItemStack> source)
        {
            dataItemCollections.computeIfAbsent(componentMap, (k)->new ResourceSourceCollection<ItemStack>()).AddSource(source);
            itemTotalCount += source.GetNumAvailable();
            source.AddListener(this::SourceChanged);
        }

        private void SourceChanged(int change)
        {
            itemTotalCount += change;
        }
    }

    private final HashMap<Item, ItemSourceCollection> specificItemSource = new HashMap<>();
    private final HashMap<TagKey<Item>, ResourceSourceCollection<ItemStack>> tagItemSource = new HashMap<>();

    @Override
    public void AddSink(@NotNull ResourceSink<ItemStack> sink)
    {
        if(sink.filter instanceof ItemFilter itemFilter)
        {
            if(itemFilter.IsWhitelistEnabled())
            {
                switch (itemFilter.GetFilterType())
                {
                    case Item:
                    {
                        if(itemFilter.IsIgnoringNBT())
                        {
                            for(var filterItem : itemFilter.GetItemMapFilter().entrySet())
                            {
                                ResourceSinkCollection<ItemStack> sinkCollection = specificItemSink.computeIfAbsent
                                (
                                    filterItem.getKey(),
                                    (k)->new ResourceSinkCollection<ItemStack>(false)
                                );
                                sinkCollection.add(sink);
                            }
                            return;
                        }
                    }
                    break;
                    case Tag:
                    {
                        for(TagKey<Item> tag : itemFilter.GetTagFilters())
                        {
                            ResourceSinkCollection<ItemStack> sinkCollection = tagItemSink.computeIfAbsent
                            (
                                tag,
                                (k)->new ResourceSinkCollection<ItemStack>(false)
                            );
                            sinkCollection.add(sink);
                        }
                    }
                    return;
                }
            }
        }

        super.AddSink(sink);
    }

    @Override
    public @Nullable ResourceSink<ItemStack> GetBestSinkForInsert(@NotNull ItemStack stack, int minPriority)
    {
        //prefer specific item over tag over default if there are sinks with the same priority
        //hence the +1 to the minPriority updates
        ResourceSink<ItemStack> bestSink = null;
        ResourceSinkCollection<ItemStack> specificSinkCollection = specificItemSink.get(stack.getItem());
        if(specificSinkCollection != null)
        {
            bestSink = specificSinkCollection.GetBestSinkForInsert(stack, minPriority);
            if(bestSink != null)
            {
                minPriority = Math.max(minPriority, bestSink.priority + 1);
            }
        }

        for(Iterator<TagKey<Item>> it = stack.getTags().iterator(); it.hasNext();)
        {
            ResourceSinkCollection<ItemStack> tagSinkCollection = tagItemSink.get(it.next());
            if(tagSinkCollection != null)
            {
               ResourceSink<ItemStack> bestTagSink = tagSinkCollection.GetBestSinkForInsert(stack, minPriority);
                if(bestTagSink != null)
                {
                    bestSink = bestTagSink;
                    minPriority = Math.max(minPriority, bestSink.priority + 1);
                }
            }
        }

        ResourceSink<ItemStack> bestDefaultSink = super.GetBestSinkForInsert(stack, minPriority);
        if(bestDefaultSink != null)
        {
            return bestDefaultSink;
        }
        else
        {
            return bestSink;
        }
    }

    @Override
    public void AddSource(@NotNull ItemStack stack, @NotNull ResourceSource<ItemStack> source)
    {
        ItemSourceCollection itemSourceCollection = specificItemSource.computeIfAbsent(stack.getItem(), (k)-> new ItemSourceCollection());
        itemSourceCollection.AddSource(stack.getComponents(), source);

        for(Iterator<TagKey<Item>> it = stack.getTags().iterator(); it.hasNext();)
        {
            TagKey<Item> tag = it.next();
            ResourceSourceCollection<ItemStack> tagSourceCollection = tagItemSource.computeIfAbsent(tag, (k)->new ResourceSourceCollection<>());
            tagSourceCollection.AddSource(source);
        }
    }

    @Override
    public @Nullable ResourceSource<ItemStack> FindBestSourceMatchingStack(@NotNull ItemStack stack, int minPriority)
    {
        ItemSourceCollection itemSourceCollection = specificItemSource.get(stack.getItem());
        if(itemSourceCollection == null)
        {
            return null;
        }
        ResourceSourceCollection<ItemStack> nbtSourceCollection = itemSourceCollection.dataItemCollections.get(stack.getComponents());
        if(nbtSourceCollection == null)
        {
            return null;
        }
        return nbtSourceCollection.GetBestSourceForExtract(minPriority);
    }

    public @Nullable ResourceSource<ItemStack> FindBestSourceMatchingItem(@NotNull Item item, int minPriority)
    {
        ItemSourceCollection itemSourceCollection = specificItemSource.get(item);
        if(itemSourceCollection == null)
        {
            return null;
        }
        ResourceSource<ItemStack> bestSource = null;
        for(var entry : itemSourceCollection.dataItemCollections.entrySet())
        {
            ResourceSource<ItemStack> source = entry.getValue().GetBestSourceForExtract(minPriority);
            if(source != null)
            {
                bestSource = source;
                minPriority = Math.max(minPriority, bestSource.priority);
            }
        }
        return bestSource;
    }

    public @Nullable ResourceSource<ItemStack> FindBestSourceMatchingItemAndComponents(@NotNull Item item, @NotNull DataComponentMap dataComponents, int minPriority)
    {
        ItemSourceCollection itemSourceCollection = specificItemSource.get(item);
        if(itemSourceCollection == null)
        {
            return null;
        }
        ResourceSourceCollection<ItemStack> componentSourceCollection = itemSourceCollection.dataItemCollections.get(dataComponents);
        if(componentSourceCollection == null)
        {
            return null;
        }
        return componentSourceCollection.GetBestSourceForExtract(minPriority);
    }

    public @Nullable ResourceSource<ItemStack> FindBestSourceMatchingItemAndComponents(@NotNull Item item, @NotNull Collection<DataComponentMap> dataComponents, int minPriority)
    {
        ItemSourceCollection itemSourceCollection = specificItemSource.get(item);
        if(itemSourceCollection == null)
        {
            return null;
        }
        ResourceSource<ItemStack> bestSource = null;
        for(DataComponentMap componentMap : dataComponents)
        {
            ResourceSourceCollection<ItemStack> componentSourceCollection = itemSourceCollection.dataItemCollections.get(componentMap);
            if(componentSourceCollection == null)
            {
                continue;
            }
            ResourceSource<ItemStack> source = componentSourceCollection.GetBestSourceForExtract(minPriority);
            if(source != null)
            {
                bestSource = source;
                minPriority = Math.max(minPriority, source.priority + 1);
            }
        }
        return bestSource;
    }

    public @Nullable ResourceSource<ItemStack> FindBestSourceMatchingTag(@NotNull TagKey<Item> tag, int minPriority)
    {
        ResourceSourceCollection<ItemStack> tagSourceCollection = tagItemSource.get(tag);
        if(tagSourceCollection == null)
        {
            return null;
        }
        return tagSourceCollection.GetBestSourceForExtract(minPriority);
    }

    @Override
    public @Nullable ResourceSource<ItemStack> FindBestSourceMatchingFilter(@NotNull ResourceFilter<ItemStack> filter, int minPriority)
    {
        if(filter instanceof ItemFilter itemFilter)
        {
            if(itemFilter.IsWhitelistEnabled())
            {
                switch(itemFilter.GetFilterType())
                {
                    case Item:
                    {
                        ResourceSource<ItemStack> bestSource = null;
                        for(var itemFilterEntry : itemFilter.GetItemMapFilter().entrySet())
                        {
                            ResourceSource<ItemStack> source = itemFilter.IsIgnoringNBT() ?
                                    FindBestSourceMatchingItem(itemFilterEntry.getKey(), minPriority) :
                                    FindBestSourceMatchingItemAndComponents(itemFilterEntry.getKey(), itemFilterEntry.getValue(), minPriority);
                            if(source != null)
                            {
                                bestSource = source;
                                minPriority = Math.max(minPriority, bestSource.priority + 1);
                            }
                        }
                        return bestSource;
                    }
                    case Tag:
                    {
                        ResourceSource<ItemStack> bestSource = null;
                        for(TagKey<Item> tag : itemFilter.GetTagFilters())
                        {
                            ResourceSource<ItemStack> source = FindBestSourceMatchingTag(tag, minPriority);
                            if(source != null)
                            {
                                bestSource = source;
                                minPriority = Math.max(minPriority, bestSource.priority + 1);
                            }
                        }
                        return bestSource;
                    }
                }
            }
        }

        //this is not ideal to reach here
        ResourceSource<ItemStack> bestSource = null;
        for(var specificItem : specificItemSource.entrySet())
        {
            for(var data : specificItem.getValue().dataItemCollections.entrySet())
            {
                ItemStack testItemStack = specificItem.getKey().getDefaultInstance().copy();
                testItemStack.applyComponents(data.getKey());
                if(!filter.Matches(testItemStack))
                {
                    continue;
                }

                ResourceSource<ItemStack> source = data.getValue().GetBestSourceForExtract(minPriority);
                if(source != null)
                {
                    bestSource = source;
                    minPriority = Math.max(minPriority, bestSource.priority + 1);
                }
            }
        }
        return bestSource;
    }

    public int CountMatchingStacks(@NotNull ItemStack stack)
    {
        ItemSourceCollection itemSourceCollection = specificItemSource.get(stack.getItem());
        if(itemSourceCollection == null)
        {
            return 0;
        }
        ResourceSourceCollection<ItemStack> sourceCollection = itemSourceCollection.dataItemCollections.get(stack.getComponents());
        if(sourceCollection == null)
        {
            return 0;
        }
        return sourceCollection.GetCount();
    }
}
