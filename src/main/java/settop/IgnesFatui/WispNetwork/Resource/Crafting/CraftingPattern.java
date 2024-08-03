package settop.IgnesFatui.WispNetwork.Resource.Crafting;

import settop.IgnesFatui.WispNetwork.Resource.ResourceKey;

import java.util.ArrayList;
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
}
