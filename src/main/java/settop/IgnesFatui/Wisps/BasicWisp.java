package settop.IgnesFatui.Wisps;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import settop.IgnesFatui.GUI.BasicWispContainer;
import settop.IgnesFatui.GUI.MultiScreenContainer;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Items.WispEnhancementItem;
import settop.IgnesFatui.Utils.Utils;
import settop.IgnesFatui.Wisps.Enhancements.IEnhancement;

import java.util.ArrayList;

public class BasicWisp extends WispBase
{
    private BasicWispContents contents = new BasicWispContents(2);
    private ArrayList<IEnhancement> enhancements;


    public BasicWisp()
    {
        super();
    }

    public BasicWisp(IChunk chunk, BlockPos inPos)
    {
        super(chunk, inPos);
    }

    @Override
    public CompoundNBT Save()
    {
        CompoundNBT nbt = super.Save();

        if(!contents.isEmpty())
        {
            nbt.put("inv", contents.write());
        }

        return nbt;
    }

    @Override
    public void Load(IChunk chunk, CompoundNBT nbt)
    {
        super.Load(chunk, nbt);
        contents.read(nbt, "inv");
        UpdateFromContents();
    }

    @Override
    public String GetType()
    {
        return WispConstants.BASIC_WISP;
    }

    @Override
    public void DropItemStackIntoWorld(IWorld world)
    {
        ItemStack droppedStack = new ItemStack(IgnesFatui.Items.WISP_ITEM.get(), 1);
        if(!contents.isEmpty())
        {
            droppedStack.setTagInfo("inv", contents.write());
        }
        Utils.SpawnAsEntity((World)world, GetPos(), droppedStack );
    }

    @Override
    public void InitFromTagData(CompoundNBT tagData)
    {
        if(tagData == null)
        {
            return;
        }
        contents.read(tagData, "inv");
        UpdateFromContents();
    }

    @Override
    public void UpdateFromContents()
    {
        enhancements = new ArrayList<>();
        for(int i = 0; i < contents.getSizeInventory(); ++i)
        {
            ItemStack contentsItem = contents.getStackInSlot(i);
            if(contentsItem == null || contentsItem.isEmpty())
            {
                continue;
            }

            if(contentsItem.getItem() instanceof WispEnhancementItem)
            {
                IEnhancement newEnhancement = contentsItem.getCapability(IgnesFatui.Capabilities.CAPABILITY_ENHANCEMENT).resolve().get();
                enhancements.add(newEnhancement);
            }
            else
            {
                IgnesFatui.LOGGER.warn("Unrecognised wisp enhancement");
            }
        }
    }

    // From INamedContainerProvider
    @Override
    public ITextComponent getDisplayName()
    {
        return new TranslationTextComponent("container.sif1.basic_wisp_container");
    }

    @Override
    public Container createMenu(int windowID, PlayerInventory playerInventory, PlayerEntity player)
    {
        MultiScreenContainer container = BasicWispContainer.CreateContainer(windowID, playerInventory, contents, this);

        return container;
    }

    @Override
    public void ContainerExtraDataWriter(PacketBuffer packetBuffer)
    {
        packetBuffer.writeInt(contents.getSizeInventory());
        packetBuffer.writeBlockPos( GetPos() );
    }
}
