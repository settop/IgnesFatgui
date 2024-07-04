package settop.IgnesFatui.Items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.BlockEntities.WispNodeBlockEntity;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.WispNetwork.WispDataCache;
import settop.IgnesFatui.WispNetwork.WispNode;

public class WispStaff extends Item
{
    public WispStaff(Item.Properties properties)
    {
        super(properties.component(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
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
            if(boundPos == null)
            {
                //clear teh bound position
                staff.remove(IgnesFatui.DataComponents.BOUND_GLOBAL_POS.get());
            }
            else
            {
                //open the internal inventory
            }

            return InteractionResultHolder.success(staff);
        }

        if(boundPos != null && boundPos.dimension().equals(level.dimension()) && boundPos.pos().equals(lookingAt.getBlockPos()))
        {
            //this position is already bound
            return InteractionResultHolder.fail(staff);
        }

        BlockEntity selectedBlockEntity = level.getBlockEntity(lookingAt.getBlockPos());
        if(selectedBlockEntity == null)
        {
            return InteractionResultHolder.fail(staff);
        }

        if(boundPos == null)
        {
            staff.set(IgnesFatui.DataComponents.BOUND_GLOBAL_POS.get(), new GlobalPos(level.dimension(), lookingAt.getBlockPos()));
            return InteractionResultHolder.success(staff);
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
        if(!BindBlockEntities(level, boundBLockEntity, selectedBlockEntity))
        {
            return InteractionResultHolder.fail(staff);
        }

            return InteractionResultHolder.success(staff);
    }

    private boolean BindBlockEntities(@NotNull Level level, @NotNull BlockEntity blockEntity0, @NotNull BlockEntity blockEntity1)
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

            WispDataCache cache = WispDataCache.GetCache(level);
            WispNode blockWispNode = cache.GetWispNode(level.dimension(), blockEntity.getBlockPos());
            if(blockWispNode != null)
            {
                //the block already has a node, it can only have one
                return false;
            }
            blockWispNode = cache.GetOrCreateWispNode(level.dimension(), blockEntity.getBlockPos());
            blockWispNode.LinkToBlockEntity(blockEntity);

            if(!wispNode.GetWispNode().TryConnectToNode(blockWispNode))
            {
                cache.RemoveWispNode(blockWispNode);
                return false;
            }

            return true;
        }
    }


}
