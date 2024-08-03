package settop.IgnesFatui.WispNetwork.Resource.Crafting;

import settop.IgnesFatui.WispNetwork.Resource.ResourceKey;

import java.util.ArrayList;
import java.util.stream.Stream;

public abstract class CraftExecutor
{
    public record Ingredient(ResourceKey stackKey, int count){}

    private final ArrayList<Ingredient> ingredients;

    protected CraftExecutor(ArrayList<Ingredient> ingredients)
    {
        this.ingredients = ingredients;
    }

    public Stream<Ingredient> GetIngredients()
    {
        return ingredients.stream();
    }

}
