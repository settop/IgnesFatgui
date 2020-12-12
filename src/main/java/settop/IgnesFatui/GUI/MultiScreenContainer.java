package settop.IgnesFatui.GUI;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.Tuple;
import settop.IgnesFatui.GUI.SubContainers.SubContainer;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MultiScreenContainer extends Container
{
    public static final int INT_ID_GAP = 100;
    private List<SubContainer> subContainers = new ArrayList<>();

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

        subContainers.add(container);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn)
    {
        return true;
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

}
