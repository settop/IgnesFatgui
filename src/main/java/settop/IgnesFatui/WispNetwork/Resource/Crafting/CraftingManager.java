package settop.IgnesFatui.WispNetwork.Resource.Crafting;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.WispNetwork.Resource.ResourceKey;
import settop.IgnesFatui.WispNetwork.Resource.ResourceManager;
import settop.IgnesFatui.WispNetwork.Resource.ResourcesManager;

import java.util.*;
import java.util.stream.Stream;

public class CraftingManager
{
    private record CraftResult(int minCrafts, float expectedCrafts, int maxCrafts, ArrayList<CraftingPattern.Entry> ingredients, ArrayList<CraftingPattern.Entry> remainders) {}
    private static class CraftStep
    {
        final CraftStep parent;
        final ResourceKey resource;
        int resourceCount = 0;
        int remainderScore = 0;

        CraftStep(CraftStep parent, @NotNull ResourceKey resource)
        {
            this.parent = parent;
            this.resource = resource;
        }

        void SetRemainderScore(int remainderScore)
        {
            this.remainderScore = remainderScore;
            for(CraftStep parent = this.parent; parent != null; parent = parent.parent)
            {
                parent.remainderScore += remainderScore;
            }
        }
    }
    private static class SimpleCraftResource
    {
        int inventoryCount = 0;
        int numCrafts = 0;
        int craftedCount = 0;
        int consumedCount = 0;

        int GetNumAvailable()
        {
            return inventoryCount + craftedCount - consumedCount;
        }

        SimpleCraftResource Clone()
        {
            SimpleCraftResource clone = new SimpleCraftResource();
            clone.inventoryCount = inventoryCount;
            clone.numCrafts = numCrafts;
            clone.craftedCount = craftedCount;
            clone.consumedCount = consumedCount;
            return clone;
        }
    }
    private class SimpleCraftBuilder
    {
        HashMap<ResourceKey, SimpleCraftResource> craftResources = new HashMap<>();

        private SimpleCraftResource GetCraftResource(ResourceKey resourceKey)
        {
            return craftResources.computeIfAbsent(resourceKey, (resource)->
            {
                ResourceManager<?> manager = GetResourceManager(resource.GetStackClass());
                SimpleCraftResource newResource = new SimpleCraftResource();
                if(manager != null)
                {
                    newResource.inventoryCount = manager.GenericCountMatchingStacks(resource);
                }
                return newResource;
            });
        }

        protected SimpleCraftBuilder Clone()
        {
            SimpleCraftBuilder simpleCraftBuilder = new SimpleCraftBuilder();
            for(var e : craftResources.entrySet())
            {
                simpleCraftBuilder.craftResources.put(e.getKey(), e.getValue().Clone());
            }
            return simpleCraftBuilder;
        }
    }
    enum SimplePatternCacheType
    {
        IS_SIMPLE,
        NO_PATTERN,
        IS_ADVANCED
    }

    private record SimpleCraftingPatternCached(CraftingPattern pattern, SimplePatternCacheType type) {}

    private final ResourcesManager resourcesManager;
    private final HashMap<ResourceKey, SimpleCraftingPatternCached> simplePatternCache = new HashMap<>();

    public CraftingManager(ResourcesManager resourcesManager)
    {
        this.resourcesManager = resourcesManager;
    }

    private ResourceManager<?> GetResourceManager(Class<?> stackClass)
    {
        if(stackClass == ItemStack.class)
        {
            return resourcesManager.GetItemResourceManager();
        }
        else
        {
            return resourcesManager.GetGenericResourceManager(stackClass);
        }
    }

    public void AddCratingPattern(CraftingPattern craftingPattern)
    {
        if(!craftingPattern.IsValid())
        {
            return;
        }
        resourcesManager.GetResourceManagers().forEach((resourceManager)->resourceManager.AddCraftingPattern(craftingPattern));
        simplePatternCache.clear();
    }

