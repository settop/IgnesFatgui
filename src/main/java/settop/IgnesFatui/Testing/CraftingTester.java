package settop.IgnesFatui.Testing;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.WispNetwork.Resource.*;
import settop.IgnesFatui.WispNetwork.Resource.Crafting.CraftExecutor;
import settop.IgnesFatui.WispNetwork.Resource.Crafting.CraftingManager;
import settop.IgnesFatui.WispNetwork.Resource.Crafting.CraftingPattern;
import settop.IgnesFatui.WispNetwork.Resource.Crafting.SimpleCraftExecutor;
import settop.IgnesFatui.WispNetwork.WispNetwork;
import settop.IgnesFatui.WispNetwork.WispNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

@GameTestHolder(IgnesFatui.MOD_ID)
public class CraftingTester
{
    static CraftingPattern GetPlankCraftPattern()
    {
        ArrayList<CraftingPattern.Entry> ingredients = new ArrayList<>(1);
        ingredients.add(new CraftingPattern.Entry(new ItemStackKey(Items.OAK_LOG.getDefaultInstance()), 1, 1, 1));

        ArrayList<CraftingPattern.Entry> results = new ArrayList<>(1);
        results.add(new CraftingPattern.Entry(new ItemStackKey(Items.OAK_PLANKS.getDefaultInstance()), 4, 4, 4));

        return new CraftingPattern(ingredients, results, CraftingPattern.EMPTY_ENTRIES);
    }
    static CraftingPattern GetStickCraftPattern()
    {
        ArrayList<CraftingPattern.Entry> ingredients = new ArrayList<>(1);
        ingredients.add(new CraftingPattern.Entry(new ItemStackKey(Items.OAK_PLANKS.getDefaultInstance()), 2, 2, 2));

        ArrayList<CraftingPattern.Entry> results = new ArrayList<>(1);
        results.add(new CraftingPattern.Entry(new ItemStackKey(Items.STICK.getDefaultInstance()), 4, 4, 4));

        return new CraftingPattern(ingredients, results, CraftingPattern.EMPTY_ENTRIES);
    }
    static CraftingPattern GetFenceCraftPattern()
    {
        ArrayList<CraftingPattern.Entry> ingredients = new ArrayList<>(2);
        ingredients.add(new CraftingPattern.Entry(new ItemStackKey(Items.OAK_PLANKS.getDefaultInstance()), 4, 4, 4));
        ingredients.add(new CraftingPattern.Entry(new ItemStackKey(Items.STICK.getDefaultInstance()), 2, 2, 2));

        ArrayList<CraftingPattern.Entry> results = new ArrayList<>(1);
        results.add(new CraftingPattern.Entry(new ItemStackKey(Items.OAK_FENCE.getDefaultInstance()), 3, 3, 3));

        return new CraftingPattern(ingredients, results, CraftingPattern.EMPTY_ENTRIES);
    }
    static CraftingPattern GetBucketCraftPattern()
    {
        ArrayList<CraftingPattern.Entry> ingredients = new ArrayList<>(1);
        ingredients.add(new CraftingPattern.Entry(new ItemStackKey(Items.IRON_INGOT.getDefaultInstance()), 3, 3, 3));

        ArrayList<CraftingPattern.Entry> results = new ArrayList<>(1);
        results.add(new CraftingPattern.Entry(new ItemStackKey(Items.BUCKET.getDefaultInstance()), 1, 1, 1));

        return new CraftingPattern(ingredients, results, CraftingPattern.EMPTY_ENTRIES);
    }
    static CraftingPattern GetMilkBucketCraftPattern()
    {
        ArrayList<CraftingPattern.Entry> ingredients = new ArrayList<>(1);
        ingredients.add(new CraftingPattern.Entry(new ItemStackKey(Items.BUCKET.getDefaultInstance()), 1, 1, 1));

        ArrayList<CraftingPattern.Entry> results = new ArrayList<>(1);
        results.add(new CraftingPattern.Entry(new ItemStackKey(Items.MILK_BUCKET.getDefaultInstance()), 1, 1, 1));

        return new CraftingPattern(ingredients, results, CraftingPattern.EMPTY_ENTRIES);
    }
    static CraftingPattern GetSugarCraftPattern()
    {
        ArrayList<CraftingPattern.Entry> ingredients = new ArrayList<>(1);
        ingredients.add(new CraftingPattern.Entry(new ItemStackKey(Items.SUGAR_CANE.getDefaultInstance()), 1, 1, 1));

        ArrayList<CraftingPattern.Entry> results = new ArrayList<>(1);
        results.add(new CraftingPattern.Entry(new ItemStackKey(Items.SUGAR.getDefaultInstance()), 1, 1, 1));

        return new CraftingPattern(ingredients, results, CraftingPattern.EMPTY_ENTRIES);
    }
    static CraftingPattern GetCakeCraftPattern()
    {
        ArrayList<CraftingPattern.Entry> ingredients = new ArrayList<>(4);
        ingredients.add(new CraftingPattern.Entry(new ItemStackKey(Items.WHEAT.getDefaultInstance()), 3, 3, 3));
        ingredients.add(new CraftingPattern.Entry(new ItemStackKey(Items.EGG.getDefaultInstance()), 1, 1, 1));
        ingredients.add(new CraftingPattern.Entry(new ItemStackKey(Items.SUGAR.getDefaultInstance()), 2, 2, 2));
        ingredients.add(new CraftingPattern.Entry(new ItemStackKey(Items.MILK_BUCKET.getDefaultInstance()), 3, 3, 3));

        ArrayList<CraftingPattern.Entry> byproducts = new ArrayList<>(1 );
        byproducts.add(new CraftingPattern.Entry(new ItemStackKey(Items.BUCKET.getDefaultInstance()), 3, 3, 3));

        ArrayList<CraftingPattern.Entry> results = new ArrayList<>(1);
        results.add(new CraftingPattern.Entry(new ItemStackKey(Items.CAKE.getDefaultInstance()), 1, 1, 1));

        return new CraftingPattern(ingredients, results, byproducts);
    }

