package settop.IgnesFatui.GUI;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IntArray;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import settop.IgnesFatui.GUI.SubContainers.*;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Items.WispEnhancementItem;
import settop.IgnesFatui.Utils.BoolArray;
import settop.IgnesFatui.Wisps.BasicWispContents;
import settop.IgnesFatui.Wisps.Enhancements.EnhancementTypes;
import settop.IgnesFatui.Wisps.Enhancements.IEnhancement;
import settop.IgnesFatui.Wisps.Enhancements.ProviderEnhancement;
import settop.IgnesFatui.Wisps.WispBase;

import java.util.ArrayList;
import java.util.List;

public class BasicWispContainer extends MultiScreenContainer implements BasicWispContents.OnEnhancementChanged
{
    private BasicWispContents wispContents;
    private WispBase parentWisp;
    private BlockState blockState;
    private TileEntity tileEntity;
    private IntReferenceHolder openSubContainerIndex;
    private List<SubContainer> tabbedContainers;
    private PlayerInventorySubContainer playerInvSubContainer;
    private BoolArray enhancementPresent;

    public static BasicWispContainer CreateContainer(int id, PlayerInventory playerInventory, PacketBuffer extraData)
    {
        int contentsSize = extraData.readInt();
        BlockPos pos = extraData.readBlockPos();
        BlockState blockState = playerInventory.player.getEntityWorld().getBlockState(pos);
        TileEntity tileEntity = playerInventory.player.getEntityWorld().getTileEntity(pos);
        return new BasicWispContainer(id, playerInventory, new BasicWispContents( contentsSize ), null, blockState, tileEntity);
    }

    public static BasicWispContainer CreateContainer(int id, PlayerInventory playerInventory, BasicWispContents inWispContents, WispBase inParentWisp)
    {
        BlockPos pos = inParentWisp.GetPos();
        BlockState blockState = playerInventory.player.getEntityWorld().getBlockState(pos);
        TileEntity tileEntity = playerInventory.player.getEntityWorld().getTileEntity(pos);
        return new BasicWispContainer(id, playerInventory, inWispContents, inParentWisp, blockState, tileEntity);
    }


    public static final int WISP_SLOT_XPOS = 3;
    public static final int WISP_SLOT_YPOS = 16;
    public static final int PLAYER_INVENTORY_XPOS = 3;
    public static final int PLAYER_INVENTORY_YPOS = 101;

