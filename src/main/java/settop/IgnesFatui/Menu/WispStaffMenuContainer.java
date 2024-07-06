package settop.IgnesFatui.Menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Utils.ItemStackContainer;

public class WispStaffMenuContainer extends ChestMenu
{
    private static final int NUM_ROWS = 1;
    private static final int NUM_SLOTS = NUM_ROWS * 9;

    public static WispStaffMenuContainer CreateMenuServer(int id, Inventory inventory, ItemStack itemStaff)
    {
        return new WispStaffMenuContainer(id, inventory, new ItemStackContainer(itemStaff, NUM_SLOTS));
    }

    public static WispStaffMenuContainer CreateMenuClient(int id, Inventory inventory)
    {
        return new WispStaffMenuContainer(id, inventory, new SimpleContainer(NUM_SLOTS));
    }

    private WispStaffMenuContainer(int id, Inventory inventory, Container container)
    {
        super(IgnesFatui.ContainerMenus.WISP_STAFF_MENU.get(), id, inventory, container, NUM_ROWS);

        for(int s = 0; s < slots.size(); ++s)
        {
            Slot slot = slots.get(s);
            if(slot.getItem().is(IgnesFatui.Items.WISP_STAFF.get()))
            {
                slots.set(s, new LockedSlot(slot));
            }
        }
    }
}
