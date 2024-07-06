package settop.IgnesFatui.Menu;

import com.google.common.collect.Lists;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import settop.IgnesFatui.Utils.StringReferenceHolder;

import java.lang.ref.WeakReference;
import java.util.List;

public abstract class WispContainerSubMenuBase
{
    private WeakReference<WispContainerMenuBase> parentContainerMenu;
    private int subWindowId = -1;
    protected boolean isActive = true;

    private int xPos;
    private int yPos;

    public final List<Slot> inventorySlots = Lists.newArrayList();
    public final List<DataSlot> trackedIntReferences = Lists.newArrayList();
    public final List<StringReferenceHolder> trackedStringReferences = Lists.newArrayList();

    protected WispContainerSubMenuBase(int xPos, int yPos)
    {
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public void SetParent(WeakReference<WispContainerMenuBase> parentContainer, int subWindowId)
    {
        this.parentContainerMenu = parentContainer;
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

    public WispContainerMenuBase GetParentContainerMenu() { return parentContainerMenu.get(); }
    public int GetSubWindowID() { return subWindowId; }

    protected DataSlot trackInt(DataSlot intIn)
    {
        trackedIntReferences.add(intIn);
        return intIn;
    }

    protected void trackIntArray(ContainerData arrayIn)
    {
        for(int i = 0; i < arrayIn.getCount(); ++i)
        {
            this.trackInt(DataSlot.forContainer(arrayIn, i));
        }
    }

    protected StringReferenceHolder trackStr(StringReferenceHolder strIn)
    {
        trackedStringReferences.add(strIn);
        return strIn;
    }

    public void HandlePropertyUpdate(int propertyId, int value)
    {
    }

    public void HandleStringPropertyUpdate(int propertyId, String value)
    {
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


    //@OnlyIn(Dist.CLIENT)
    //abstract public SubScreen CreateScreen(MultiScreen<?> parentScreen);
}