    public boolean TryBuildCraft(@NotNull Object stack, int count)
    {
        ResourceManager<?> resourceManager = GetResourceManager(stack.getClass());
        if(resourceManager == null)
        {
            return false;
        }
        return TryBuildCraft(resourceManager.GetStackKey(stack), count);
    }

    public boolean TryBuildCraft(@NotNull ResourceKey stack, int count)
    {
        if(TrySimpleCraft(new SimpleCraftBuilder(), stack, count))
        {
            return true;
        }

        /*
        Stream<CraftingPattern> craftingPatterns = resourceManager.GetGenericMatchingCraftingPatterns(stackKey);
        Optional<CraftingPattern> selectedPattern = craftingPatterns.findFirst();
        if(selectedPattern.isEmpty())
        {
            return;
        }

         */
        return false;
    }

    private SimpleCraftingPatternCached GetSimpleCraftingPattern(@NotNull ResourceKey resource)
    {
        SimpleCraftingPatternCached cachedSimplePattern = simplePatternCache.get(resource);
        if(cachedSimplePattern != null)
        {
            return cachedSimplePattern;
        }
        ResourceManager<?> resourceManager = GetResourceManager(resource.GetStackClass());
        if(resourceManager == null)
        {
            SimpleCraftingPatternCached cache = new SimpleCraftingPatternCached(null, SimplePatternCacheType.NO_PATTERN);
            simplePatternCache.put(resource, cache);
            return cache;
        }

        Stream<CraftingPattern> craftingPatterns = resourceManager.GetGenericMatchingCraftingPatterns(resource);
        CraftingPattern selectedPattern = null;
        for(Iterator<CraftingPattern> it = craftingPatterns.iterator(); it.hasNext();)
        {
            if(selectedPattern == null)
            {
                selectedPattern = it.next();
            }
            else
            {
                //more than one pattern for this resource
                SimpleCraftingPatternCached cache = new SimpleCraftingPatternCached(null, SimplePatternCacheType.IS_ADVANCED);
                simplePatternCache.put(resource, cache);
                return cache;
            }
        }
        if(selectedPattern == null)
        {
            SimpleCraftingPatternCached cache = new SimpleCraftingPatternCached(null, SimplePatternCacheType.NO_PATTERN);
            simplePatternCache.put(resource, cache);
            return cache;
        }
        else if(!selectedPattern.IsSimple())
        {
            SimpleCraftingPatternCached cache = new SimpleCraftingPatternCached(null, SimplePatternCacheType.IS_ADVANCED);
            simplePatternCache.put(resource, cache);
            return cache;
        }

        for(Iterator<CraftingPattern.Entry> it = selectedPattern.GetIngredients().iterator(); it.hasNext();)
        {
            SimpleCraftingPatternCached ingredientRecipe = GetSimpleCraftingPattern(it.next().stackKey());
            if(ingredientRecipe.type == SimplePatternCacheType.IS_ADVANCED)
            {
                //an advanced ingredient, so we are advanced as well
                SimpleCraftingPatternCached cache = new SimpleCraftingPatternCached(null, SimplePatternCacheType.IS_ADVANCED);
                simplePatternCache.put(resource, cache);
                return cache;
            }
        }

        //it's a simple craft
        SimpleCraftingPatternCached cache = new SimpleCraftingPatternCached(selectedPattern, SimplePatternCacheType.IS_SIMPLE);
        simplePatternCache.put(resource, cache);
        return cache;
    }

