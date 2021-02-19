package settop.IgnesFatui.GUI;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IntReferenceHolder;
import net.minecraftforge.fml.network.PacketDistributor;
import settop.IgnesFatui.GUI.Network.Packets.SWindowStringPropertyPacket;
import settop.IgnesFatui.GUI.SubContainers.SubContainer;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Utils.StringReferenceHolder;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MultiScreenContainer extends Container
{
    private final List<SubContainer> subContainers = new ArrayList<>();
    private final List<StringReferenceHolder> trackedStringReferences = Lists.newArrayList();
    private ServerPlayerEntity player;

    protected MultiScreenContainer(@Nullable ContainerType<?> type, int id)
    {
        super(type, id);
    }

    protected void SetSubContainers(List<SubContainer> subContainers)
    {
        for(SubContainer subContainer : subContainers)
        {
            AddSubContainer(subContainer);
        }
    }

    private void AddSubContainer(SubContainer container)
    {
        container.SetParent( new WeakReference(this), subContainers.size() );

        for(Slot subSlot : container.inventorySlots)
        {
            addSlot(subSlot);
        }
        for(IntReferenceHolder intRef : container.trackedIntReferences)
        {
            trackInt(intRef);
        }
        for(StringReferenceHolder strRef : container.trackedStringReferences)
        {
            trackString(strRef);
        }

        subContainers.add(container);
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
    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);
        if(listener instanceof ServerPlayerEntity)
        {
            player = (ServerPlayerEntity)listener;
        }
    }

    @Override
    public void removeListener(IContainerListener listener)
    {
        super.removeListener(listener);
        if(player == listener)
        {
            player = null;
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn)
    {
        return true;
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        for(int i = 0; i < trackedStringReferences.size(); ++i)
        {
            StringReferenceHolder strRef = trackedStringReferences.get(i);
            if(strRef.isDirty() && player != null)
            {
                strRef.clearDirty();
                IgnesFatui.MULTI_SCREEN_CHANNEL.send(PacketDistributor.PLAYER.with(()->player), new SWindowStringPropertyPacket( windowId, i, strRef.get() ));
            }
        }
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn)
    {
        for(SubContainer subContainer : subContainers)
        {
            subContainer.OnClose();
        }
        super.onContainerClosed(playerIn);
    }

    public List<SubContainer> GetSubContainers() { return subContainers; }

    @Override
    public boolean canDragIntoSlot(Slot slotIn)
    {
        return !(slotIn instanceof FakeSlot);
    }

    private ItemStack FakeSlotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player)
    {
        ItemStack heldItem = player.inventory.getItemStack();
        FakeSlot clickedSlot = (FakeSlot)inventorySlots.get(slotId);
        switch (clickTypeIn)
        {
            case PICKUP:
            {
                if(clickedSlot.getStack().getItem() == heldItem.getItem())
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

                    int newCount = clickedSlot.getStack().getCount() + countChange;
                    if(newCount <= 0)
                    {
                        clickedSlot.putStack(ItemStack.EMPTY);
                    }
                    else
                    {
                        clickedSlot.getStack().setCount(Math.min(clickedSlot.getSlotStackLimit(), newCount));
                    }
                }
                else if(heldItem.isEmpty())
                {
                    if(!clickedSlot.getStack().isEmpty())
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

                        int newCount = clickedSlot.getStack().getCount() + countChange;
                        if(newCount <= 0)
                        {
                            clickedSlot.putStack(ItemStack.EMPTY);
                        }
                        else
                        {
                            clickedSlot.getStack().setCount(Math.min(clickedSlot.getSlotStackLimit(), newCount));
                        }
                    }
                }
                else
                {
                    if(dragType == 0)
                    {
                        ItemStack stack = heldItem.copy();
                        stack.setCount(Math.min(stack.getCount(), clickedSlot.getSlotStackLimit()));
                        clickedSlot.putStack(stack);
                    }
                }
                return ItemStack.EMPTY;
            }
            case QUICK_MOVE:
            {
                int newCount = 0;
                if(dragType == 0)
                {
                    newCount = clickedSlot.getStack().getCount() * 2;
                }
                else if(dragType == 1)
                {
                    newCount = clickedSlot.getStack().getCount() / 2;
                }

                if(newCount <= 0)
                {
                    clickedSlot.putStack(ItemStack.EMPTY);
                }
                else
                {
                    clickedSlot.getStack().setCount(Math.min(clickedSlot.getSlotStackLimit(), newCount));
                }
                return ItemStack.EMPTY;
            }
            case CLONE:
            {
                clickedSlot.putStack(ItemStack.EMPTY);
                return ItemStack.EMPTY;
            }
            default:
                return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player)
    {
        Slot clickedSlot = slotId >= 0 ? inventorySlots.get(slotId) : null;
        if (clickedSlot != null && clickedSlot instanceof FakeSlot)
        {
            return FakeSlotClick(slotId, dragType, clickTypeIn, player);
        }
        else
        {
            return super.slotClick(slotId, dragType, clickTypeIn, player);
        }
    }

    public boolean mouseScrolled(int slotID, double mouseX, double mouseY, double delta)
    {
        Slot hoveredSlot = slotID >= 0 ? inventorySlots.get(slotID) : null;
        if(hoveredSlot instanceof FakeSlot)
        {
            ItemStack stack = hoveredSlot.getStack();

            int newCount = stack.getCount() + (int)delta;
            if(newCount <= 0)
            {
                hoveredSlot.putStack(ItemStack.EMPTY);
            }
            else
            {
                hoveredSlot.getStack().setCount(Math.min(hoveredSlot.getSlotStackLimit(), newCount));
            }

            return true;
        }
        return false;
    }
}