    static void AddResourceSource(ItemResourceManager itemManager, ItemStack itemStack, int count)
    {
        itemManager.AddSource(itemStack, new ResourceSource<ItemStack>(0, count));
    }

    @GameTest(batch = "Crafting", template = "forge:empty3x3x3")
    public static void SimpleCraftTest(@NotNull GameTestHelper helper)
    {
        CraftingPattern plankCrafting = GetPlankCraftPattern();
        CraftingPattern stickCrafting = GetStickCraftPattern();
        CraftingPattern fenceCrafting = GetFenceCraftPattern();

        helper.assertTrue(plankCrafting.IsValid(), "Plank crafting recipe is not valid");
        helper.assertTrue(stickCrafting.IsValid(), "Stick crafting recipe is not valid");
        helper.assertTrue(fenceCrafting.IsValid(), "Fence crafting recipe is not valid");

        ResourcesManager resourcesManager = new ResourcesManager();
        AddResourceSource(resourcesManager.GetItemResourceManager(), Items.OAK_LOG.getDefaultInstance(), 64);
        AddResourceSource(resourcesManager.GetItemResourceManager(), Items.STICK.getDefaultInstance(), 3);
        CraftingManager craftingManager = new CraftingManager(resourcesManager);
        craftingManager.AddCratingPattern(plankCrafting);
        craftingManager.AddCratingPattern(stickCrafting);
        craftingManager.AddCratingPattern(fenceCrafting);

        Optional<CraftExecutor> craftExecutorOptional = craftingManager.TryBuildCraft(Items.OAK_FENCE.getDefaultInstance(), 13);
        helper.assertTrue(craftExecutorOptional.isPresent(), "Failed to craft 13 oak fences");

        SimpleCraftExecutor craftExecutor = (SimpleCraftExecutor)craftExecutorOptional.get();
        helper.assertTrue(craftExecutor != null, "Fence craft is not a simple craft");

        boolean allIngredientsMatch = craftExecutor.GetIngredients().allMatch((ingredient)->
        {
            if(ingredient.stackKey() instanceof ItemStackKey itemStackKey)
            {
                if(itemStackKey.GetItemStack().getItem() == Items.OAK_LOG)
                {
                    return ingredient.count() == 6;
                }
                else if(itemStackKey.GetItemStack().getItem() == Items.STICK)
                {
                    return ingredient.count() == 2;
                }
            }
            return false;
        });
        helper.assertTrue(allIngredientsMatch, "Fence craft has wrong ingredients");

        boolean allCraftsMatch = craftExecutor.GetCrafts().allMatch((craft)->
        {
            if(craft.pattern().GetResults().count() != 1)
            {
                return false;
            }
            ItemStackKey resultItemStackKey = (ItemStackKey)craft.pattern().GetResults().findFirst().get().stackKey();
            if(resultItemStackKey == null)
            {
                return false;
            }
            if(resultItemStackKey.GetItemStack().getItem() == Items.OAK_FENCE)
            {
                return craft.craftCount() == 5;
            }
            else if(resultItemStackKey.GetItemStack().getItem() == Items.OAK_PLANKS)
            {
                return craft.craftCount() == 6;
            }
            else if(resultItemStackKey.GetItemStack().getItem() == Items.STICK)
            {
                return craft.craftCount() == 2;
            }
            return false;
        });

        helper.assertTrue(allCraftsMatch, "Fence craft has wrong crafts");

        helper.succeed();
    }