    private BasicWispContainer(int id, PlayerInventory playerInventory, BasicWispContents inWispContents, WispBase inParentWisp, BlockState inBlockState, TileEntity inTileEntity)
    {
        super(IgnesFatui.Containers.BASIC_WISP_CONTAINER, id);

        playerInvSubContainer = new PlayerInventorySubContainer(playerInventory, PLAYER_INVENTORY_XPOS, PLAYER_INVENTORY_YPOS);
        WispContentsContainer wispContentsContainer = new WispContentsContainer(inWispContents, WISP_SLOT_XPOS, WISP_SLOT_YPOS);

        wispContents = inWispContents;
        parentWisp = inParentWisp;
        blockState = inBlockState;
        tileEntity = inTileEntity;
        openSubContainerIndex = IntReferenceHolder.single();
        openSubContainerIndex.set(0);
        enhancementPresent = new BoolArray(EnhancementTypes.NUM);

        trackInt(openSubContainerIndex);
        trackIntArray(enhancementPresent);

        tabbedContainers = new ArrayList<>();
        tabbedContainers.add(wispContentsContainer);
        for(int i = 0; i < EnhancementTypes.NUM; ++i)
        {
            SubContainer enhancementSubContainer = EnhancementTypes.values()[i].GetFactory().CreateSubContainer(0, 0, blockState, tileEntity);
            enhancementSubContainer.SetActive(false);
            tabbedContainers.add(enhancementSubContainer);
        }

        List<SubContainer> subContainers = new ArrayList<>();
        subContainers.add(playerInvSubContainer);
        subContainers.addAll(tabbedContainers);
        SetSubContainers(subContainers);

        if(!playerInventory.player.world.isRemote())
        {
            //only care about this on the server
            wispContents.SetListener(this);
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn)
    {
        return wispContents.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerEntity, int sourceSlotIndex)
    {
        Slot sourceSlot = inventorySlots.get(sourceSlotIndex);
        if (sourceSlot == null || !sourceSlot.getHasStack()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getStack();
        ItemStack copyOfSourceStack = sourceStack.copy();

        //first slots are player inventory
        int playerInvSlotCount = playerInvSubContainer.inventorySlots.size();

        if(sourceSlotIndex < playerInvSlotCount)
        {
            int openIndex = openSubContainerIndex.get();
            int slotStart = playerInvSlotCount;
            for(int i = 0; i < openIndex; ++i)
            {
                slotStart += tabbedContainers.get(i).inventorySlots.size();
            }
            int slotEnd = slotStart + tabbedContainers.get(openIndex).inventorySlots.size();
            //from the player inventory
            if (!mergeItemStack(sourceStack, slotStart, slotEnd, false))
            {
                return ItemStack.EMPTY;
            }
        }
        else if(sourceSlotIndex < inventorySlots.size())
        {
            //from the sub container
            if (!mergeItemStack(sourceStack, 0, playerInvSlotCount, false))
            {
                return ItemStack.EMPTY;
            }
        }
        else
        {
            IgnesFatui.LOGGER.warn("Invalid slotIndex:" + sourceSlotIndex);
            return ItemStack.EMPTY;
        }

        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0)
        {
            sourceSlot.putStack(ItemStack.EMPTY);
        }
        else
        {
            sourceSlot.onSlotChanged();
        }

        sourceSlot.onTake(playerEntity, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn)
    {
        wispContents.SetListener(null);
        if(parentWisp != null)
        {
            parentWisp.UpdateFromContents();
        }
        super.onContainerClosed(playerIn);
    }

    @Override
    public void updateProgressBar(int id, int data)
    {
        super.updateProgressBar(id, data);
        if(id == 0)
        {
            UpdateActiveSubContainers();
        }
    }

    private void UpdateActiveSubContainers()
    {
        for(int i = 0; i < tabbedContainers.size(); ++i)
        {
            tabbedContainers.get(i).SetActive(i == openSubContainerIndex.get());
        }
    }

    @Override
    public void OnEnhancementChange(int index, IEnhancement previousEnhancement, IEnhancement nextEnhancement)
    {
        if(previousEnhancement != null)
        {
            enhancementPresent.SetBool(previousEnhancement.GetType().ordinal(), false);
            SubContainer enhancementContainer = tabbedContainers.get(previousEnhancement.GetType().ordinal() + 1);
            IEnhancementSubContainer enhanceCont = (IEnhancementSubContainer)enhancementContainer;
            enhanceCont.SetEnhancement(null);
        }

        if(nextEnhancement != null)
        {
            enhancementPresent.SetBool(nextEnhancement.GetType().ordinal(), true);
            SubContainer enhancementContainer = tabbedContainers.get(nextEnhancement.GetType().ordinal() + 1);
            IEnhancementSubContainer enhanceCont = (IEnhancementSubContainer)enhancementContainer;
            enhanceCont.SetEnhancement(nextEnhancement);
        }
    }

    public BasicWispContents GetWispContents() { return wispContents; }

    public SubContainer GetEnhancementSubContainer(EnhancementTypes enhancementType)
    {
        return tabbedContainers.get(enhancementType.ordinal() + 1);
    }

    public PlayerInventorySubContainer GetPlayerInventorySubContainer()
    {
        return playerInvSubContainer;
    }

    public boolean IsTabActive(int index)
    {
        if(index == 0)
        {
            return true;
        }
        else if(index - 1 < EnhancementTypes.NUM)
        {
            return enhancementPresent.GetBool(index - 1);
        }
        else
        {
            return false;
        }
    }

    public boolean IsTabSelected(int index)
    {
        return index == openSubContainerIndex.get();
    }

    public void SelectTab(int index)
    {
        if(!IsTabActive(index))
        {
            IgnesFatui.LOGGER.warn("Setting selected tab to an inactive tab");
            return;
        }
        if(index >= tabbedContainers.size())
        {
            IgnesFatui.LOGGER.error("Setting selected tab to an invalid tab");
            return;
        }

        openSubContainerIndex.set(index);
        UpdateActiveSubContainers();
    }
}
