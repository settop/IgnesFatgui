package settop.IgnesFatui.Blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.extensions.IForgeBlock;
import settop.IgnesFatui.TileEntities.WispCoreTileEntity;

public class WispCore extends Block implements IForgeBlock
{
    public enum WispCoreType implements IStringSerializable
    {
        CORE,
        RING,
        AIR,
        CORE_COMPLETE;

        @Override
        public String getString()
        {
            switch(this)
            {
                case CORE: return "core";
                case RING: return "ring";
                case AIR: return "air";
                case CORE_COMPLETE: return "core_complete";
                default: return "";
            }
        }
    }

    public static final EnumProperty<WispCoreType> TYPE = EnumProperty.create("type", WispCoreType.class );

    public WispCore()
    {
        super( Block.Properties.create( Material.IRON )
                .hardnessAndResistance( 4.f, 10.f )
                .harvestLevel( 2 )
                .harvestTool( ToolType.PICKAXE )
                .notSolid() );

        setDefaultState(stateContainer.getBaseState().with(TYPE, WispCoreType.CORE));
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if(state.get(TYPE) == WispCoreType.CORE)
        {
            WispCoreTileEntity tileEntity = (WispCoreTileEntity) worldIn.getTileEntity(pos);
            if (tileEntity != null)
            {
                tileEntity.TryToFormMultiBlock();
            }
        }
    }

    public boolean hasTileEntity(BlockState state)
    {
        switch(state.get(TYPE))
        {
            case CORE:
            case CORE_COMPLETE:
                return true;
            default:
                return false;
        }
    }

    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        switch(state.get(TYPE))
        {
            case CORE:
            case CORE_COMPLETE:
                return new WispCoreTileEntity();
            default:
                return null;
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(TYPE);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos)
    {
        if(state.get(TYPE) == WispCoreType.CORE)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state)
    {
        if(state.get(TYPE) == WispCoreType.CORE)
        {
            return super.getRenderType(state);
        }
        else
        {
            return BlockRenderType.INVISIBLE;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public float getAmbientOcclusionLightValue(BlockState state, IBlockReader worldIn, BlockPos pos)
    {
        if(state.get(TYPE) == WispCoreType.CORE)
        {
            return 0.f;
        }
        else
        {
            return 1.f;
        }
    }

    public BlockState getBlockState( WispCoreType type )
    {
        return this.stateContainer.getBaseState().with(TYPE, type);
    }

}
