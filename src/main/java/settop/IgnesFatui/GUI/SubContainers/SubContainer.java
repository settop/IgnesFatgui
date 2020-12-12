package settop.IgnesFatui.GUI.SubContainers;

import com.google.common.collect.Lists;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import settop.IgnesFatui.Client.Screens.SubScreens.SubScreen;
import settop.IgnesFatui.GUI.IActivatableSlot;
import settop.IgnesFatui.GUI.MultiScreenContainer;

import java.lang.ref.WeakReference;
import java.util.List;

public abstract class SubContainer
{
    private WeakReference<MultiScreenContainer> parentContainer;
    private int subWindowId = -1;
    private boolean isActive = true;

    private int xPos;
    private int yPos;

    public final List<Slot> inventorySlots = Lists.newArrayList();
    public final List<IntReferenceHolder> trackedIntReferences = Lists.newArrayList();

    protected SubContainer(int xPos, int yPos)
    {
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public void SetParent(WeakReference<MultiScreenContainer> parentContainer, int subWindowId)
    {
        this.parentContainer = parentContainer;
        this.subWindowId = subWindowId;
    }

    public void SetActive(boolean active)
    {
        isActive = active;
        for(Slot slot : inventorySlots)
        {
            if(slot instanceof IActivatableSlot)
            {
                ((IActivatableSlot)slot).SetActive(active);
            }
        }
    }

    public MultiScreenContainer GetParentContainer() { return parentContainer.get(); }
    public int GetSubWindowID() { return subWindowId; }

    protected IntReferenceHolder trackInt(IntReferenceHolder intIn)
    {
        trackedIntReferences.add(intIn);
        return intIn;
    }

    protected void trackIntArray(IIntArray arrayIn)
    {
        for(int i = 0; i < arrayIn.size(); ++i)
        {
            this.trackInt(IntReferenceHolder.create(arrayIn, i));
        }
    }

    public void OnClose()
    {
    }

    public boolean IsActive()
    {
        return isActive;
    }

    public int GetXPos(){ return xPos; }
    public int GetYPos(){ return yPos; }


    @OnlyIn(Dist.CLIENT)
    abstract public SubScreen CreateScreen();
}