    @GameTest(batch = "Crafting", template = "forge:empty3x3x3")
    public static void CakeCraftTest(@NotNull GameTestHelper helper)
    {
        CraftingPattern bucketCrafting = GetBucketCraftPattern();
        CraftingPattern milkBucketCrafting = GetMilkBucketCraftPattern();
        CraftingPattern sugarCrafting = GetSugarCraftPattern();
        CraftingPattern cakeCrafting = GetCakeCraftPattern();

        helper.assertTrue(bucketCrafting.IsValid(), "Bucket crafting recipe is not valid");
        helper.assertTrue(milkBucketCrafting.IsValid(), "Milk bucket crafting recipe is not valid");
        helper.assertTrue(sugarCrafting.IsValid(), "Sugar crafting recipe is not valid");
        helper.assertTrue(cakeCrafting.IsValid(), "Cake crafting recipe is not valid");

        ResourcesManager resourcesManager = new ResourcesManager();
        AddResourceSource(resourcesManager.GetItemResourceManager(), Items.IRON_INGOT.getDefaultInstance(), 64);
        AddResourceSource(resourcesManager.GetItemResourceManager(), Items.BUCKET.getDefaultInstance(), 2);
        AddResourceSource(resourcesManager.GetItemResourceManager(), Items.WHEAT.getDefaultInstance(), 64);
        AddResourceSource(resourcesManager.GetItemResourceManager(), Items.EGG.getDefaultInstance(), 64);
        AddResourceSource(resourcesManager.GetItemResourceManager(), Items.SUGAR_CANE.getDefaultInstance(), 64);
        CraftingManager craftingManager = new CraftingManager(resourcesManager);
        craftingManager.AddCratingPattern(bucketCrafting);
        craftingManager.AddCratingPattern(milkBucketCrafting);
        craftingManager.AddCratingPattern(sugarCrafting);
        craftingManager.AddCratingPattern(cakeCrafting);

        Optional<CraftExecutor> craftExecutorOptional = craftingManager.TryBuildCraft(Items.CAKE.getDefaultInstance(), 10);

        helper.succeed();
    }
}
