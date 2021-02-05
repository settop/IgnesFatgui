package settop.IgnesFatui.TileEntities;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import settop.IgnesFatui.Blocks.WispCore;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.WispNetwork.WispNetwork;
import settop.IgnesFatui.Wisps.ChunkWispData;

public class WispCoreTileEntity extends TileEntity
{
    static public final ResourceLocation RING_BLOCK_TAGS[] =
            {
                    new ResourceLocation("forge", "storage_blocks/gold"),
                    new ResourceLocation("forge", "storage_blocks/quartz")
            };

    public float renderTimer = 0.f;

    private boolean multiBlockComplete = false;
    private boolean cachedMultiblockComplete = false;
    private BlockState ringOriginalBlocks[][][];
    private WispNetwork network;

    public WispCoreTileEntity()
    {
        super( IgnesFatui.TileEntities.WISP_CORE_TILE_ENTITY.get() );
    }

    @Override
    public void updateContainingBlockInfo()
    {
        super.updateContainingBlockInfo();
        cachedMultiblockComplete = false;
    }

    public boolean IsMultiblockComplete()
    {
        if(!cachedMultiblockComplete)
        {
            cachedMultiblockComplete = true;
            multiBlockComplete = world.getBlockState(getPos()).getBlockState().get(WispCore.TYPE) == WispCore.WispCoreType.CORE_COMPLETE;
        }
        return multiBlockComplete;
    }

    private static final int LAYOUT[][][] =
    {
            {
                    { -1,  0, -1 },
                    {  0,  1,  0 },
                    { -1,  0, -1 }
            },
            {
                    {  0,  1,  0 },
                    {  1, -1,  1 },
                    {  0,  1,  0 }
            },
            {
                    { -1,  0, -1 },
                    {  0,  1,  0 },
                    { -1,  0, -1 }
            }
    };

    public void TryToFormMultiBlock()
    {
        if(IsMultiblockComplete())
        {
            return;
        }
        BlockPos myBlockPos = getPos();

        for(int y = -1; y <=1; ++y)
            for(int x = -1; x <=1; ++x)
                for(int z = -1; z <=1; ++z)
                {
                    if(x == 0 && y == 0 && z == 0) continue;

                    BlockPos testPos = myBlockPos.add(x, y, z);
                    BlockState blockState = world.getBlockState(testPos);

                    int blockType = LAYOUT[y+1][x+1][z+1];
                    if(blockType == -1)
                    {
                        //needs to be air
                        if(!blockState.getBlock().isAir(blockState, world, testPos))
                        {
                            return;
                        }
                    }
                    else
                    {
                        //needs to be the right block
                        if(!blockState.getBlock().getTags().contains(RING_BLOCK_TAGS[blockType]))
                        {
                            return;
                        }
                    }

                }

        //we passed all the checks to become a multiblock
        FormMultiBlock();
    }

    private void FormMultiBlock()
    {
        BlockPos myBlockPos = getPos();
        WispCore coreBlock = (WispCore)IgnesFatui.Blocks.WISP_CORE.get();

        ringOriginalBlocks = new BlockState[3][3][3];

        world.setBlockState(myBlockPos, coreBlock.getBlockState(WispCore.WispCoreType.CORE_COMPLETE));

        for(int y = -1; y <=1; ++y)
            for(int x = -1; x <=1; ++x)
                for(int z = -1; z <=1; ++z)
                {
                    if (x == 0 && y == 0 && z == 0) continue;

                    BlockPos setPos = myBlockPos.add(x, y, z);
                    ringOriginalBlocks[y+1][x+1][z+1] = world.getBlockState(setPos);

                    int blockType = LAYOUT[y+1][x+1][z+1];
                    if (blockType == -1)
                    {
                        world.setBlockState(setPos, coreBlock.getBlockState(WispCore.WispCoreType.AIR));
                    }
                    else
                    {
                        world.setBlockState(setPos, coreBlock.getBlockState(WispCore.WispCoreType.RING));
                    }
                }

        if(!world.isRemote)
        {
            network = new WispNetwork(getPos());
            ChunkWispData.RegisterWispNetwork(world, network);
        }
        markDirty();
    }