    //modifies simpleCraftBuilder even if it fails
    private boolean TrySimpleCraft(@NotNull SimpleCraftBuilder simpleCraftBuilder, @NotNull ResourceKey stack, int count)
    {
        SimpleCraftingPatternCached simpleCraftCache = GetSimpleCraftingPattern(stack);
        if(simpleCraftCache.type != SimplePatternCacheType.IS_SIMPLE)
        {
            return false;
        }
        //a simple recipe so all max==min

        CraftResult craftResult = GetIngredientsForCraftCount(simpleCraftCache.pattern, stack, count);
        if(craftResult == null)
        {
            return false;
        }
        for(CraftingPattern.Entry ingredient : craftResult.ingredients)
        {
            SimpleCraftResource craftResource = simpleCraftBuilder.GetCraftResource(ingredient.stackKey());
            int numAvailable = craftResource.GetNumAvailable();
            if(ingredient.max() <= numAvailable)
            {
                craftResource.consumedCount += ingredient.max();
                continue;
            }

            int countToCraft = ingredient.max() - numAvailable;
            if(!TrySimpleCraft(simpleCraftBuilder, ingredient.stackKey(), countToCraft))
            {
                return false;
            }
            craftResource.consumedCount += ingredient.max();
        }
        SimpleCraftResource resultCraftResource = simpleCraftBuilder.GetCraftResource(stack);
        resultCraftResource.craftedCount += count;
        if(!craftResult.remainders.isEmpty())
        {
            //since this is a simple craft the only possible remainder is excess from the craft
            assert craftResult.remainders.getFirst().stackKey().equals(stack);
            resultCraftResource.craftedCount += craftResult.remainders.getFirst().max();
        }
        resultCraftResource.numCrafts += craftResult.maxCrafts;

        return true;
    }

    private CraftResult GetIngredientsForCraftCount(CraftingPattern pattern, ResourceKey targetResult, int count)
    {
        Optional<CraftingPattern.Entry> selectedResultOpt = pattern.GetResults().dropWhile((r)->!r.stackKey().equals(targetResult)).findFirst();
        if(selectedResultOpt.isEmpty())
        {
            return null;
        }
        CraftingPattern.Entry selectedResult = selectedResultOpt.get();
        int minCrafts = (count + selectedResult.max() - 1) / selectedResult.max();
        float expectedCrafts = count / selectedResult.expected();
        int maxCrafts = selectedResult.min() > 0 ? (count + selectedResult.min() - 1) / selectedResult.min() : Integer.MAX_VALUE;

        ArrayList<CraftingPattern.Entry> ingredients = new ArrayList<>();
        ArrayList<CraftingPattern.Entry> remainders = new ArrayList<>();

        pattern.GetIngredients().forEach((ingredient)->
        {
            CraftingPattern.Entry ingredientEntry = new CraftingPattern.Entry
            (
                ingredient.stackKey(),
				ingredient.min() * minCrafts,
				ingredient.expected() * expectedCrafts,
                maxCrafts == Integer.MAX_VALUE ? Integer.MAX_VALUE : ingredient.max() * maxCrafts
             );
             ingredients.add(ingredientEntry);
        });
        pattern.GetResults().forEach((result)->
        {
            if(result.stackKey().equals(targetResult))
            {
				int maxResult = maxCrafts == Integer.MAX_VALUE ? Integer.MAX_VALUE : result.max() * maxCrafts;
				if(maxResult <= count)
				{
					return;
				}

                CraftingPattern.Entry remainderEntry = new CraftingPattern.Entry
                (
					result.stackKey(),
					result.min() * minCrafts - count,
					result.expected() * expectedCrafts - count,
                    maxCrafts == Integer.MAX_VALUE ? Integer.MAX_VALUE : maxResult - count
                );
                remainders.add(remainderEntry);
            }
            else
            {
                CraftingPattern.Entry remainderEntry = new CraftingPattern.Entry
                (
                    result.stackKey(),
					result.min() * minCrafts,
					result.expected() * expectedCrafts,
                    maxCrafts == Integer.MAX_VALUE ? Integer.MAX_VALUE : result.max() * maxCrafts
                );
                remainders.add(remainderEntry);
            }
        });

        pattern.GetByproducts().forEach((byproduct)->
        {
            CraftingPattern.Entry remainderEntry = new CraftingPattern.Entry
            (
				byproduct.stackKey(),
				byproduct.min() * minCrafts,
				byproduct.expected() * expectedCrafts,
                maxCrafts == Integer.MAX_VALUE ? Integer.MAX_VALUE : byproduct.max() * maxCrafts
            );
            remainders.add(remainderEntry);
        });
        return new CraftResult(minCrafts, expectedCrafts, maxCrafts, ingredients, remainders);
    }
}
