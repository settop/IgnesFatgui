package settop.IgnesFatui.Items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;
import settop.IgnesFatui.Wisps.ChunkWispData;
import settop.IgnesFatui.Wisps.WispBase;
import settop.IgnesFatui.Wisps.WispConstants;

import javax.annotation.Nullable;
import java.util.List;

import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

public class BasicWispItem extends Item
{
    public BasicWispItem()
    {
        super(new Item.Properties().maxStackSize(64).group(ItemGroup.MISC));
    }

    // adds 'tooltip' text
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(new StringTextComponent("Sneak right click on a block to insert wisp"));
        tooltip.add(new StringTextComponent("Can be enchanted"));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        World world = context.getWorld();
        if(!context.getPlayer().isSneaking())
        {
            return ActionResultType.PASS;
        }
        TileEntity tileEntity = world.getTileEntity( context.getPos() );
        if(tileEntity == null)
        {
            return ActionResultType.PASS;
        }
        LazyOptional<IItemHandler> itemHandler = tileEntity.getCapability(ITEM_HANDLER_CAPABILITY);
        LazyOptional<IFluidHandler> fluidHandler = tileEntity.getCapability(FLUID_HANDLER_CAPABILITY);
        if(itemHandler.isPresent() || fluidHandler.isPresent())
        {
            if(!world.isRemote())
            {
                ItemStack wispItemStack = context.getItem();
                //server side only work
                Tuple<WispBase, Boolean> blocksWisp = ChunkWispData.GetOrCreateWisp(WispConstants.BASIC_WISP, world, context.getPos(), wispItemStack.getTag());
                if(blocksWisp.getB())
                {
                    //we just added it, so remove one from the stack
                    wispItemStack.shrink(1);
                }
                PlayerEntity player = context.getPlayer();
                if (!(player instanceof ServerPlayerEntity))
                    return ActionResultType.FAIL;  // should always be true, but just in case...
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
                NetworkHooks.openGui(serverPlayerEntity, blocksWisp.getA(),  (packetBuffer) -> blocksWisp.getA().ContainerExtraDataWriter(packetBuffer));
            }
            return ActionResultType.CONSUME;
        }
        else
        {
            return ActionResultType.PASS;
        }
    }
}
