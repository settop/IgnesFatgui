package settop.IgnesFatui.Items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.BlockEntities.WispNodeBlockEntity;
import settop.IgnesFatui.Capabilities.ExternalWispNodeCapabilityProvider;
import settop.IgnesFatui.Client.Tooltip.ItemTooltip;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Menu.WispStaffMenuContainer;
import settop.IgnesFatui.WispNetwork.WispDataCache;

import java.util.List;
import java.util.Optional;

public class WispStaff extends Item
{
    public static boolean AcceptsItemInStorage(ItemStack item)
    {
        return item.getItem() instanceof WispStaffStorable;
    }

    public WispStaff(Item.Properties properties)
    {
        super(properties.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
    }

    @Override
    public boolean overrideStackedOnOther(@NotNull ItemStack itemStack, @NotNull Slot stackOnSlot, @NotNull ClickAction clickAction, @NotNull Player player)
    {
        return super.overrideStackedOnOther(itemStack, stackOnSlot, clickAction, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack itemStack, @NotNull ItemStack otherItemStack, @NotNull Slot itemSlot, @NotNull ClickAction clickAction, @NotNull Player player, @NotNull SlotAccess slotAccess)
    {
        return super.overrideOtherStackedOnMe(itemStack, otherItemStack, itemSlot, clickAction, player, slotAccess);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        if(super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged))
        {
            ItemContainerContents oldContents = oldStack.get(DataComponents.CONTAINER);
            ItemContainerContents newContents = newStack.get(DataComponents.CONTAINER);
            //if the contents was updated, don't do the animation
            return oldContents == null || oldContents.equals(newContents);
        }
        else
        {
            return false;
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand)
    {
        ItemStack staff = player.getItemInHand(hand);
        if(!player.isCrouching())
        {
            return InteractionResultHolder.pass(staff);
        }
        GlobalPos boundPos = staff.get(IgnesFatui.DataComponents.BOUND_GLOBAL_POS.get());

        Vec3 start = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        Vec3 look = player.getLookAngle();
        double reach = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        Vec3 end = new Vec3(start.x() + look.x * reach, start.y() + look.y * reach, start.z() + look.z * reach);

        BlockHitResult lookingAt = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, CollisionContext.empty()));
        if(lookingAt.getType() == HitResult.Type.MISS)
        {
            if(boundPos != null)
            {
                //clear the bound position
                staff.remove(IgnesFatui.DataComponents.BOUND_GLOBAL_POS.get());
            }
            else
            {
                //open the internal inventory
                if(player instanceof ServerPlayer)
                {
                    player.openMenu(new SimpleMenuProvider(
                            (id, inventory, menuPlayer)->WispStaffMenuContainer.CreateMenuServer(id, inventory, staff),
                            staff.getDisplayName()
                            ));
                }
            }

            return InteractionResultHolder.sidedSuccess(staff, level.isClientSide());
        }

        if(boundPos != null && boundPos.dimension().equals(level.dimension()) && boundPos.pos().equals(lookingAt.getBlockPos()))
        {
            //this position is already bound
            return InteractionResultHolder.sidedSuccess(staff, level.isClientSide());
        }

        BlockEntity selectedBlockEntity = level.getBlockEntity(lookingAt.getBlockPos());
        if(selectedBlockEntity == null)
        {
            return InteractionResultHolder.fail(staff);
        }

        if(boundPos == null)
        {
            staff.set(IgnesFatui.DataComponents.BOUND_GLOBAL_POS.get(), new GlobalPos(level.dimension(), lookingAt.getBlockPos()));
            return InteractionResultHolder.sidedSuccess(staff, level.isClientSide());
        }
        else if(!level.dimension().equals(boundPos.dimension()))
        {
            //can only bind within the same dimension
            return InteractionResultHolder.fail(staff);
        }

        BlockEntity boundBLockEntity = level.getBlockEntity(boundPos.pos());
        if(boundBLockEntity == null)
        {
            return InteractionResultHolder.fail(staff);
        }

        //now clear the bound pos
        staff.remove(IgnesFatui.DataComponents.BOUND_GLOBAL_POS.get());
        if(!BindBlockEntities(staff, level, boundBLockEntity, selectedBlockEntity))
        {
            return InteractionResultHolder.fail(staff);
        }

        return InteractionResultHolder.sidedSuccess(staff, level.isClientSide());
    }

