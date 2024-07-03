package settop.IgnesFatui.Items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class DirectionalBlockItem extends BlockItem
{
    private final DirectionProperty directionProperty;

    public DirectionalBlockItem(Block block, Item.Properties properties, DirectionProperty directionProperty)
    {
        super(block, properties);
        this.directionProperty = directionProperty;
    }

    @Nullable
    protected BlockState getPlacementState(@NotNull BlockPlaceContext placeContext)
    {
        BlockState blockstate = getBlock().getStateForPlacement(placeContext);
        LevelReader levelreader = placeContext.getLevel();
        BlockPos blockPos = placeContext.getClickedPos();

        if(blockstate == null)
        {
            return null;
        }

        if(blockstate.canSurvive(levelreader, blockPos) && levelreader.isUnobstructed(blockstate, blockPos, CollisionContext.empty()))
        {
            return blockstate;
        }

        Direction initialDirection = blockstate.getValue(directionProperty);

        //initial direction failed, try to place perpendicular
        for(Direction dir : Direction.values())
        {
            if(dir == initialDirection || dir == initialDirection.getOpposite())
            {
                continue;
            }
            BlockState otherState = blockstate.setValue(directionProperty, dir);
            if(otherState.canSurvive(levelreader, blockPos) && levelreader.isUnobstructed(otherState, blockPos, CollisionContext.empty()))
            {
                return otherState;
            }
        }
        return null;
    }
}
