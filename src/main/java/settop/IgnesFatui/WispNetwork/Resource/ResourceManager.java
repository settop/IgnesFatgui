package settop.IgnesFatui.WispNetwork.Resource;

import net.minecraft.util.SortedArraySet;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.WispNetwork.Resource.Crafting.CraftingPattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

public abstract class ResourceManager<T>
{

    protected static class ResourceSinkCollection<T>
    {
        //sorted highest priority first
        private final ArrayList<ResourceSink<T>> resourceSinks = new ArrayList<>();
        private final boolean checkSinkFilter;

        protected ResourceSinkCollection(boolean checkSinkFilter)
        {
            this.checkSinkFilter = checkSinkFilter;
        }

        void add(@NotNull ResourceSink<T> sink)
        {
            int insertPoint = Collections.binarySearch(resourceSinks, sink, (l, r)->r.priority - l.priority);
            if(insertPoint < 0)
            {
                insertPoint = -(insertPoint + 1);
            }
            resourceSinks.add(insertPoint, sink);
        }

        public @Nullable ResourceSink<T> GetBestSinkForInsert(@NotNull T stack, int minPriority)
        {
            for(Iterator<ResourceSink<T>> it = resourceSinks.iterator(); it.hasNext();)
            {
                ResourceSink<T> sink = it.next();
                if(!sink.IsValid())
                {
                    it.remove();
                    continue;
                }
                if(sink.priority < minPriority)
                {
                    //since this is sorted all other sinks will be equal or lower priority than this one
                    break;
                }
                if(checkSinkFilter && sink.filter != null && !sink.filter.Matches(stack))
                {
                    continue;
                }
                if(sink.CanInsert(stack))
                {
                    return sink;
                }
            }
            return null;
        }
    }

    protected static class ResourceSourceCollection<T>
    {
        private final ArrayList<ResourceSource<T>> resourceSources = new ArrayList<>();
        private final ArrayList<CraftingPattern> craftingPatterns = new ArrayList<>();
        private int totalCount = 0;

        public int GetCount()
        {
            return totalCount;
        }

        public void AddSource(ResourceSource<T> source)
        {
            int insertPoint = Collections.binarySearch(resourceSources, source, (l, r)->r.priority - l.priority);
            if(insertPoint < 0)
            {
                insertPoint = -(insertPoint + 1);
            }
            resourceSources.add(insertPoint, source);
            totalCount += source.GetNumAvailable();
            source.AddListener(this::SourceChanged);
        }

        private void SourceChanged(int change)
        {
            totalCount += change;
        }

        public ResourceSource<T> GetBestSourceForExtract(int minPriority)
        {
            for(Iterator<ResourceSource<T>> it = resourceSources.iterator(); it.hasNext();)
            {
                ResourceSource<T> source = it.next();
                if(!source.IsValid())
                {
                    it.remove();
                    continue;
                }
                if(source.priority < minPriority)
                {
                    //since this is sorted all other sinks will be equal or lower priority than this one
                    break;
                }
                if(source.GetNumAvailable() <= 0)
                {
                    continue;
                }
                return source;
            }
            return null;
        }

        public void AddCraftingPattern(CraftingPattern cratingPattern)
        {
            craftingPatterns.add(cratingPattern);
        }

        public Stream<CraftingPattern> GetCratingPatterns()
        {
            return craftingPatterns.stream();
        }
    }

    private final Class<T> stackClass;
    protected final ResourceSinkCollection<T> defaultSinks = new ResourceSinkCollection<>(true);

    protected ResourceManager(Class<T> stackClass)
    {
        this.stackClass = stackClass;
    }

    public Class<?> GetStackClass()
    {
        return stackClass;
    }

    public final boolean IsValidStackForResource(Object obj)
    {
        return obj.getClass() == stackClass;
    }

    public abstract ResourceKey GetStackKey(@NotNull Object stack);

    abstract public int GetCountFromStack(Object obj);

    public void AddSink(@NotNull ResourceSink<T> sink)
    {
        defaultSinks.add(sink);
    }

