package settop.IgnesFatui.WispNetwork.Resource.Crafting;

import settop.IgnesFatui.WispNetwork.Resource.ResourceKey;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

public class CraftingPattern
{
    public record Entry(ResourceKey stackKey, int min, float expected, int max)
    {
        public boolean IsValid()
        {
            return stackKey != null &&
                    0 <= min &&
                    min <= expected &&
                    expected <= max &&
                    min <= max &&
                    max >= 1;
        }
    }
    public enum PatternComparisonResult
    {
        Superior,
        Same,
        Inferior,
        Incomparable
    }
    public static final ArrayList<Entry> EMPTY_ENTRIES = new ArrayList<>();

    private final ArrayList<Entry> ingredients;
    private final ArrayList<Entry> results;
    private final ArrayList<Entry> byproducts;

    public CraftingPattern(ArrayList<Entry> ingredients, ArrayList<Entry> results, ArrayList<Entry> byproducts)
    {
        this.ingredients = ingredients;
        this.results = results;
        this.byproducts = byproducts;
    }

    public Stream<Entry> GetIngredients() { return ingredients.stream(); }
    public Stream<Entry> GetResults() { return results.stream(); }
    public Stream<Entry> GetByproducts() { return byproducts.stream(); }

    public int GetIngredientCount() { return ingredients.size(); }
    public int GetResultsCount() { return results.size(); }
    public int GetByproductsCount() { return byproducts.size(); }

    public boolean IsValid()
    {
        return !results.isEmpty() &&
                ingredients.stream().allMatch(Entry::IsValid) &&
                results.stream().allMatch(Entry::IsValid) &&
                byproducts.stream().allMatch(Entry::IsValid);
    }

    public PatternComparisonResult CompareTo(CraftingPattern otherPattern)
    {
        //a pattern is superior if it uses the same or fewer ingredients and gives the same or greater results
        PatternComparisonResult ingredientComparison = FlippedComparisonResult(CompareEntries(ingredients, otherPattern.ingredients));
        if(ingredientComparison == PatternComparisonResult.Incomparable)
        {
            return PatternComparisonResult.Incomparable;
        }

        //ignore byproducts
        PatternComparisonResult resultsComparison = CompareEntries(results, otherPattern.results);
        if(resultsComparison == PatternComparisonResult.Incomparable)
        {
            return PatternComparisonResult.Incomparable;
        }
        if(ingredientComparison == PatternComparisonResult.Same)
        {
            return resultsComparison;
        }
        else if(resultsComparison == PatternComparisonResult.Same)
        {
            return ingredientComparison;
        }
        else if(ingredientComparison == resultsComparison)
        {
            return ingredientComparison;
        }
        else
        {
            return PatternComparisonResult.Incomparable;
        }
    }

    private static PatternComparisonResult FlippedComparisonResult(PatternComparisonResult comparisonResult)
    {
        return switch (comparisonResult)
        {
            case Superior -> PatternComparisonResult.Inferior;
            case Same -> PatternComparisonResult.Same;
            case Inferior -> PatternComparisonResult.Superior;
            case Incomparable -> PatternComparisonResult.Incomparable;
        };
    }

    private static PatternComparisonResult CompareEntries(ArrayList<Entry> source, ArrayList<Entry> comparison)
    {
        if(comparison.size() < source.size())
        {
            PatternComparisonResult flippedResult = CompareEntries(comparison, source);
            return FlippedComparisonResult (flippedResult);
        }

        //check if everything in the source is included in the comparison
        boolean isInferior = true;
        boolean isSuperior = source.size() == comparison.size();
        for(Entry sourceEntry : source)
        {
            Optional<Entry> foundEntry = comparison.stream().filter(e->e.stackKey().equals(sourceEntry.stackKey())).findFirst();
            //since comparison.size() >= source.size() then if we can't find an entry, then they are incomparable
            if(foundEntry.isEmpty())
            {
                return PatternComparisonResult.Incomparable;
            }
            if(sourceEntry.max() < foundEntry.get().max())
            {
                isSuperior = false;
            }
            else if(sourceEntry.max() > foundEntry.get().max())
            {
                isInferior = false;
            }
        }

        if(isInferior && isSuperior)
        {
            return PatternComparisonResult.Same;
        }
        else if(isInferior)
        {
            return PatternComparisonResult.Inferior;
        }
        else if(isSuperior)
        {
            return PatternComparisonResult.Superior;
        }
        else
        {
            return PatternComparisonResult.Incomparable;
        }
    }
}
