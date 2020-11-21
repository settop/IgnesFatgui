package settop.IgnesFatui.Events;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import settop.IgnesFatui.Blocks.WispCore;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.TileEntities.WispCoreTileEntity;

@Mod.EventBusSubscriber( modid = IgnesFatui.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT )
public class BlockEventHandler
{
    @SubscribeEvent
    public static void OnWispCoreRingBlockPlace(BlockEvent.EntityPlaceEvent blockPlaced)
    {
        boolean blockCanFormMultiBlock = false;
        for(ResourceLocation tag : WispCoreTileEntity.RING_BLOCK_TAGS)
        {
            if (blockPlaced.getPlacedBlock().getBlock().getTags().contains(tag))
            {
                blockCanFormMultiBlock = true;
                break;
            }
        }
        if(!blockCanFormMultiBlock)
        {
            return;
        }

        for(int x = -1; x <=1; ++x)
            for(int y = -1; y <=1; ++y)
                for(int z = -1; z <=1; ++z)
                {
                    if(x == 0 && y == 0 && z == 0) continue;
                    //check for a wisp core tile entity
                    BlockPos blockPos = blockPlaced.getPos().add(x, y, z);
                    WispCoreTileEntity tileEntity = (WispCoreTileEntity)blockPlaced.getWorld().getTileEntity(blockPos);
                    if(tileEntity != null)
                    {
                        tileEntity.TryToFormMultiBlock();
                        return;
                    }
                }
    }


    @SubscribeEvent
    public static void OnWispCoreRingBreak(BlockEvent.BreakEvent blockBroken)
    {
        if(blockBroken.getState().getBlock() != IgnesFatui.RegistryHandler.WISP_CORE.get())
        {
            return;
        }
        WispCore.WispCoreType coreType = blockBroken.getState().get(WispCore.TYPE);
        if(coreType == WispCore.WispCoreType.CORE)
        {
            //the core is handled normally
            return;
        }

        for(int x = -1; x <=1; ++x)
            for(int y = -1; y <=1; ++y)
                for(int z = -1; z <=1; ++z)
                {
                    if(x == 0 && y == 0 && z == 0) continue;
                    //check for a wisp core tile entity
                    BlockPos blockPos = blockBroken.getPos().add(x, y, z);
                    WispCoreTileEntity tileEntity = (WispCoreTileEntity)blockBroken.getWorld().getTileEntity(blockPos);
                    if(tileEntity != null)
                    {
                        if(coreType == WispCore.WispCoreType.RING)
                        {
                            BlockState dropBlock = tileEntity.BreakMultiBlock(blockBroken.getPos());
                            Block.spawnDrops(dropBlock, (World) blockBroken.getWorld(), blockBroken.getPos(), null, blockBroken.getPlayer(), blockBroken.getPlayer().getHeldItemMainhand());
                        }
                        else
                        {
                            //do nothing for the 'air' block
                            blockBroken.setCanceled(true);
                        }
                        return;
                    }
                }

    }
}