    public BlockState BreakMultiBlock(BlockPos brokenPos)
    {
        BlockPos myBlockPos = getPos();
        WispCore coreBlock = (WispCore)IgnesFatui.Blocks.WISP_CORE.get();
        BlockPos offset = brokenPos.subtract(myBlockPos);

        world.setBlockState(myBlockPos, coreBlock.getBlockState(WispCore.WispCoreType.CORE));

        BlockState returnBlockState = null;

        for(int y = -1; y <=1; ++y)
            for(int x = -1; x <=1; ++x)
                for(int z = -1; z <=1; ++z)
                {
                    if (x == 0 && y == 0 && z == 0) continue;

                    if(offset.getX() == x && offset.getY() == y && offset.getZ() == z)
                    {
                        //this is the block being broken
                        returnBlockState = ringOriginalBlocks[y+1][x+1][z+1];
                    }
                    else
                    {
                        BlockPos setPos = getPos().add(x, y, z);
                        world.setBlockState(setPos, ringOriginalBlocks[y+1][x+1][z+1]);
                    }

                }

        if(!world.isRemote)
        {
            ChunkWispData.UnregisterWispNetwork(world, network);
            network = null;
        }
        ringOriginalBlocks = null;
        markDirty();
        return returnBlockState;
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        CompoundNBT nbt = super.write(compound);
        if(ringOriginalBlocks != null)
        {
            CompoundNBT originalBlockNBT = new CompoundNBT();

            for(int y = -1; y <=1; ++y)
            {
                CompoundNBT yNBT = new CompoundNBT();
                for (int x = -1; x <= 1; ++x)
                {
                    CompoundNBT xNBT = new CompoundNBT();
                    for (int z = -1; z <= 1; ++z)
                    {
                        BlockState blockState = ringOriginalBlocks[y+1][x+1][z+1];
                        if(blockState != null)
                        {
                            xNBT.put(String.valueOf(z), NBTUtil.writeBlockState(blockState));
                        }
                    }
                    yNBT.put(String.valueOf(x), xNBT);
                }
                originalBlockNBT.put( String.valueOf(y), yNBT );
            }
            nbt.put("RingOriginalBlocks", originalBlockNBT);
        }

        if(network != null)
        {
            CompoundNBT networkNBT = new CompoundNBT();
            nbt.put("Network", networkNBT);
        }

        return nbt;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read (state, nbt);

        CompoundNBT originalBlockNBT = nbt.getCompound("RingOriginalBlocks");

        if(originalBlockNBT != null)
        {
            ringOriginalBlocks = new BlockState[3][3][3];
            for(int y = -1; y <=1; ++y)
            {
                CompoundNBT yNBT = originalBlockNBT.getCompound( String.valueOf(y) );
                for (int x = -1; x <= 1; ++x)
                {
                    CompoundNBT xNBT = yNBT.getCompound( String.valueOf(x) );
                    for (int z = -1; z <= 1; ++z)
                    {
                        ringOriginalBlocks[y+1][x+1][z+1] = NBTUtil.readBlockState(xNBT.getCompound(String.valueOf(z)));
                    }
                }
            }
        }
        else
        {
            ringOriginalBlocks = null;
        }

        CompoundNBT networkNBT = nbt.getCompound("Network");
        if(networkNBT != null)
        {
            network = new WispNetwork(getPos());
        }
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        if(!world.isRemote && network != null)
        {
            ChunkWispData.RegisterWispNetwork(world, network);
        }
    }


    @OnlyIn(Dist.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return new AxisAlignedBB(getPos().add(-1, -1, -1), getPos().add(2, 2, 2));
    }
}
