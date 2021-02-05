package settop.IgnesFatui.Wisps;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IWispAddListener
{
    BlockPos GetPos();
    int GetRange();

    void WispObjAdded(World world, BlockPos pos, Object obj);
}
