package settop.IgnesFatui.Menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Items.WispStaff;
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

        //assume the first slots are for the staff inventory
        for(int s = 0; s < NUM_SLOTS; ++s)
        {
            Slot slot = slots.get(s);
            slots.set(s, new FilteredSlot(slot, WispStaff::AcceptsItemInStorage));
        }

        //the remaining slots are for teh players inventory
        for(int s = NUM_SLOTS; s < slots.size(); ++s)
        {
            Slot slot = slots.get(s);
            if(slot.getItem().is(IgnesFatui.Items.WISP_STAFF.get()))
            {
                slots.set(s, new LockedSlot(slot));
            }
        }
    }
}
