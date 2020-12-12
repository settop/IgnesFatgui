package settop.IgnesFatui.Wisps.Enhancements;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import settop.IgnesFatui.GUI.SubContainers.ProviderEnhancementSubContainer;
import settop.IgnesFatui.GUI.SubContainers.SubContainer;
import settop.IgnesFatui.Items.WispEnhancementItem;

public class ProviderEnhancement implements IEnhancement
{
    public static class Factory implements IEnhancement.IFactory
    {
        @Override
        public IEnhancement Create()
        {
            return new ProviderEnhancement();
        }

        @Override
        public SubContainer CreateSubContainer(int xPos, int yPos) { return new ProviderEnhancementSubContainer(xPos, yPos); }
    }
    public static final Factory FACTORY = new Factory();

    private boolean providedDirections[];

    @Override
    public CompoundNBT SerializeNBT()
    {
        if(providedDirections == null)
        {
            //it's the default, so no need to store extra nbt
            return new CompoundNBT();
        }
        boolean anyFalse = false;
        for(boolean b : providedDirections)
        {
            if(!b)
            {
                anyFalse = true;
                break;
            }
        }

        if(!anyFalse)
        {
            //it's on the default, don't write any NBT
            return new CompoundNBT();
        }

        CompoundNBT directionsNBT = new CompoundNBT();
        for(int i = 0; i < 6; ++i)
        {
            directionsNBT.putBoolean(Direction.byIndex(i).getString(), providedDirections[i]);
        }

        CompoundNBT nbt = new CompoundNBT();
        nbt.put("providerDirections", directionsNBT);

        return nbt;
    }

    @Override
    public void DeserializeNBT(CompoundNBT nbt)
    {
        if (nbt == null)
        {
            return;
        }
        if (!nbt.contains("providerDirections"))
        {
            return;
        }

        providedDirections = new boolean[6];
        CompoundNBT directionsNBT = nbt.getCompound("providerDirections");

        for(int i = 0; i < 6; ++i)
        {
            providedDirections[i] = directionsNBT.getBoolean(Direction.byIndex(i).getString());
        }
    }

    @Override
    public EnhancementTypes GetType()
    {
        return EnhancementTypes.PROVIDER;
    }

    public boolean IsDirectionSet(Direction dir)
    {
        return providedDirections == null || providedDirections[dir.getIndex()];
    }

    public void SetDirectionProvided(Direction dir, boolean isProvided)
    {
        if(providedDirections == null)
        {
            providedDirections = new boolean[6];
            for(int i = 0; i < 6; ++i)
            {
                providedDirections[i] = true;
            }
        }
        providedDirections[dir.getIndex()] = isProvided;
    }
}
