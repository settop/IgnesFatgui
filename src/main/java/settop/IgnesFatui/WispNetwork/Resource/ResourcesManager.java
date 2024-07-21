package settop.IgnesFatui.WispNetwork.Resource;

import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.IgnesFatui;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ResourcesManager
{
    private final ItemResourceManager itemResourceManager;
    private final HashMap<Class<?>, ResourceManager<?>> resourceManagers;

    public ResourcesManager()
    {
        this.itemResourceManager = new ItemResourceManager();
        this.resourceManagers = new HashMap<>();

        this.resourceManagers.put(itemResourceManager.GetStackClass(), itemResourceManager);
    }

    public ItemResourceManager GetItemResourceManager()
    {
        return itemResourceManager;
    }

    public ResourceManager<?> GetGenericResourceManager(Class<?> stackClass)
    {
        return resourceManagers.get(stackClass);
    }

    public <T> ResourceManager<T> GetResourceManager(Class<T> stackClass)
    {
        ResourceManager<?> resourceManager = resourceManagers.get(stackClass);
        if(resourceManager == null)
        {
            return null;
        }
        return (ResourceManager<T>) resourceManager;
    }

    public Stream<ResourceManager<?>> GetResourceManagers()
    {
        return resourceManagers.values().stream();
    }

    public void RegisterResourceManager(@NotNull ResourceManager<?> resourceManager)
    {
        if(resourceManagers.containsKey(resourceManager.GetStackClass()))
        {
            IgnesFatui.LOGGER.error("Registering resource manager for stackKey class that already has a resource manager. Stack class: {}. Resource manager: {}",
                                    resourceManager.GetStackClass().toString(),
                                    resourceManager.toString()
                                    );
            return;
        }

        this.resourceManagers.put(resourceManager.GetStackClass(), resourceManager);
    }
}