    private boolean BindBlockEntities(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockEntity blockEntity0, @NotNull BlockEntity blockEntity1)
    {
        boolean entity0IsWispNode = blockEntity0 instanceof WispNodeBlockEntity;
        boolean entity1IsWispNode = blockEntity1 instanceof WispNodeBlockEntity;

        if(!entity0IsWispNode && !entity1IsWispNode)
        {
            //can't connect two block entities
            return false;
        }
        else if(entity0IsWispNode && entity1IsWispNode)
        {
            if(level.isClientSide())
            {
                return true;
            }
            else
            {
                WispNodeBlockEntity wispNode0 = (WispNodeBlockEntity)blockEntity0;
                WispNodeBlockEntity wispNode1 = (WispNodeBlockEntity)blockEntity1;
                return wispNode0.GetWispNode().TryConnectToNode(wispNode1.GetWispNode());
            }
        }
        else
        {
            WispNodeBlockEntity wispNode = (WispNodeBlockEntity)(entity0IsWispNode ? blockEntity0 : blockEntity1);
            BlockEntity blockEntity = entity0IsWispNode ? blockEntity1 : blockEntity0;

            if(level.isClientSide())
            {
                return true;
            }

            int cannotConnectReasons = wispNode.GetCannotConnectReasons(blockEntity);
            if(cannotConnectReasons != 0)
            {
                return false;
            }

            LazyOptional<ExternalWispNodeCapabilityProvider.Cap> externalNodeCapLazyOptional = blockEntity.getCapability(IgnesFatui.Capabilities.EXTERNAL_WISP_NODE_HANDLER);
            if(!externalNodeCapLazyOptional.isPresent())
            {
                return false;
            }
            WispDataCache cache = WispDataCache.GetCache(level);
            ExternalWispNodeCapabilityProvider.Cap externalNodeCap = externalNodeCapLazyOptional.resolve().get();
            if(!externalNodeCap.HasItem())
            {
                //need to insert a new external node item
                ItemContainerContents staffContents = stack.get(DataComponents.CONTAINER);
                if(staffContents == null)
                {
                    return false;
                }
                NonNullList<ItemStack> staffItemContents = NonNullList.withSize((int)staffContents.stream().count(), ItemStack.EMPTY);
                staffContents.copyInto(staffItemContents);
                //just use the first valid item
                for (ItemStack content : staffItemContents)
                {
                    if (externalNodeCap.CanSetItem(content))
                    {
                        externalNodeCap.SetItem(level.registryAccess(), content.split(1));
                        break;
                    }
                }
                if(!externalNodeCap.HasItem())
                {
                    //failed to insert an item
                    return false;
                }

                stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(staffItemContents));
                externalNodeCap.CreateNode(cache, blockEntity);
                blockEntity.setChanged();
            }

            return wispNode.GetWispNode().TryConnectToNode(externalNodeCap.GetNode());
        }
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack itemStack)
    {
        if(itemStack.has(DataComponents.HIDE_TOOLTIP) || itemStack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP))
        {
            return Optional.empty();
        }
        GlobalPos boundPos = itemStack.get(IgnesFatui.DataComponents.BOUND_GLOBAL_POS.get());
        if(boundPos == null)
        {
            return Optional.empty();
        }
        ClientLevel clientLevel = Minecraft.getInstance().level;
        if(clientLevel == null || !clientLevel.dimension().equals(boundPos.dimension()))
        {
            return Optional.empty();
        }
        return Optional.of(new ItemTooltip(clientLevel.getBlockState(boundPos.pos()).getBlock().asItem().getDefaultInstance()));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull Item.TooltipContext tooltipContext, @NotNull List<Component> hoverText, @NotNull TooltipFlag tooltipFlags)
    {
        GlobalPos boundPos = itemStack.get(IgnesFatui.DataComponents.BOUND_GLOBAL_POS.get());
        if(boundPos != null)
        {
            hoverText.add(Component.translatable("item.sif1.bound_pos", boundPos.pos().getX(), boundPos.pos().getY(), boundPos.pos().getZ()));
        }
    }

}