    public @Nullable ResourceSink<T> GetBestSinkForInsert(@NotNull T stack, int minPriority)
    {
        return defaultSinks.GetBestSinkForInsert(stack, minPriority);
    }

    public final @Nullable ResourceSink<T> GetBestSinkForInsert(@NotNull T stack)
    {
        return GetBestSinkForInsert(stack, Integer.MIN_VALUE);
    }

    public abstract void AddSource(@NotNull T stack, @NotNull ResourceSource<T> source);
    public abstract @Nullable ResourceSource<T> FindBestSourceMatchingStack(@NotNull T stack, int minPriority);
    public abstract @Nullable ResourceSource<T> FindBestSourceMatchingFilter(@NotNull ResourceFilter<T> filter, int minPriority);
    public abstract int CountMatchingStacks(@NotNull T stack);

    public final @Nullable ResourceSource<T> FindBestSourceMatchingStack(@NotNull T stack)
    {
        return FindBestSourceMatchingStack(stack, Integer.MIN_VALUE);
    }
    public final @Nullable ResourceSource<T> FindBestSourceMatchingFilter(@NotNull ResourceFilter<T> filter)
    {
        return FindBestSourceMatchingFilter(filter, Integer.MIN_VALUE);
    }

    public abstract void AddCraftingPattern(CraftingPattern craftingPattern);
    public abstract @Nonnull Stream<CraftingPattern> GetMatchingCraftingPatterns(@NotNull T stack);
    public abstract @Nonnull Stream<CraftingPattern> GetMatchingCraftingPatterns(@NotNull ResourceFilter<T> filter);

    public final @Nullable ResourceSource<?> FindGenericBestSourceMatchingStack(@NotNull Object stack, int minPriority)
    {
        if(stack.getClass() == stackClass)
        {
            return FindBestSourceMatchingStack((T)stack, minPriority);
        }
        else if(stack instanceof ResourceKey resourceKey)
        {
            if(resourceKey.GetStackClass() == stackClass)
            {
                return FindBestSourceMatchingStack((T)resourceKey.GetStack(), minPriority);
            }
        }
        return null;
    }
    public final @Nullable ResourceSource<?> FindGenericBestSourceMatchingStack(@NotNull Object stack)
    {
        return FindGenericBestSourceMatchingStack(stack, Integer.MIN_VALUE);
    }

    public final @Nullable ResourceSource<?> FindGenericBestSourceMatchingFilter(@NotNull ResourceFilter<?> filter, int minPriority)
    {
        if(filter.GetStackClass() == stackClass)
        {
            return FindBestSourceMatchingFilter((ResourceFilter<T>)filter, minPriority);
        }
        return null;
    }
    public final @Nullable ResourceSource<?> FindGenericBestSourceMatchingFilter(@NotNull ResourceFilter<?> filter)
    {
        return FindGenericBestSourceMatchingFilter(filter, Integer.MIN_VALUE);
    }

    public final int GenericCountMatchingStacks(@NotNull Object stack)
    {
        if(stack.getClass() == stackClass)
        {
            return CountMatchingStacks((T)stack);
        }
        else if(stack instanceof ResourceKey resourceKey)
        {
            if(resourceKey.GetStackClass() == stackClass)
            {
                return CountMatchingStacks((T)resourceKey.GetStack());
            }
        }
        return 0;
    }

    public final @Nonnull Stream<CraftingPattern> GetGenericMatchingCraftingPatterns(@NotNull Object stack)
    {
        if(stack.getClass() == stackClass)
        {
            return GetMatchingCraftingPatterns((T)stack);
        }
        else if(stack instanceof ResourceKey resourceKey)
        {
            if(resourceKey.GetStackClass() == stackClass)
            {
                return GetMatchingCraftingPatterns((T)resourceKey.GetStack());
            }
        }
        return Stream.empty();
    }

    public final @Nonnull Stream<CraftingPattern> GetGenericMatchingCraftingPatterns(@NotNull ResourceFilter<?> filter)
    {
        if(filter.GetStackClass() == stackClass)
        {
            return GetMatchingCraftingPatterns((ResourceFilter<T>)filter);
        }
        return Stream.empty();
    }
}
