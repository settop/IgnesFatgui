package settop.IgnesFatui.Wisps.Enhancements;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import settop.IgnesFatui.GUI.SubContainers.SubContainer;

public interface IEnhancement
{
    interface IFactory
    {
        IEnhancement Create();
        SubContainer CreateSubContainer(int xPos, int yPos);
    }

    CompoundNBT SerializeNBT();
    void DeserializeNBT(CompoundNBT nbt);
    EnhancementTypes GetType();
}
