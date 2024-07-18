package settop.IgnesFatui.WispNetwork.Resource;

import net.minecraft.util.SortedArraySet;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

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
    }

    protected final ResourceSinkCollection<T> defaultSinks = new ResourceSinkCollection<>(true);

    public void AddSink(@NotNull ResourceSink<T> sink)
    {
        defaultSinks.add(sink);
    }

    public @Nullable ResourceSink<T> GetBestSinkForInsert(@NotNull T stack, int minPriority)
    {
        return defaultSinks.GetBestSinkForInsert(stack, minPriority);
    }

    public abstract void AddSource(@NotNull T stack, @NotNull ResourceSource<T> source);
    public abstract @Nullable ResourceSource<T> FindBestSourceMatchingStack(@NotNull T stack, int minPriority);
    public abstract @Nullable ResourceSource<T> FindBestSourceMatchingFilter(@NotNull ResourceFilter<T> filter, int minPriority);
    public abstract int CountMatchingStacks(@NotNull T stack);
}
