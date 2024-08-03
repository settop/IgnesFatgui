package settop.IgnesFatui.WispNetwork.Resource.Crafting;

import com.google.common.collect.HashMultimap;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.WispNetwork.Resource.ResourceKey;
import settop.IgnesFatui.WispNetwork.Resource.ResourceManager;
import settop.IgnesFatui.WispNetwork.Resource.ResourcesManager;

import java.util.*;
import java.util.stream.Stream;

public class CraftingManager
{
    private record CraftResult(int minCrafts, float expectedCrafts, int maxCrafts, ArrayList<CraftingPattern.Entry> ingredients, CraftingPattern.Entry resultLeftover, ArrayList<CraftingPattern.Entry> remainders) {}
    private record CraftingPatternCached(CraftingPattern pattern, PatternCacheType type, ResourceKey targetResourceKey, ArrayList<ResourceKey> allRemainders, ArrayList<ResourceKey> loopedRemainders) {}
    public record CraftStep(CraftingPattern pattern, int count){};
    private static class AdvancedCraftStep
    {
        public final CraftingPatternCached patternCached;
        public final ResourceKey targetResource;
        public final int craftCount;
        public final ArrayList<AdvancedCraftStep> childCraftSteps = new ArrayList<>();

        public AdvancedCraftStep(CraftingPatternCached patternCached, ResourceKey targetResource, int craftCount)
        {
            this.patternCached = patternCached;
            this.targetResource = targetResource;
            this.craftCount = craftCount;
        }

        public AdvancedCraftStep()
        {
            this.patternCached = null;
            this.targetResource = null;
            this.craftCount = 0;
        }
    }
    private static class CraftScore
    {
        public int craftCount = 0;
        public float byproductScore = 0;
        public static Comparator<CraftScore> SCORE_COMPARATOR = (l, r) ->
        {
            int c = Integer.compare(l.craftCount, r.craftCount);
            if(c != 0)
            {
                return c;
            }
            return Float.compare(l.byproductScore, r.byproductScore);
        };
    }

    private static class CraftResource
    {
        int inventoryCount = 0;
        int numCrafts = 0;
        int craftedCount = 0;
        int remainingCount = 0;
        int consumedCount = 0;

        int GetNumAvailable()
        {
            return inventoryCount + craftedCount + remainingCount - consumedCount;
        }

        CraftResource Clone()
        {
            CraftResource clone = new CraftResource();
            clone.inventoryCount = inventoryCount;
            clone.numCrafts = numCrafts;
            clone.craftedCount = craftedCount;
            clone.remainingCount = remainingCount;
            clone.consumedCount = consumedCount;
            return clone;
        }
    }
    private class CraftBuilder
    {
        HashMap<ResourceKey, CraftResource> craftResources = new HashMap<>();

        private CraftResource GetCraftResource(ResourceKey resourceKey)
        {
            return craftResources.computeIfAbsent(resourceKey, (resource)->
            {
                ResourceManager<?> manager = GetResourceManager(resource.GetStackClass());
                CraftResource newResource = new CraftResource();
                if(manager != null)
                {
                    newResource.inventoryCount = manager.GenericCountMatchingStacks(resource);
                }
                return newResource;
            });
        }

        protected CraftBuilder Clone()
        {
            CraftBuilder craftBuilder = new CraftBuilder();
            for(var e : craftResources.entrySet())
            {
                craftBuilder.craftResources.put(e.getKey(), e.getValue().Clone());
            }
            return craftBuilder;
        }

        private void CopyFrom(CraftBuilder other)
        {
            craftResources.clear();
            for(var e : other.craftResources.entrySet())
            {
                craftResources.put(e.getKey(), e.getValue().Clone());
            }
        }
    }
    enum PatternCacheType
    {
        NO_PATTERN(false, false),
        IS_SIMPLE(false, false),//a simple craft, ie can be computed in a single batch
        IS_SIMPLE_WTH_REMAINDER(true, false),//the craft itself is simple, but it has remainders
        IS_ADVANCED(true, true);//a more complex craft, that might need to be ordered to loop it's remainders back into itself

        private final boolean hasRemainder;
        private final boolean isAdvanced;

        PatternCacheType(boolean hasRemainder, boolean isAdvanced)
        {
            this.hasRemainder = hasRemainder;
            this.isAdvanced = isAdvanced;
        }

