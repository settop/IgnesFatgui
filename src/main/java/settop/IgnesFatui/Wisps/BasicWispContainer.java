package settop.IgnesFatui.Wisps;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import settop.IgnesFatui.IgnesFatui;

public class BasicWispContainer extends Container
{
    private BasicWispContents wispContents;

    public BasicWispContainer(int id, PlayerInventory playerInventory, PacketBuffer extraData)
    {
        //called on the client
        //ToDo get size from packet
        this(id, playerInventory, new BasicWispContents( extraData.readVarInt() ));
    }

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;

    private static final int SLOT_X_SPACING = 18;
    private static final int SLOT_Y_SPACING = 18;

    private static final int WISP_SLOT_XPOS = 8;
    private static final int WISP_SLOT_YPOS = 8;
    private static final int HOTBAR_XPOS = 8;
    private static final int HOTBAR_YPOS = 109;
    public static final int PLAYER_INVENTORY_XPOS = 8;
    public static final int PLAYER_INVENTORY_YPOS = 51;

    public BasicWispContainer(int id, PlayerInventory playerInventory, BasicWispContents inWispContents)
    {
        super(IgnesFatui.Containers.BASIC_WISP_CONTAINER, id);

        wispContents = inWispContents;
        PlayerInvWrapper playerInventoryForge = new PlayerInvWrapper(playerInventory);


        for(int i = 0; i < wispContents.getSizeInventory(); ++i)
        {
            //todo, make a wisp upgrade slot type
            addSlot(new Slot(wispContents, i, WISP_SLOT_XPOS + SLOT_X_SPACING * i, WISP_SLOT_YPOS));
        }

        int slotIndex = 0;
        for (int i = 0; i < HOTBAR_SLOT_COUNT; ++i)
        {
            addSlot(new SlotItemHandler(playerInventoryForge, slotIndex++, HOTBAR_XPOS + SLOT_X_SPACING * i, HOTBAR_YPOS));
        }
        for(int i = 0; i < playerInventory.mainInventory.size() - HOTBAR_SLOT_COUNT; ++i)
        {
            int row = i / PLAYER_INVENTORY_COLUMN_COUNT;
            int column = i % PLAYER_INVENTORY_COLUMN_COUNT;
            addSlot(new SlotItemHandler(playerInventoryForge, slotIndex++,  PLAYER_INVENTORY_XPOS + column * SLOT_X_SPACING, PLAYER_INVENTORY_YPOS + row * SLOT_Y_SPACING));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn)
    {
        return wispContents.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerEntity, int sourceSlotIndex)
    {
        Slot sourceSlot = inventorySlots.get(sourceSlotIndex);
        if (sourceSlot == null || !sourceSlot.getHasStack()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getStack();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if(sourceSlotIndex < wispContents.getSizeInventory())
        {
            //from the wisp inventory
            if (!mergeItemStack(sourceStack, wispContents.getSizeInventory(), inventorySlots.size(), false))
            {
                return ItemStack.EMPTY;
            }
        }
        else if(sourceSlotIndex < inventorySlots.size())
        {
            //from the player inventory
            if (!mergeItemStack(sourceStack, 0, wispContents.getSizeInventory(), false))
            {
                return ItemStack.EMPTY;
            }
        }
        else
        {
            IgnesFatui.LOGGER.warn("Invalid slotIndex:" + sourceSlotIndex);
            return ItemStack.EMPTY;
        }

        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0)
        {
            sourceSlot.putStack(ItemStack.EMPTY);
        }
        else
        {
            sourceSlot.onSlotChanged();
        }

        sourceSlot.onTake(playerEntity, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn)
    {
        //Todo - get the wisp to recalculate it's stats
        super.onContainerClosed(playerIn);
    }
}
