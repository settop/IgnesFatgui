package settop.IgnesFatui.Items;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;
import settop.IgnesFatui.BlockEntities.WispNodeBlockEntity;
import settop.IgnesFatui.Capabilities.ExternalWispNodeCapabilityProvider;
import settop.IgnesFatui.Client.Tooltip.ItemTooltip;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Menu.ExternalWispNodeMenu;
import settop.IgnesFatui.Menu.WispStaffMenuContainer;

import java.util.List;
import java.util.Optional;

public class WispExternalNodeItem extends Item implements WispStaffStorable
{
    public WispExternalNodeItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext useOnContext)
    {
        return super.useOn(useOnContext);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand)
    {
        ItemStack nodeItem = player.getItemInHand(hand);
        if(!player.isCrouching())
        {
            return InteractionResultHolder.pass(nodeItem);
        }

        Vec3 start = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        Vec3 look = player.getLookAngle();
        double reach = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        Vec3 end = new Vec3(start.x() + look.x * reach, start.y() + look.y * reach, start.z() + look.z * reach);

        BlockHitResult lookingAt = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, CollisionContext.empty()));
        if(lookingAt.getType() == HitResult.Type.MISS)
        {
            //open the internal inventory
            if(player instanceof ServerPlayer)
            {
                /*player.openMenu(new SimpleMenuProvider(
                        (id, inventory, menuPlayer)-> WispStaffMenuContainer.CreateMenuServer(id, inventory, staff),
                        staff.getDisplayName()
                        ));
                 */
            }

            return InteractionResultHolder.sidedSuccess(nodeItem, level.isClientSide());
        }
        else
        {
            BlockEntity selectedBlockEntity = level.getBlockEntity(lookingAt.getBlockPos());
            if(selectedBlockEntity == null)
            {
                return InteractionResultHolder.pass(nodeItem);
            }

            Optional<ExternalWispNodeCapabilityProvider.Cap> capability = selectedBlockEntity.getCapability(IgnesFatui.Capabilities.EXTERNAL_WISP_NODE_HANDLER).resolve();
            if(capability.isEmpty())
            {
                return InteractionResultHolder.pass(nodeItem);
            }

            if(player instanceof ServerPlayer)
            {
                player.openMenu(new SimpleMenuProvider(
                        (id, inventory, menuPlayer)-> ExternalWispNodeMenu.CreateMenuServer(id, inventory, selectedBlockEntity),
                        selectedBlockEntity.getBlockState().getBlock().getName()
                ));
            }
            return InteractionResultHolder.sidedSuccess(nodeItem, level.isClientSide());
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
