package settop.IgnesFatui.WispNetwork.Resource.Crafting;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Utils.Utils;
import settop.IgnesFatui.WispNetwork.Resource.ResourceKey;
import settop.IgnesFatui.WispNetwork.Resource.ResourceManager;
import settop.IgnesFatui.WispNetwork.Resource.ResourcesManager;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CraftingManager
{
    private record CraftResult(int minCrafts, float expectedCrafts, int maxCrafts, ArrayList<CraftingPattern.Entry> ingredients, CraftingPattern.Entry resultLeftover, ArrayList<CraftingPattern.Entry> remainders) {}
    private record CraftingPatternCached(CraftingPattern pattern, PatternCacheType type, ResourceKey targetResourceKey, ArrayList<ResourceKey> allRemainders, ArrayList<ResourceKey> loopedRemainders) {}
    private static class CraftScore
    {
        private int craftCount = 0;
        private int craftBatchCount = 0;
        private float byproductScore = 0;
        public static Comparator<CraftScore> SCORE_COMPARATOR = (l, r) ->
        {
            //prioritise the least number of crafts
            //then followed by the least number of craft batches
            //finally, the least remainder
            int c = Integer.compare(l.craftCount, r.craftCount);
            if(c != 0)
            {
                return c;
            }
            c = Integer.compare(l.craftBatchCount, r.craftBatchCount);
            if(c != 0)
            {
                return c;
            }
            return Float.compare(l.byproductScore, r.byproductScore);
        };

        @Override
        public boolean equals(Object obj)
        {
            if(obj instanceof CraftScore otherScore)
            {
                return craftCount == otherScore.craftCount &&
                        craftBatchCount == otherScore.craftBatchCount &&
                        byproductScore == otherScore.byproductScore;
            }
            return false;
        }

        public void AddCraftBatch(int numCrafts)
        {
            craftCount += numCrafts;
            craftBatchCount += 1;
        }

        public void Add(CraftScore otherCraftScore)
        {
            craftCount += otherCraftScore.craftCount;
            craftBatchCount += otherCraftScore.craftBatchCount;
            byproductScore += otherCraftScore.byproductScore;
        }

        public CraftScore Clone()
        {
            CraftScore newScore = new CraftScore();
            newScore.craftCount = craftCount;
            newScore.craftBatchCount = craftBatchCount;
            newScore.byproductScore = byproductScore;
            return newScore;
        }
    }

    private static class AdvancedCraftSequence
    {
        public final ArrayList<AdvancedCraftStep> craftSteps = new ArrayList<>();
        public final CraftBuilder simpleCraftBuilder;
        public final CraftScore totalCraftScore;
        public boolean finished = false;
        public boolean failed = false;

        private AdvancedCraftSequence(@NotNull CraftBuilder simpleCraftBuilder, @NotNull CraftScore totalCraftScore)
        {
            this.simpleCraftBuilder = simpleCraftBuilder;
            this.totalCraftScore = totalCraftScore;
        }

        public AdvancedCraftSequence Clone()
        {
            AdvancedCraftSequence newSequence = new AdvancedCraftSequence(simpleCraftBuilder.Clone(), totalCraftScore.Clone());
            newSequence.finished = finished;
            newSequence.failed = failed;
            newSequence.craftSteps.ensureCapacity(craftSteps.size());
            for(AdvancedCraftStep step : craftSteps)
            {
                newSequence.craftSteps.add(step.Clone());
            }
            return newSequence;
        }
    }

    //ToDo: Split this into two classes with a base class, a craft step and a request step
    private static class AdvancedCraftStep
    {
        public final CraftingPatternCached patternCached;
        public final int numCrafts;
        public final ResourceKey targetResource;
        public int requiredCount;
        public boolean craftingOnly;

        public AdvancedCraftStep(@NotNull ResourceKey targetResource, int requiredCount, boolean craftingOnly)
        {
            this.patternCached = null;
            this.numCrafts = 0;
            this.targetResource = targetResource;
            this.requiredCount = requiredCount;
            this.craftingOnly = craftingOnly;
        }

        public AdvancedCraftStep(@NotNull ResourceKey targetResource, CraftingPatternCached craftingPattern, int numCrafts)
        {
            this.patternCached = craftingPattern;
            this.numCrafts = numCrafts;
            this.targetResource = targetResource;
            this.requiredCount = 0;
            this.craftingOnly = true;
        }

        public boolean CanBeExpanded()
        {
            return patternCached == null && requiredCount > 0;
        }

        public AdvancedCraftStep Clone()
        {
            if(CanBeExpanded())
            {
                return new AdvancedCraftStep(targetResource, requiredCount, craftingOnly);
            }
            else
            {
                return this;
            }
        }
    }

    private static class CraftResource
    {
        int inventoryCount = 0;
        int numSimpleCrafts = 0;
        int craftedCount = 0;
        int remainingCount = 0;
        int consumedCount = 0;

        int GetNumAvailable()
        {
            return inventoryCount + craftedCount + remainingCount - consumedCount;
        }

        int GetInventoryRequestedCount()
        {
            return consumedCount - craftedCount - remainingCount;
        }

        CraftResource Clone()
        {
            CraftResource clone = new CraftResource();
            clone.inventoryCount = inventoryCount;
            clone.numSimpleCrafts = numSimpleCrafts;
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
        NO_PATTERN( false),
        IS_SIMPLE( false),//a simple craft, ie can be computed in a single batch
        IS_ADVANCED( true),//a more complex craft, that might need to be ordered to loop its remainders back into itself
        HAS_MULTIPLE( true),//there are multiple recipes to craft this item
        INGREDIENTS_HAS_MULTIPLE( true);//this crafting recipe has an ingredient that has multiple crafts

        private final boolean isAdvanced;

        PatternCacheType(boolean isAdvanced)
        {
            this.isAdvanced = isAdvanced;
        }

        public boolean IsAdvanced() { return isAdvanced; }
    }

    private final ResourcesManager resourcesManager;
    private final HashMap<ResourceKey, CraftingPatternCached> patternCache = new HashMap<>();
    private final HashMap<ResourceKey, ArrayList<CraftingPatternCached>> multiPatternCache = new HashMap<>();
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
            if (TrySimpleCraft(craftBuilder, null, stack, count))
            {
                return Optional.of(ReconstructSimpleCraftSteps(craftBuilder));
            }
        }
        else
        {
            AdvancedCraftSequence craftSequence = BuildAdvanceCraft(stack, count);
            if(craftSequence != null)
            {
                return Optional.of(ReconstructSimpleCraftSteps(craftSequence));
            }
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
        CraftingPatternCached firstPatternCache = null;
        ArrayList<CraftingPatternCached> allPatterns = null;
        HashSet<ResourceKey> allRemainders = null;
        for(Iterator<CraftingPattern> it = craftingPatterns.iterator(); it.hasNext();)
        {
            if(firstPatternCache == null)
            {
                firstPatternCache = CreatePatternCacheFromPattern(it.next(), resource);
            }
            else
            {
                //more than one pattern for this resource
                if(allPatterns == null)
                {
                    allPatterns = new ArrayList<>(2);
                    allPatterns.add(firstPatternCache);
                    if(firstPatternCache.allRemainders != null)
                    {
                        allRemainders = new HashSet<>(firstPatternCache.allRemainders);
                    }
                }

                CraftingPatternCached cache = CreatePatternCacheFromPattern(it.next(), resource);
                if(cache.allRemainders != null)
                {
                    if(allRemainders == null)
                    {
                        allRemainders = new HashSet<>();
                    }
                    allRemainders.addAll(cache.allRemainders);
                }

                boolean added = false;
                for(int i = 0; i < allPatterns.size(); ++i)
                {
                    CraftingPattern.PatternComparisonResult comparison = allPatterns.get(i).pattern().CompareTo(cache.pattern());
                    switch (comparison)
                    {
                        case Superior:
                        case Same:
                            //don't add this
                            added = true;
                            break;
                        case Inferior:
                            //replace this entry as it's inferior to the new cache pattern
                            if(!added)
                            {
                                allPatterns.set(i, cache);
                            }
                            else
                            {
                                allPatterns.remove(i);
                                --i;
                            }
                            added = true;
                            break;
                        case Incomparable:
                            break;
                    }
                }

                if(!added)
                {
                    allPatterns.add(cache);
                }
            }
        }
        if(allPatterns != null && allPatterns.size() == 1)
        {
            firstPatternCache = allPatterns.getFirst();
            allPatterns = null;
        }

        if(allPatterns != null)
        {
            ArrayList<ResourceKey> allRemaindersList = allRemainders != null ? new ArrayList<>(allRemainders) : null;
            CraftingPatternCached cache = new CraftingPatternCached(null, PatternCacheType.HAS_MULTIPLE, resource, allRemaindersList, null);
            patternCache.put(resource, cache);
            multiPatternCache.put(resource, allPatterns);
            return cache;
        }
        else if(firstPatternCache != null)
        {
            patternCache.put(resource, firstPatternCache);
            return firstPatternCache;
        }
        else
        {
            CraftingPatternCached cache = new CraftingPatternCached(null, PatternCacheType.NO_PATTERN, resource, null, null);
            patternCache.put(resource, cache);
            return cache;
        }
    }

    private CraftingPatternCached CreatePatternCacheFromPattern(@NotNull CraftingPattern pattern, @NotNull ResourceKey resource)
    {
        HashSet<ResourceKey> remainders = new HashSet<>();
        boolean anIngredientHasMultiple = false;
        for(Iterator<CraftingPattern.Entry> it = pattern.GetIngredients().iterator(); it.hasNext();)
        {
            ResourceKey ingredient = it.next().stackKey();
            CraftingPatternCached ingredientRecipe = GetCraftingPattern(ingredient);
            switch (ingredientRecipe.type)
            {
                case NO_PATTERN:
                case IS_SIMPLE:
                case IS_ADVANCED:
                    if(ingredientRecipe.allRemainders() != null)
                    {
                        for(ResourceKey remainder : ingredientRecipe.allRemainders())
                        {
                            if(!remainder.equals(resource))
                            {
                                remainders.add(remainder);
                            }
                        }
                    }
                    break;
                case HAS_MULTIPLE:
                case INGREDIENTS_HAS_MULTIPLE:
                    anIngredientHasMultiple = true;
                    if(ingredientRecipe.allRemainders() != null)
                    {
                        for(ResourceKey remainder : ingredientRecipe.allRemainders())
                        {
                            if(!remainder.equals(resource))
                            {
                                remainders.add(remainder);
                            }
                        }
                    }
                    break;
            }
        }

        for(Iterator<CraftingPattern.Entry> it = pattern.GetIngredients().iterator(); it.hasNext();)
        {
            remainders.remove(it.next().stackKey());
        }

        pattern.GetResults().map(CraftingPattern.Entry::stackKey).filter(resourceKey ->!resourceKey.equals(resource)).forEach(remainders::add);
        pattern.GetByproducts().map(CraftingPattern.Entry::stackKey).forEach(remainders::add);

        if(remainders.isEmpty() && !anIngredientHasMultiple)
        {
            return new CraftingPatternCached(pattern, PatternCacheType.IS_SIMPLE, resource, null, null);
        }
        else
        {
            //check to see if any crafts need to use one of the remainders
            ArrayList<CraftingPattern> patternsToCheck = new ArrayList<>();
            HashSet<CraftingPattern> checkedPatterns = new HashSet<>();
            patternsToCheck.add(pattern);
            checkedPatterns.add(pattern);

            HashSet<ResourceKey> loopedRemainders = new HashSet<>();
            while(!patternsToCheck.isEmpty())
            {
                CraftingPattern patternToCheck = patternsToCheck.removeFirst();
                patternToCheck.GetIngredients().map(CraftingPattern.Entry::stackKey).filter(remainders::contains).forEach(loopedRemainders::add);

                patternToCheck.GetIngredients().forEach((ingredient)->
                {
                    CraftingPatternCached ingredientRecipe = GetCraftingPattern(ingredient.stackKey());
                    if(ingredientRecipe.type == PatternCacheType.HAS_MULTIPLE)
                    {
                        for(CraftingPatternCached subPattern : multiPatternCache.get(ingredient.stackKey()))
                        {
                            if(checkedPatterns.add(subPattern.pattern()))
                            {
                                patternsToCheck.add(subPattern.pattern());
                            }
                        }
                    }
                    else if(ingredientRecipe.type != PatternCacheType.NO_PATTERN)
                    {
                        if(checkedPatterns.add(ingredientRecipe.pattern()))
                        {
                            patternsToCheck.add(ingredientRecipe.pattern());
                        }
                    }
                });
            }
            if(loopedRemainders.isEmpty())
            {
                return new CraftingPatternCached(pattern, anIngredientHasMultiple ? PatternCacheType.INGREDIENTS_HAS_MULTIPLE : PatternCacheType.IS_SIMPLE, resource, new ArrayList<>(remainders), null);
            }
            else
            {
                return new CraftingPatternCached(pattern, PatternCacheType.IS_ADVANCED, resource, new ArrayList<>(remainders), new ArrayList<>(loopedRemainders));
            }
        }
    }

    //modifies simpleCraftBuilder even if it fails
    private boolean TrySimpleCraft(@NotNull CraftBuilder craftBuilder, @Nullable CraftScore craftScore, @NotNull ResourceKey stack, int count)
    {
        CraftingPatternCached simpleCraftCache = GetCraftingPattern(stack);
        return TrySimpleCraft(craftBuilder, craftScore, simpleCraftCache, count);
    }

    //modifies simpleCraftBuilder even if it fails
    private boolean TrySimpleCraft(@NotNull CraftBuilder craftBuilder, @Nullable CraftScore craftScore, @NotNull CraftingPatternCached simpleCraftCache, int count)
    {
        if(simpleCraftCache.type.IsAdvanced() || simpleCraftCache.type == PatternCacheType.NO_PATTERN)
        {
            return false;
        }
        //a simple recipe so all max==min

        CraftResult craftResult = GetIngredientsForCraftCount(simpleCraftCache.pattern, simpleCraftCache.targetResourceKey, count);
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
            if(!TrySimpleCraft(craftBuilder, craftScore, ingredient.stackKey(), countToCraft))
            {
                return false;
            }
            craftResource.consumedCount += ingredient.max();
        }
        CraftResource resultCraftResource = craftBuilder.GetCraftResource(simpleCraftCache.targetResourceKey);
        resultCraftResource.craftedCount += count + craftResult.resultLeftover.max();
        resultCraftResource.numSimpleCrafts += craftResult.maxCrafts;
        for(CraftingPattern.Entry remainder : craftResult.remainders)
        {
            CraftResource remainderCraftResource = craftBuilder.GetCraftResource(remainder.stackKey());
            remainderCraftResource.remainingCount += remainder.max();
        }
        if(craftScore != null)
        {
            craftScore.AddCraftBatch(craftResult.maxCrafts);
        }

        return true;
    }

    //returns either ArrayList<AdvancedCraftSequence> or AdvancedCraftSequence
    private @NotNull Object GetCraftSteps(@NotNull AdvancedCraftSequence sourceSequence)
    {
        //find the next step that needs expanding
        int expandingStepIndex = 0;
        for(; expandingStepIndex < sourceSequence.craftSteps.size(); ++expandingStepIndex)
        {
            AdvancedCraftStep step = sourceSequence.craftSteps.get(expandingStepIndex);
            if(!step.CanBeExpanded())
            {
                continue;
            }
            if(!step.craftingOnly)
            {
                //first try to request the required amount from the existing
                CraftResource targetCraftResource = sourceSequence.simpleCraftBuilder.GetCraftResource(step.targetResource);
                int numAvailable = targetCraftResource.GetNumAvailable();
                if(step.requiredCount <= numAvailable)
                {
                    //we can just request all of what we need now, so do that
                    targetCraftResource.consumedCount += step.requiredCount;
                    sourceSequence.craftSteps.remove(expandingStepIndex);
                    --expandingStepIndex;
                    continue;
                }
                else if(numAvailable > 0)
                {
                    step.requiredCount -= numAvailable;
                    targetCraftResource.consumedCount += numAvailable;
                }
                step.craftingOnly = true;
            }
            break;
        }

        if(expandingStepIndex >= sourceSequence.craftSteps.size())
        {
            sourceSequence.finished = true;
            sourceSequence.failed = false;
            return sourceSequence;
        }

        AdvancedCraftStep expandingStep = sourceSequence.craftSteps.get(expandingStepIndex);

        CraftingPatternCached craftingPatternCache = GetCraftingPattern(expandingStep.targetResource);
        if(craftingPatternCache.type == PatternCacheType.NO_PATTERN)
        {
            sourceSequence.finished = true;
            sourceSequence.failed = true;
            return sourceSequence;
        }

        return GetCraftStepsFromPattern(sourceSequence, expandingStepIndex, craftingPatternCache);
    }

    //returns either ArrayList<AdvancedCraftSequence> or AdvancedCraftSequence
    private @NotNull Object GetCraftStepsFromPattern(@NotNull AdvancedCraftSequence sourceSequence, int expandingStepIndex, CraftingPatternCached pattern)
    {
        if(pattern.type == PatternCacheType.HAS_MULTIPLE)
        {
            ArrayList<AdvancedCraftSequence> results = new ArrayList<>();
            ArrayList<CraftingPatternCached> subPatterns = multiPatternCache.get(pattern.targetResourceKey);
            int numSubPatterns = subPatterns.size();
            for(int i = 0 ; i < numSubPatterns; ++i)
            {
                AdvancedCraftSequence subSourceSequence = i + 1 == numSubPatterns ? sourceSequence : sourceSequence.Clone();
                Object result = GetCraftStepsFromPattern(subSourceSequence, expandingStepIndex, subPatterns.get(i));
                if(result instanceof AdvancedCraftSequence subSequence)
                {
                    boolean hasFailed = subSequence.finished && subSequence.failed;
                    if(!hasFailed)
                    {
                        results.add(subSequence);
                    }
                }
                else if(result instanceof ArrayList<?> subSequences)
                {
                    results.addAll((ArrayList<AdvancedCraftSequence>)subSequences);
                }
            }

            if(results.isEmpty())
            {
                sourceSequence.finished = true;
                sourceSequence.failed = true;
                return sourceSequence;
            }
            else if(results.size() == 1)
            {
                return results.getFirst();
            }
            else
            {
                return results;
            }
        }

        AdvancedCraftStep expandingStep = sourceSequence.craftSteps.get(expandingStepIndex);

        if(!pattern.type.IsAdvanced())
        {
            CraftResource targetCraftResource = sourceSequence.simpleCraftBuilder.GetCraftResource(pattern.targetResourceKey);
            int initialSimpleCraftCount = targetCraftResource.numSimpleCrafts;
            boolean simpleCraftSuccess = TrySimpleCraft(sourceSequence.simpleCraftBuilder, sourceSequence.totalCraftScore, pattern, expandingStep.requiredCount);
            if(simpleCraftSuccess)
            {
                //can't cache the crafts inside the simple craft count, since this is technically not a simple craft, it's a part of an advanced craft
                //so can't cache it as a simple craft as it will confuse the craft reconstruction step later
                int numCraftsDone = targetCraftResource.numSimpleCrafts - initialSimpleCraftCount;
                targetCraftResource.numSimpleCrafts = initialSimpleCraftCount;
                targetCraftResource.consumedCount += expandingStep.requiredCount;
                sourceSequence.craftSteps.set(expandingStepIndex, new AdvancedCraftStep(pattern.targetResourceKey, pattern, numCraftsDone));
            }
            else
            {
                sourceSequence.finished = true;
                sourceSequence.failed = true;
            }
            return sourceSequence;
        }

        CraftResult craftResult = GetIngredientsForCraftCount(pattern.pattern(), expandingStep.targetResource, 1);
        assert craftResult != null;

        ArrayList<CraftingPatternCached> ingredientCrafts = new ArrayList<>();
        int ingredientsWithRemainderCount = 0;

        for(Iterator<CraftingPattern.Entry> it = pattern.pattern().GetIngredients().iterator(); it.hasNext();)
        {
            CraftingPattern.Entry ingredient = it.next();
            CraftingPatternCached ingredientCraftingPatternCache = GetCraftingPattern(ingredient.stackKey());
            if(ingredientCraftingPatternCache.allRemainders != null)
            {
                ingredientCrafts.addFirst(ingredientCraftingPatternCache);
                ingredientsWithRemainderCount += 1;
            }
            else
            {
                ingredientCrafts.addLast(ingredientCraftingPatternCache);
            }
        }

        if(ingredientsWithRemainderCount <= 1)
        {
            ExpandCraftStepWithOrder(sourceSequence, expandingStepIndex, pattern, ingredientCrafts, craftResult);
            return sourceSequence;
        }
        else
        {
            if(ingredientsWithRemainderCount >= 5)
            {
                //don't go above 4
                IgnesFatui.LOGGER.warn("Craft ingredient ordering was restricted to 4 for pattern {}", pattern.targetResourceKey.toString());
                ingredientsWithRemainderCount = 4;
            }
            final int numOrderings = IntStream.rangeClosed(2, ingredientsWithRemainderCount).reduce(1, (a, b) -> a * b);
            ArrayList<AdvancedCraftSequence> allCraftSteps = new ArrayList<>(numOrderings);
            AtomicInteger step = new AtomicInteger();
            //can reduce the number of permutations by checking to see if any of them actually potentially care about ordering(ie check remainders are inputs of the other crafts)
            Utils.IteratePermutations(ingredientCrafts, ingredientsWithRemainderCount, permutedIngredients->
            {
                boolean finalStep = step.incrementAndGet() == numOrderings;
                AdvancedCraftSequence permutationSequence = finalStep ? sourceSequence : sourceSequence.Clone();
                ExpandCraftStepWithOrder(permutationSequence, expandingStepIndex, pattern, permutedIngredients, craftResult);
                boolean failed = permutationSequence.finished && permutationSequence.failed;
                if(!failed)
                {
                    allCraftSteps.add(permutationSequence);
                }
            });
            return allCraftSteps;
        }
    }

    private void ExpandCraftStepWithOrder(@NotNull AdvancedCraftSequence sourceSequence, int expandingStepIndex, CraftingPatternCached pattern, ArrayList<CraftingPatternCached> ingredientCrafts, CraftResult craftResult)
    {
        AdvancedCraftStep expandingStep = sourceSequence.craftSteps.get(expandingStepIndex);
        int craftedCountPerCraft = 1 + craftResult.resultLeftover().max();

        while(expandingStep.requiredCount > 0)
        {
            boolean anyAdvanceIngredientCrafts = false;
            for(CraftingPatternCached ingredientCraft : ingredientCrafts)
            {
                int expectedCount = craftResult.ingredients().stream()
                        .filter((ingredient) -> ingredient.stackKey().equals(ingredientCraft.targetResourceKey))
                        .map(CraftingPattern.Entry::max)
                        .findFirst().orElse(0);
                CraftResource ingredientResource = sourceSequence.simpleCraftBuilder.GetCraftResource(ingredientCraft.targetResourceKey());
                int numAvailable = ingredientResource.GetNumAvailable();
                if(expectedCount <= numAvailable)
                {
                    ingredientResource.consumedCount += expectedCount;
                    continue;
                }
                int numIngredientsToCraft = expectedCount - numAvailable;
                //ToDo: Check if craft can be affected by previous crafts, ie check remainders with input tree, instead of assuming anyAdvanceIngredientCrafts needs to be handled
                if(ingredientCraft.type.IsAdvanced() || anyAdvanceIngredientCrafts)
                {
                    anyAdvanceIngredientCrafts = true;
                    AdvancedCraftStep craftStep = new AdvancedCraftStep(ingredientCraft.targetResourceKey, numIngredientsToCraft, false);
                    sourceSequence.craftSteps.add(expandingStepIndex, craftStep);
                    expandingStepIndex += 1;
                    //consume the rest last after the craft is done so that we don't go negative with the numAvailable
                    ingredientResource.consumedCount += numAvailable;
                }
                else
                {
                    //if it's a simple craft, do it now
                    if(!TrySimpleCraft(sourceSequence.simpleCraftBuilder, sourceSequence.totalCraftScore, ingredientCraft.targetResourceKey(), numIngredientsToCraft))
                    {
                        sourceSequence.finished = true;
                        sourceSequence.failed = true;
                        return;
                    }
                    ingredientResource.consumedCount += expectedCount;
                }
            }

            sourceSequence.totalCraftScore.AddCraftBatch(craftResult.maxCrafts());
            CraftResource targetCraftResource = sourceSequence.simpleCraftBuilder.GetCraftResource(expandingStep.targetResource);
            targetCraftResource.craftedCount += craftedCountPerCraft;
            targetCraftResource.consumedCount += Integer.min(craftedCountPerCraft, expandingStep.requiredCount);
            expandingStep.requiredCount -= craftedCountPerCraft;
            for(CraftingPattern.Entry remainder : craftResult.remainders())
            {
                sourceSequence.simpleCraftBuilder.GetCraftResource(remainder.stackKey()).remainingCount += remainder.max();
            }
            AdvancedCraftStep craftStep = new AdvancedCraftStep(expandingStep.targetResource, pattern, craftResult.maxCrafts());
            sourceSequence.craftSteps.add(expandingStepIndex, craftStep);
            expandingStepIndex += 1;
            if(anyAdvanceIngredientCrafts)
            {
                return;
            }
        }
        //we should now have completed this craft, so can remove it
        sourceSequence.craftSteps.remove(expandingStepIndex);
    }

    private AdvancedCraftSequence BuildAdvanceCraft(@NotNull ResourceKey targetStackKey, int count)
    {
        CraftingPatternCached craftingPatternCache = GetCraftingPattern(targetStackKey);
        if(craftingPatternCache.type == PatternCacheType.NO_PATTERN)
        {
            return null;
        }

        AdvancedCraftSequence initialSequence = new AdvancedCraftSequence(new CraftBuilder(), new CraftScore());
        initialSequence.craftSteps.add(new AdvancedCraftStep(targetStackKey, count, true));

        Comparator<CraftScore> scoreComparator = CraftScore.SCORE_COMPARATOR;
        PriorityQueue<AdvancedCraftSequence> craftSequence = new PriorityQueue<>((l, r)->scoreComparator.compare(l.totalCraftScore, r.totalCraftScore));
        craftSequence.add(initialSequence);

        while (!craftSequence.isEmpty())
        {
            AdvancedCraftSequence currentSequence = craftSequence.poll();
            if(currentSequence.finished)
            {
                assert !currentSequence.failed;
                return currentSequence;
            }
            Object nextSequences = GetCraftSteps(currentSequence);
            if(nextSequences instanceof AdvancedCraftSequence nextSequence)
            {
                boolean stepFailed = nextSequence.finished && nextSequence.failed;
                if(!stepFailed)
                {
                    craftSequence.add(nextSequence);
                }
            }
            else if(nextSequences instanceof ArrayList<?> nextSequencesArray)
            {
                craftSequence.addAll((ArrayList<AdvancedCraftSequence>)nextSequencesArray);
            }
        }
        //we failed
        return null;
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

    private void FillSimpleCraftData(@NotNull CraftBuilder craftBuilder, @NotNull ArrayList<CraftExecutor.Ingredient> ingredients, @NotNull ArrayList<CraftExecutor.Craft> crafts )
    {
        for(var entry : craftBuilder.craftResources.entrySet())
        {
            int requestFromInventoryCount = entry.getValue().GetInventoryRequestedCount();
            if(requestFromInventoryCount > 0)
            {
                ingredients.add(new CraftExecutor.Ingredient(entry.getKey(), requestFromInventoryCount));
            }

            if(entry.getValue().numSimpleCrafts > 0)
            {
                CraftingPatternCached simpleCraftCache = GetCraftingPattern(entry.getKey());
                assert simpleCraftCache.type == PatternCacheType.IS_SIMPLE;
                crafts.add(new CraftExecutor.Craft(simpleCraftCache.pattern, entry.getValue().numSimpleCrafts));
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
                     if(craftResource == null || craftResource.numSimpleCrafts == 0)
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
    }

    private CraftExecutor ReconstructSimpleCraftSteps(CraftBuilder craftBuilder)
    {
        ArrayList<CraftExecutor.Ingredient> ingredients = new ArrayList<>();
        ArrayList<CraftExecutor.Craft> crafts = new ArrayList<>();

        FillSimpleCraftData(craftBuilder, ingredients, crafts);

        return new CraftExecutor(ingredients, crafts);
    }

    private CraftExecutor ReconstructSimpleCraftSteps(AdvancedCraftSequence craftSequence)
    {
        ArrayList<CraftExecutor.Ingredient> ingredients = new ArrayList<>();
        ArrayList<CraftExecutor.Craft> crafts = new ArrayList<>();

        FillSimpleCraftData(craftSequence.simpleCraftBuilder, ingredients, crafts);

        for(AdvancedCraftStep craftStep : craftSequence.craftSteps)
        {
            if(craftStep.patternCached == null)
            {
                continue;
            }
            boolean updatedExistingEntry = false;
            for(int i = 0; i < crafts.size(); ++i)
            {
                CraftExecutor.Craft advancedCraft = crafts.get(i);
                if(advancedCraft.pattern() == craftStep.patternCached.pattern())
                {
                    CraftExecutor.Craft updatedCraft = new CraftExecutor.Craft(advancedCraft.pattern(), advancedCraft.craftCount() + craftStep.numCrafts);
                    crafts.set(i, updatedCraft);
                    updatedExistingEntry = true;
                    break;
                }
            }
            if(!updatedExistingEntry)
            {
                CraftExecutor.Craft newCraft = new CraftExecutor.Craft(craftStep.patternCached.pattern(), craftStep.numCrafts);
                crafts.add(newCraft);
            }
        }

        return new CraftExecutor(ingredients, crafts);
    }
}
