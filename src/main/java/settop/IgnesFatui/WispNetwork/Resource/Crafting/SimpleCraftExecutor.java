package settop.IgnesFatui.WispNetwork.Resource.Crafting;

import java.util.ArrayList;
import java.util.stream.Stream;

public class SimpleCraftExecutor extends CraftExecutor
{
    public record Craft(CraftingPattern pattern, int craftCount){}

    private final ArrayList<Craft> crafts;

    protected SimpleCraftExecutor(ArrayList<Ingredient> ingredients, ArrayList<Craft> crafts)
    {
        super(ingredients);
        this.crafts = crafts;
    }

    public Stream<Craft> GetCrafts()
    {
        return crafts.stream();
    }
}
