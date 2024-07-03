package settop.IgnesFatui.Blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import settop.IgnesFatui.BlockEntities.WispNodeBlockEntity;

import java.util.List;

public class WispNodeBlock extends Block implements EntityBlock
{
    public static final DirectionProperty FACING = DirectionProperty.create("facing");

    public WispNodeBlock()
    {
        super(Block.Properties.of().mapColor(MapColor.METAL).
                strength(4.0F, 10.0F).
                sound(SoundType.METAL)
                .noCollission() );

        registerDefaultState(defaultBlockState().setValue(FACING, Direction.DOWN));
    }

    @Override
    protected boolean canSurvive(@NotNull BlockState blockState, @NotNull LevelReader level, @NotNull BlockPos pos)
    {
        Direction direction = blockState.getValue(FACING);
        return canSupportCenter(level, pos.offset(direction.getNormal()), direction.getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState blockState, @NotNull Direction updatedDirection, @NotNull BlockState updatedBlockState, @NotNull LevelAccessor level, @NotNull BlockPos blockPos, @NotNull BlockPos updatedBlockPos)
    {
        if(updatedDirection == blockState.getValue(FACING))
        {
            if(!canSurvive(blockState, level, blockPos))
            {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(blockState, updatedDirection, updatedBlockState, level, blockPos, updatedBlockPos);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state)
    {
        return new WispNodeBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull BlockState previousState, boolean p_60570_)
    {
        super.onPlace(state, level, pos, previousState, p_60570_);
        var blockEntity = (WispNodeBlockEntity)level.getBlockEntity(pos);
        if(blockEntity != null)
        {
            blockEntity.onPlaced();
        }
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState nextState, boolean p_60519_)
    {
        var blockEntity = (WispNodeBlockEntity)level.getBlockEntity(pos);
        if(blockEntity != null)
        {
            blockEntity.onRemoved();
        }
        super.onRemove(state, level, pos, nextState, p_60519_);
    }

    @Override
    protected @NotNull List<ItemStack> getDrops(@NotNull BlockState blockState, LootParams.@NotNull Builder lootBuilder)
    {
        //ToDo: store meta data on the item
        return super.getDrops(blockState, lootBuilder);
    }
}
