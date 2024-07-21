package settop.IgnesFatui.WispNetwork.Resource;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemOnlyFilter extends ResourceFilter<ItemStack>
{
    private final Item item;

    public ItemOnlyFilter(@NotNull Item item)
    {
        super(ItemStack.class);
        this.item = item;
    }

    public Item GetItem()
    {
        return item;
    }

    @Override
    public boolean Matches(@NotNull ItemStack resource)
    {
        return resource.getItem() == item;
    }
}
