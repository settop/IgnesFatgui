package settop.IgnesFatui.Menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.BlockEntities.WispNodeBlockEntity;
import settop.IgnesFatui.Capabilities.ExternalWispNodeCapabilityProvider;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Utils.ItemStackContainer;

public class ExternalWispNodeMenu extends WispContainerMenuBase
{
    private static class CapabilityContainer extends SimpleContainer
    {
        BlockEntity blockEntity;
        ExternalWispNodeCapabilityProvider.Cap capability;

        CapabilityContainer(BlockEntity blockEntity)
        {
            super(1);
            this.blockEntity = blockEntity;
            var capOpt = blockEntity.getCapability(IgnesFatui.Capabilities.EXTERNAL_WISP_NODE_HANDLER).resolve();

            if(capOpt.isEmpty())
            {
                return;
            }
            if(blockEntity.getLevel() == null)
            {
                return;
            }
            capability = capOpt.get();
            setItem(0, capability.GetItem(blockEntity.getLevel().registryAccess()));
        }

        @Override
        public boolean stillValid(@NotNull Player player)
        {
            return Container.stillValidBlockEntity(blockEntity, player);
        }

        @Override
        public void setChanged()
        {
            super.setChanged();
            capability.SetItem(blockEntity.getLevel().registryAccess(), getItem(0));
        }

        @Override
        public int getMaxStackSize()
        {
            return 1;
        }

        @Override
        public boolean canPlaceItem(int slot, @NotNull ItemStack itemStack)
        {
            return capability.CanSetItem(itemStack);
        }
    }

    public static ExternalWispNodeMenu CreateMenuServer(int id, Inventory inventory, BlockEntity blockEntity)
    {
        return new ExternalWispNodeMenu(id, inventory, new CapabilityContainer(blockEntity));
    }

    public static ExternalWispNodeMenu CreateMenuClient(int id, Inventory inventory)
    {
        return new ExternalWispNodeMenu(id, inventory, new SimpleContainer(1));
    }

    private final Container container;

    private ExternalWispNodeMenu(int id, Inventory inventory, Container container)
    {
        super(IgnesFatui.ContainerMenus.EXTERNAL_WISP_NODE_MENU.get(), id);
        this.container = container;

        int slot_end_x = SLOT_START_X + SLOT_SIZE * 9;
        int x = (slot_end_x - SLOT_START_X) / 2 - SLOT_SIZE / 2;
        addSlot(new Slot(container, 0, x, SLOT_SIZE)
        {
            @Override
            public boolean mayPlace(@NotNull ItemStack itemStack)
            {
                return ExternalWispNodeCapabilityProvider.IsValidItem(itemStack);
            }

            @Override
            public int getMaxStackSize()
            {
                return 1;
            }
        });

        SetPlayerInventorySlots(inventory);
    }

    @Override
    public boolean stillValid(@NotNull Player player)
    {
        return container.stillValid(player);
    }
}
