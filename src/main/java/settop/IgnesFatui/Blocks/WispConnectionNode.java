package settop.IgnesFatui.Blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import settop.IgnesFatui.TileEntities.WispConnectionNodeTileEntity;
import settop.IgnesFatui.Wisps.ChunkWispData;

public class WispConnectionNode extends Block
{
    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class );

    public WispConnectionNode()
    {
        super(Block.Properties.create( Material.IRON )
                .hardnessAndResistance( 4.f, 10.f )
                .harvestLevel( 2 )
                .harvestTool( ToolType.PICKAXE )
                .notSolid() );

        setDefaultState(stateContainer.getBaseState().with(FACING, Direction.DOWN));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return stateContainer.getBaseState().with(FACING, context.getFace().getOpposite());
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new WispConnectionNodeTileEntity();
    }
}
