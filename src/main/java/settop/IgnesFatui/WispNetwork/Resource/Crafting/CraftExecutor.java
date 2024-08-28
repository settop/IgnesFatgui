package settop.IgnesFatui.WispNetwork.Resource.Crafting;

import settop.IgnesFatui.WispNetwork.Resource.ResourceKey;

import java.util.ArrayList;
import java.util.stream.Stream;

public class CraftExecutor
{
    public record Ingredient(ResourceKey stackKey, int count){}
    public record Craft(CraftingPattern pattern, int craftCount){}

    private final ArrayList<Ingredient> ingredients;
    private final ArrayList<Craft> crafts;

    protected CraftExecutor(ArrayList<Ingredient> ingredients, ArrayList<Craft> crafts)
    {
        this.ingredients = ingredients;
        this.crafts = crafts;
    }

    public Stream<Ingredient> GetIngredients()
    {
        return ingredients.stream();
    }

    public Stream<Craft> GetCrafts()
    {
        return crafts.stream();
    }

}
