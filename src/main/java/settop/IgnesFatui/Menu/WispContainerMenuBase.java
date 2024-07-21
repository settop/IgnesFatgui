package settop.IgnesFatui.Menu;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Network.PacketHandler;
import settop.IgnesFatui.Utils.StringReferenceHolder;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class WispContainerMenuBase extends AbstractContainerMenu
{
    private final List<WispContainerSubMenuBase> subMenuContainers = new ArrayList<>();
    private final List<StringReferenceHolder> trackedStringReferences = Lists.newArrayList();
    private ServerPlayer player;
    private int playerInvSlotStart = 0;

    public final int SLOT_START_X = 8;
    public final int SLOT_SIZE = 18;
    public final int PLAYER_SLOT_START_Y = 103 + 2 * SLOT_SIZE;
    public final int PLAYER_HOTBAR_SLOT_START_Y = 161 + 2 * SLOT_SIZE;

    protected WispContainerMenuBase(@Nullable MenuType<?> type, int id)
    {
        super(type, id);
    }

    protected void SetPlayerInventorySlots(Inventory playerInventory)
    {
        player = (ServerPlayer)playerInventory.player;
        playerInvSlotStart = slots.size();

        for(int row = 0; row < 3; ++row)
        {
            for(int x = 0; x < 9; ++x)
            {
                addSlot(new Slot(playerInventory, row * 9 + x + 9, SLOT_START_X + x * SLOT_SIZE, row * SLOT_SIZE + PLAYER_SLOT_START_Y));
            }
        }

        //hotbar
        for(int x = 0; x < 9; ++x)
        {
            this.addSlot(new Slot(playerInventory, x, SLOT_START_X + x * SLOT_SIZE, PLAYER_HOTBAR_SLOT_START_Y));
        }
    }

    protected void SetSubMenuContainers(List<WispContainerSubMenuBase> subMenuContainers)
    {
        for(WispContainerSubMenuBase subMenuContainer : subMenuContainers)
        {
            AddSubMenuContainer(subMenuContainer);
        }
    }

    private void AddSubMenuContainer(WispContainerSubMenuBase subMenuContainer)
    {
        subMenuContainer.SetParent( new WeakReference<>(this), subMenuContainers.size() );

        for(Slot subSlot : subMenuContainer.inventorySlots)
        {
            addSlot(subSlot);
        }
        for(DataSlot data : subMenuContainer.trackedIntReferences)
        {
            addDataSlot(data);
        }
        for(StringReferenceHolder strRef : subMenuContainer.trackedStringReferences)
        {
            trackString(strRef);
        }

        subMenuContainers.add(subMenuContainer);
    }

    protected StringReferenceHolder trackString(StringReferenceHolder strIn)
    {
        this.trackedStringReferences.add(strIn);
        return strIn;
    }

    public void updateTrackedString(int id, String value)
    {
        if(id >= 0 && id < trackedStringReferences.size())
        {
            trackedStringReferences.get(id).set(value);
        }
        else
        {
            IgnesFatui.LOGGER.warn("Invalid id for tracked string update");
        }
    }

    @Override
    public boolean stillValid(@NotNull Player playerIn)
    {
        return true;
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();

        for(int i = 0; i < trackedStringReferences.size(); ++i)
        {
            StringReferenceHolder strRef = trackedStringReferences.get(i);
            if(strRef.isDirty() && player != null)
            {
                strRef.clearDirty();
                PacketHandler.SendStringUpdate(player, containerId, i, strRef.get());
            }
        }
    }

    @Override
    public void removed(@NotNull Player playerIn)
    {
        for(WispContainerSubMenuBase subMenuContainer : subMenuContainers)
        {
            subMenuContainer.OnClose();
        }
        super.removed(playerIn);
    }

    public List<WispContainerSubMenuBase> GetSubMenuContainers() { return subMenuContainers; }

    @Override
    public boolean canDragTo(@NotNull Slot slotIn)
    {
        return !(slotIn instanceof FakeSlot);
    }

    private void FakeSlotClick(int slotId, int dragType, ClickType clickTypeIn, Player player)
    {
        ItemStack heldItem = this.getCarried();
        FakeSlot clickedSlot = (FakeSlot)slots.get(slotId);
        switch (clickTypeIn)
        {
            case PICKUP:
            {
                if(ItemStack.isSameItemSameComponents(clickedSlot.getItem(), heldItem))
                {
                    int countChange = 0;
                    if(dragType == 0)
                    {
                        //left click
                        countChange = heldItem.getCount();
                    }
                    else if(dragType == 1)
                    {
                        //right click
                        countChange = -heldItem.getCount();
                    }

                    int newCount = clickedSlot.getItem().getCount() + countChange;
                    if(newCount <= 0)
                    {
                        clickedSlot.set(ItemStack.EMPTY);
                    }
                    else
                    {
                        clickedSlot.getItem().setCount(Math.min(clickedSlot.getMaxStackSize(), newCount));
                    }
                }
                else if(heldItem.isEmpty())
                {
                    if(!clickedSlot.getItem().isEmpty())
                    {
                        int countChange = 0;
                        if(dragType == 0)
                        {
                            //left click
                            countChange = 1;
                        }
                        else if(dragType == 1)
                        {
                            //right click
                            countChange = -1;
                        }

                        int newCount = clickedSlot.getItem().getCount() + countChange;
                        if(newCount <= 0)
                        {
                            clickedSlot.set(ItemStack.EMPTY);
                        }
                        else
                        {
                            clickedSlot.getItem().setCount(Math.min(clickedSlot.getMaxStackSize(), newCount));
                        }
                    }
                }
                else
                {
                    if(dragType == 0)
                    {
                        ItemStack stack = heldItem.copy();
                        stack.setCount(Math.min(stack.getCount(), clickedSlot.getMaxStackSize()));
                        clickedSlot.set(stack);
                    }
                }
                break;
            }
            case QUICK_MOVE:
            {
                int newCount = 0;
                if(dragType == 0)
                {
                    newCount = clickedSlot.getItem().getCount() * 2;
                }
                else if(dragType == 1)
                {
                    newCount = clickedSlot.getItem().getCount() / 2;
                }

                if(newCount <= 0)
                {
                    clickedSlot.set(ItemStack.EMPTY);
                }
                else
                {
                    clickedSlot.getItem().setCount(Math.min(clickedSlot.getMaxStackSize(), newCount));
                }
                break;
            }
            case CLONE:
            {
                clickedSlot.set(ItemStack.EMPTY);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void clicked(int slotId, int dragType, @NotNull ClickType clickTypeIn, @NotNull Player player)
    {
        Slot clickedSlot = slotId >= 0 ? slots.get(slotId) : null;
        if (clickedSlot instanceof FakeSlot)
        {
            FakeSlotClick(slotId, dragType, clickTypeIn, player);
        }
        else
        {
            super.clicked(slotId, dragType, clickTypeIn, player);
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int sourceSlot)
    {
        ItemStack quickMovedStack = ItemStack.EMPTY;
        Slot quickMovedSlot = slots.get(sourceSlot);

        if (quickMovedSlot != null && quickMovedSlot.hasItem())
        {
            ItemStack rawStack = quickMovedSlot.getItem();
            quickMovedStack = rawStack.copy();

            // If the quick move was performed on the data inventory result slot
            if (sourceSlot < playerInvSlotStart)
            {
                // Try to move the result slot into the player inventory/hotbar
                if (!moveItemStackTo(rawStack, playerInvSlotStart, slots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else
            {
                // Try to move the inventory/hotbar slot into the data inventory input slots
                if (!moveItemStackTo(rawStack, 0, playerInvSlotStart, false))
                {
                    return ItemStack.EMPTY;
                }
            }

            if (rawStack.isEmpty())
            {
                // If the raw stackKey has completely moved out of the slot, set the slot to the empty stackKey
                quickMovedSlot.set(ItemStack.EMPTY);
            }
            else
            {
                // Otherwise, notify the slot that the stackKey count has changed
                quickMovedSlot.setChanged();
            }

            if (rawStack.getCount() == quickMovedStack.getCount())
            {
                // If the raw stackKey was not able to be moved to another slot, no longer quick move
                return ItemStack.EMPTY;
            }
            quickMovedSlot.onTake(player, rawStack);
        }

        return quickMovedStack; // Return the slot stackKey
    }

    public boolean mouseScrolled(int slotID, double mouseX, double mouseY, double delta)
    {
        Slot hoveredSlot = slotID >= 0 ? slots.get(slotID) : null;
        if(hoveredSlot instanceof FakeSlot)
        {
            ItemStack stack = hoveredSlot.getItem();

            int newCount = stack.getCount() + (int)delta;
            if(newCount <= 0)
            {
                hoveredSlot.set(ItemStack.EMPTY);
            }
            else
            {
                hoveredSlot.getItem().setCount(Math.min(hoveredSlot.getMaxStackSize(), newCount));
            }

            return true;
        }
        return false;
    }
}
