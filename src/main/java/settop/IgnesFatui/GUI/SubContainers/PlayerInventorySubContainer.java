package settop.IgnesFatui.GUI.SubContainers;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import settop.IgnesFatui.Client.Client;
import settop.IgnesFatui.Client.Screens.SubScreens.SubScreen;
import settop.IgnesFatui.GUI.ActivatableSlotItemHandler;

public class PlayerInventorySubContainer extends SubContainer
{
    public static final int HOTBAR_YOFFSET = 4;

    public PlayerInventorySubContainer(PlayerInventory playerInventory, int xPos, int yPos)
    {
        super(xPos, yPos);
        PlayerInvWrapper playerInventoryForge = new PlayerInvWrapper(playerInventory);

        int numRows = (playerInventory.mainInventory.size() - Client.PLAYER_INVENTORY_COLUMN_COUNT) / Client.PLAYER_INVENTORY_COLUMN_COUNT;

        int slotIndex = 0;
        for (int i = 0; i < Client.PLAYER_INVENTORY_COLUMN_COUNT; ++i)
        {
            this.inventorySlots.add(new ActivatableSlotItemHandler(playerInventoryForge, slotIndex++, xPos + Client.SLOT_X_SPACING * i, yPos + HOTBAR_YOFFSET + Client.SLOT_Y_SPACING * numRows));
        }
        for(int i = 0; i < playerInventory.mainInventory.size() - Client.PLAYER_INVENTORY_COLUMN_COUNT; ++i)
        {
            int row = i / Client.PLAYER_INVENTORY_COLUMN_COUNT;
            int column = i % Client.PLAYER_INVENTORY_COLUMN_COUNT;
            this.inventorySlots.add(new ActivatableSlotItemHandler(playerInventoryForge, slotIndex++,  xPos + column * Client.SLOT_X_SPACING, yPos + row * Client.SLOT_Y_SPACING));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public SubScreen CreateScreen()
    {
        return null;
    }
}
