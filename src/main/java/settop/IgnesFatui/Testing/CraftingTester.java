package settop.IgnesFatui.Testing;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.WispNetwork.Resource.Crafting.CraftingManager;
import settop.IgnesFatui.WispNetwork.Resource.Crafting.CraftingPattern;
import settop.IgnesFatui.WispNetwork.Resource.ItemResourceManager;
import settop.IgnesFatui.WispNetwork.Resource.ItemStackKey;
import settop.IgnesFatui.WispNetwork.Resource.ResourceSource;
import settop.IgnesFatui.WispNetwork.Resource.ResourcesManager;
import settop.IgnesFatui.WispNetwork.WispNetwork;
import settop.IgnesFatui.WispNetwork.WispNode;

import java.util.ArrayList;
import java.util.Collections;

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

        helper.assertTrue(plankCrafting.IsSimple(), "Plank crafting recipe is not simple");
        helper.assertTrue(stickCrafting.IsSimple(), "Stick crafting recipe is not simple");
        helper.assertTrue(fenceCrafting.IsSimple(), "Fence crafting recipe is not simple");

        ResourcesManager resourcesManager = new ResourcesManager();
        AddResourceSource(resourcesManager.GetItemResourceManager(), Items.OAK_LOG.getDefaultInstance(), 64);
        AddResourceSource(resourcesManager.GetItemResourceManager(), Items.STICK.getDefaultInstance(), 1);
        CraftingManager craftingManager = new CraftingManager(resourcesManager);
        craftingManager.AddCratingPattern(plankCrafting);
        craftingManager.AddCratingPattern(stickCrafting);
        craftingManager.AddCratingPattern(fenceCrafting);

        helper.assertTrue(craftingManager.TryBuildCraft(Items.OAK_FENCE.getDefaultInstance(), 10), "Failed to craft 10 oak fences");

        helper.succeed();
    }

}
