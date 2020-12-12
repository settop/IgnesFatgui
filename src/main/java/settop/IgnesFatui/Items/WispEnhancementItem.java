package settop.IgnesFatui.Items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Wisps.Enhancements.EnhancementTypes;
import settop.IgnesFatui.Wisps.Enhancements.IEnhancement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WispEnhancementItem extends Item
{
    public static class CapabilityProviderEnhancementStorage implements Capability.IStorage<IEnhancement>
    {

        @Nullable
        @Override
        public INBT writeNBT(Capability<IEnhancement> capability, IEnhancement instance, Direction side)
        {
            return instance.SerializeNBT();
        }

        @Override
        public void readNBT(Capability<IEnhancement> capability, IEnhancement instance, Direction side, INBT nbt)
        {
            instance.DeserializeNBT((CompoundNBT)nbt);
        }
    }

    public class CapabilityProviderEnhancement implements ICapabilitySerializable<CompoundNBT>
    {
        public CapabilityProviderEnhancement()
        {
            enhancement = type.GetFactory().Create();
        }

        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
        {
            return IgnesFatui.Capabilities.CAPABILITY_ENHANCEMENT.orEmpty(capability, LazyOptional.of(()->enhancement));
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            return enhancement.SerializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt)
        {
            enhancement.DeserializeNBT(nbt);
        }

        private IEnhancement enhancement;
    }

    private final EnhancementTypes type;

    public WispEnhancementItem(EnhancementTypes enhancementType)
    {
        super(new Item.Properties().maxStackSize(64).group(ItemGroup.MISC));
        type = enhancementType;
    }

    public EnhancementTypes GetType() { return type; }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt)
    {
        return new CapabilityProviderEnhancement();
    }
}

