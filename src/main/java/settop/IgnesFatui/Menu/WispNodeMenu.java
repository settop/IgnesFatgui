package settop.IgnesFatui.Menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.network.IContainerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import settop.IgnesFatui.BlockEntities.WispNodeBlockEntity;
import settop.IgnesFatui.IgnesFatui;

public class WispNodeMenu extends AbstractContainerMenu
{
    private interface CannotConnectReasonGetter
    {
        int GetReasonForSlot(int slot);
    }

    public static void WriteToBuffer(FriendlyByteBuf buffer, int nodeInvSize)
    {
        buffer.writeShort(nodeInvSize);
    }

    public static WispNodeMenu CreateMenuServer(int containerID, Inventory playerInventory, WispNodeBlockEntity.NodeInventory nodeInventory)
    {
        return new WispNodeMenu(containerID, playerInventory, nodeInventory, nodeInventory::GetCannotConnectReason);
    }

    public static WispNodeMenu CreateMenuClient(int containerID, Inventory playerInventory, FriendlyByteBuf buffer)
    {
        int nodeInvSize = buffer.readShort();
        Container dummyNodeContainer = new SimpleContainer(nodeInvSize);
        return new WispNodeMenu(containerID, playerInventory, dummyNodeContainer, null);
    }

    private final Container nodeContents;
    private final CannotConnectReasonGetter cannotConnectReasonGetter;
    private final int[] cannotConnectReasons;
    private final int playerInvSlotStart;

    private WispNodeMenu(int containerID, Inventory playerInventory, Container nodeInventory, CannotConnectReasonGetter cannotConnectReasonGetter)
    {
        super(IgnesFatui.ContainerMenus.WISP_NODE_MENU.get(), containerID);
        nodeContents = nodeInventory;
        this.cannotConnectReasonGetter = cannotConnectReasonGetter;

        int slotX = 8;
        int slotSize = 18;
        cannotConnectReasons = new int[nodeContents.getContainerSize()];
        for(int slot = 0; slot < nodeContents.getContainerSize(); ++slot)
        {
            addSlot(new Slot(nodeInventory, slot, slotX + slot * slotSize, slotSize)
            {
                @Override
                public boolean mayPlace(@NotNull ItemStack itemStack)
                {
                    return WispNodeBlockEntity.NodeInventory.CanPlaceItem(itemStack);
                }

                @Override
                public int getMaxStackSize()
                {
                    return 1;
                }

                @Override
                public void setChanged()
                {
                    super.setChanged();
                    WispNodeMenu.this.SlotUpdated(index);
                }
            });

            int initialReason = cannotConnectReasonGetter != null ? cannotConnectReasonGetter.GetReasonForSlot(slot) : 0;
            DataSlot cannontConnectDataSlot = DataSlot.shared(cannotConnectReasons, slot);
            cannontConnectDataSlot.set(initialReason);
            addDataSlot(cannontConnectDataSlot);
        }
        playerInvSlotStart = slots.size();

        int yOffset = 103 + (1 - 4) * 18;
        for(int row = 0; row < 3; ++row)
        {
            for(int x = 0; x < 9; ++x)
            {
                addSlot(new Slot(playerInventory, row * 9 + x + 9, slotX + x * slotSize, row * slotSize + yOffset));
            }
        }

        //hotbar
        yOffset = 161 + (1 - 4) * 18;
        for(int x = 0; x < 9; ++x)
        {
            this.addSlot(new Slot(playerInventory, x, slotX + x * slotSize, yOffset));
        }
    }

    private void SlotUpdated(int slot)
    {
        if(slot < playerInvSlotStart && cannotConnectReasonGetter != null)
        {
            setData(slot, cannotConnectReasonGetter.GetReasonForSlot(slot));
        }
    }

    public int GetCannotConnectReasonForSlot(int slot)
    {
        if(slot >= 0 && slot < cannotConnectReasons.length)
        {
            return cannotConnectReasons[slot];
        }
        else
        {
            return 0;
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
                // If the raw stack has completely moved out of the slot, set the slot to the empty stack
                quickMovedSlot.set(ItemStack.EMPTY);
            }
            else
            {
                // Otherwise, notify the slot that the stack count has changed
                quickMovedSlot.setChanged();
            }

            if (rawStack.getCount() == quickMovedStack.getCount())
            {
                // If the raw stack was not able to be moved to another slot, no longer quick move
                return ItemStack.EMPTY;
            }
            quickMovedSlot.onTake(player, rawStack);
        }

        return quickMovedStack; // Return the slot stack
    }

    @Override
    public boolean stillValid(@NotNull Player player)
    {
        return nodeContents.stillValid(player);
    }
}