        public boolean HasRemainder() { return hasRemainder; }
        public boolean IsAdvanced() { return isAdvanced; }
    }

    private final ResourcesManager resourcesManager;
    private final HashMap<ResourceKey, CraftingPatternCached> patternCache = new HashMap<>();
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
        patternCache.clear();
    }

    public Optional<CraftExecutor> TryBuildCraft(@NotNull Object stack, int count)
    {
        ResourceManager<?> resourceManager = GetResourceManager(stack.getClass());
        if(resourceManager == null)
        {
            return Optional.empty();
        }
        return TryBuildCraft(resourceManager.GetStackKey(stack), count);
    }

    public Optional<CraftExecutor> TryBuildCraft(@NotNull ResourceKey stack, int count)
    {
        CraftingPatternCached simpleCraftCache = GetCraftingPattern(stack);
        if(simpleCraftCache.type() == PatternCacheType.NO_PATTERN)
        {
            return Optional.empty();
        }
        else if(!simpleCraftCache.type().IsAdvanced())
        {
            CraftBuilder craftBuilder = new CraftBuilder();
            if (TrySimpleCraft(craftBuilder, stack, count))
            {
                return Optional.of(ReconstructSimpleCraftSteps(craftBuilder));
            }
        }
        else
        {
            CraftBuilder craftBuilder = new CraftBuilder();
            BuildAdvanceCraft(craftBuilder, stack, count);
        }

        return Optional.empty();
    }

    private CraftingPatternCached GetCraftingPattern(@NotNull ResourceKey resource)
    {
        //ToDo: Handle random results/ingredients
        CraftingPatternCached cachedSimplePattern = patternCache.get(resource);
        if(cachedSimplePattern != null)
        {
            return cachedSimplePattern;
        }
        ResourceManager<?> resourceManager = GetResourceManager(resource.GetStackClass());
        if(resourceManager == null)
        {
            CraftingPatternCached cache = new CraftingPatternCached(null, PatternCacheType.NO_PATTERN, resource, null, null);
            patternCache.put(resource, cache);
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
                //ToDo: Handle multiple crafts for a recipe, perhaps use a hashmultimap
                CraftingPatternCached cache = new CraftingPatternCached(null, PatternCacheType.NO_PATTERN, resource, null, null);
                patternCache.put(resource, cache);
                return cache;
            }
        }
        if(selectedPattern == null)
        {
            CraftingPatternCached cache = new CraftingPatternCached(null, PatternCacheType.NO_PATTERN, resource, null, null);
            patternCache.put(resource, cache);
            return cache;
        }

        HashSet<ResourceKey> remainders = new HashSet<>();
        for(Iterator<CraftingPattern.Entry> it = selectedPattern.GetIngredients().iterator(); it.hasNext();)
        {
            CraftingPatternCached ingredientRecipe = GetCraftingPattern(it.next().stackKey());
            if(ingredientRecipe.type.HasRemainder())
            {
                remainders.addAll(ingredientRecipe.allRemainders());
            }
        }

        selectedPattern.GetResults().map(CraftingPattern.Entry::stackKey).filter(resourceKey ->!resourceKey.equals(resource)).forEach(remainders::add);
        selectedPattern.GetByproducts().map(CraftingPattern.Entry::stackKey).forEach(remainders::add);

        if(remainders.isEmpty())
        {
            CraftingPatternCached cache = new CraftingPatternCached(selectedPattern, PatternCacheType.IS_SIMPLE, resource, null, null);
            patternCache.put(resource, cache);
            return cache;
        }
        else
        {
            //check to see if any crafts need to use one of the remainders
            ArrayList<CraftingPattern> patternsToCheck = new ArrayList<>();
            HashSet<CraftingPattern> checkedPatterns = new HashSet<>();
            patternsToCheck.add(selectedPattern);
            checkedPatterns.add(selectedPattern);

            HashSet<ResourceKey> loopedRemainders = new HashSet<>();
            while(!patternsToCheck.isEmpty())
            {
                CraftingPattern patternToCheck = patternsToCheck.removeFirst();
                patternToCheck.GetIngredients().map(CraftingPattern.Entry::stackKey).filter(remainders::contains).forEach(loopedRemainders::add);

                patternToCheck.GetIngredients().forEach((ingredient)->
                {
                    CraftingPatternCached ingredientRecipe = GetCraftingPattern(ingredient.stackKey());
                    if(ingredientRecipe.pattern() != null)
                    {
                        if(checkedPatterns.add(ingredientRecipe.pattern()))
                        {
                            patternsToCheck.add(ingredientRecipe.pattern());
                        }
                    }
                });
            }
            CraftingPatternCached cache;
            if(loopedRemainders.isEmpty())
            {
                cache = new CraftingPatternCached(selectedPattern, PatternCacheType.IS_SIMPLE_WTH_REMAINDER, resource, new ArrayList<>(remainders), null);
            }
            else
            {
                cache = new CraftingPatternCached(selectedPattern, PatternCacheType.IS_ADVANCED, resource, new ArrayList<>(remainders), new ArrayList<>(loopedRemainders));
            }
            patternCache.put(resource, cache);
            return cache;
        }
    }

    //modifies simpleCraftBuilder even if it fails
    private boolean TrySimpleCraft(@NotNull CraftBuilder craftBuilder, @NotNull ResourceKey stack, int count)
    {
        CraftingPatternCached simpleCraftCache = GetCraftingPattern(stack);
        if(simpleCraftCache.type.IsAdvanced())
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
            CraftResource craftResource = craftBuilder.GetCraftResource(ingredient.stackKey());
            int numAvailable = craftResource.GetNumAvailable();
            if(ingredient.max() <= numAvailable)
            {
                craftResource.consumedCount += ingredient.max();
                continue;
            }

            int countToCraft = ingredient.max() - numAvailable;
            if(!TrySimpleCraft(craftBuilder, ingredient.stackKey(), countToCraft))
            {
                return false;
            }
            craftResource.consumedCount += ingredient.max();
        }
        CraftResource resultCraftResource = craftBuilder.GetCraftResource(stack);
        resultCraftResource.craftedCount += count;
        assert craftResult.remainders.isEmpty();
        //since this is a simple craft the only possible remainder is excess from the craft
        resultCraftResource.craftedCount += craftResult.resultLeftover.max();
        resultCraftResource.numCrafts += craftResult.maxCrafts;

        return true;
    }

    private AdvancedCraftStep BuildAdvanceCraft(@NotNull CraftBuilder craftBuilder, @NotNull ResourceKey targetStackKey, int count)
    {
        CraftingPatternCached craftingPatternCache = GetCraftingPattern(targetStackKey);
        if(craftingPatternCache.type == PatternCacheType.NO_PATTERN)
        {
            return null;
        }

        if(!craftingPatternCache.type.IsAdvanced())
        {
            if(TrySimpleCraft(craftBuilder, targetStackKey, count))
            {
                return new AdvancedCraftStep();
            }
            else
            {
                return null;
            }
        }


        ArrayList<CraftingPatternCached> ingredientCrafts = new ArrayList<>();
        int advancedIngredientCount = 0;

        for(Iterator<CraftingPattern.Entry> it = craftingPatternCache.pattern().GetIngredients().iterator(); it.hasNext();)
        {
            CraftingPattern.Entry ingredient = it.next();
            CraftingPatternCached ingredientCraftingPatternCache = GetCraftingPattern(ingredient.stackKey());
            if(ingredientCraftingPatternCache.type().HasRemainder())
            {
                ingredientCrafts.addFirst(ingredientCraftingPatternCache);
                advancedIngredientCount += 1;
            }
            else
            {
                ingredientCrafts.addLast(ingredientCraftingPatternCache);
            }
        }

        if(advancedIngredientCount <= 1)
        {
            AdvancedCraftStep rootCraftStep = new AdvancedCraftStep();
            CraftResult craftResult = GetIngredientsForCraftCount(craftingPatternCache.pattern(), targetStackKey, 1);
            assert craftResult != null;
            int craftedCount = 1 + craftResult.resultLeftover().max();
            for(int c = 0; c < count; c += craftedCount)
            {
                for(CraftingPatternCached ingredientCraft : ingredientCrafts)
                {
                    int expectedCount = craftResult.ingredients().stream()
                            .filter((ingredient)->ingredient.stackKey().equals(ingredientCraft.targetResourceKey))
                            .map(CraftingPattern.Entry::max)
                            .findFirst().orElse(0);
                    CraftResource ingredientResource = craftBuilder.GetCraftResource(ingredientCraft.targetResourceKey());
                    int numAvailable = ingredientResource.GetNumAvailable();
                    if(expectedCount <= numAvailable)
                    {
                        ingredientResource.consumedCount += expectedCount;
                    }
                    else
                    {
                        int numToCraft = expectedCount - numAvailable;
                        AdvancedCraftStep ingredientCraftStep = BuildAdvanceCraft(craftBuilder, ingredientCraft.targetResourceKey, numToCraft);
                        if(ingredientCraftStep == null)
                        {
                            return null;
                        }
                        if(ingredientCraftStep.craftCount > 0 || !ingredientCraftStep.childCraftSteps.isEmpty())
                        {
                             rootCraftStep.childCraftSteps.add(ingredientCraftStep);
                        }
                        ingredientResource.consumedCount += expectedCount;
                    }
                }
                CraftResource resultResource = craftBuilder.GetCraftResource(craftResult.resultLeftover.stackKey());
                resultResource.numCrafts += 1;
                resultResource.craftedCount += craftedCount;

                for(CraftingPattern.Entry remainder : craftResult.remainders)
                {
                    CraftResource remainderResource = craftBuilder.GetCraftResource(remainder.stackKey());
                    remainderResource.remainingCount += remainder.max();
                }
            }
            return rootCraftStep;
        }
        else
        {
            throw new NotImplementedException();
        }
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

        CraftingPattern.Entry leftoverEntry = null;
        for(Iterator<CraftingPattern.Entry> it = pattern.GetResults().iterator(); it.hasNext();)
        {
            CraftingPattern.Entry result = it.next();
            if(result.stackKey().equals(targetResult))
            {
				int maxResult = maxCrafts == Integer.MAX_VALUE ? Integer.MAX_VALUE : result.max() * maxCrafts;
				if(maxResult <= count)
				{
					continue;
				}

                leftoverEntry = new CraftingPattern.Entry
                (
					result.stackKey(),
					result.min() * minCrafts - count,
					result.expected() * expectedCrafts - count,
                    maxCrafts == Integer.MAX_VALUE ? Integer.MAX_VALUE : maxResult - count
                );
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
        }

        if(leftoverEntry == null)
        {
            leftoverEntry = new CraftingPattern.Entry(targetResult, 0, 0.f, 0);
        }

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
        return new CraftResult(minCrafts, expectedCrafts, maxCrafts, ingredients, leftoverEntry, remainders);
    }

    private SimpleCraftExecutor ReconstructSimpleCraftSteps(CraftBuilder craftBuilder)
    {
        ArrayList<SimpleCraftExecutor.Ingredient> ingredients = new ArrayList<>();
        ArrayList<SimpleCraftExecutor.Craft> crafts = new ArrayList<>();
        for(var entry : craftBuilder.craftResources.entrySet())
        {
            int requestFromInventoryCount = entry.getValue().consumedCount - entry.getValue().craftedCount;
            if(requestFromInventoryCount > 0)
            {
                ingredients.add(new CraftExecutor.Ingredient(entry.getKey(), requestFromInventoryCount));
            }

            if(entry.getValue().numCrafts > 0)
            {
                CraftingPatternCached simpleCraftCache = GetCraftingPattern(entry.getKey());
                assert simpleCraftCache.type == PatternCacheType.IS_SIMPLE;
                crafts.add(new SimpleCraftExecutor.Craft(simpleCraftCache.pattern, entry.getValue().numCrafts));
            }
        }

        for(int i = 0; i < crafts.size() - 1; ++i)
        {
            for(int j = i; j < crafts.size(); ++j)
            {
                int finalJ = j;
                boolean allIngredientCraftsAdded = crafts.get(j).pattern().GetIngredients().allMatch((ingredient)->
                {
                     CraftResource craftResource = craftBuilder.craftResources.get(ingredient.stackKey());
                     if(craftResource == null || craftResource.numCrafts == 0)
                     {
                         return true;
                     }
                     else
                     {
                         return crafts.stream().limit(finalJ).anyMatch(sortedCraft->
                             sortedCraft.pattern().GetResults().findFirst().stream().allMatch((r)->r.stackKey().equals(ingredient.stackKey()))
                         );
                     }
                });
                if(allIngredientCraftsAdded)
                {
                    if(i != j)
                    {
                        var temp = crafts.get(i);
                        crafts.set(i, crafts.get(j));
                        crafts.set(j, temp);
                    }
                    break;
                }
            }
        }

        return new SimpleCraftExecutor(ingredients, crafts);
    }
}
