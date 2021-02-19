package settop.IgnesFatui.GUI.SubContainers;

import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import settop.IgnesFatui.Client.Client;
import settop.IgnesFatui.Client.Screens.MultiScreen;
import settop.IgnesFatui.Client.Screens.SubScreens.ProviderSubScreen;
import settop.IgnesFatui.Client.Screens.SubScreens.SubScreen;
import settop.IgnesFatui.GUI.FakeSlot;
import settop.IgnesFatui.GUI.IActivatableSlot;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Utils.BoolArray;
import settop.IgnesFatui.Utils.FakeInventory;
import settop.IgnesFatui.Utils.StringReferenceHolder;
import settop.IgnesFatui.Utils.StringVariableArrayReferenceHolder;
import settop.IgnesFatui.Wisps.Enhancements.IEnhancement;
import settop.IgnesFatui.Wisps.Enhancements.ProviderEnhancement;

import java.util.ArrayList;

public class ProviderEnhancementSubContainer extends SubContainer implements IEnhancementSubContainer
{
    private ProviderEnhancement currentEnhancement;
    private BoolArray directionsValue = new BoolArray(6);
    private IntReferenceHolder whiteListEnabled = IntReferenceHolder.single();
    private BlockState blockState;
    private FakeInventory filter = new FakeInventory( ProviderEnhancement.FILTER_SIZE, false );
    private StringVariableArrayReferenceHolder tagFilters = new StringVariableArrayReferenceHolder(';');
    private IntReferenceHolder filterType = IntReferenceHolder.single();

    private FakeInventory tagGetHelper = new FakeInventory( 1, false );

    public static final int WHITELIST_PROPERTY_ID = 0;
    public static final int FILTER_TYPE_PROPERTY_ID = 1;

    public static final int FILTER_TAGS_STRING_PROPERTY_ID = 0;

    public static final int FILTER_SLOT_X = 21;
    public static final int FILTER_SLOT_Y = 1;

    public static final int TAG_FETCH_HELPER_SLOT_X = 1;
    public static final int TAG_FETCH_HELPER_SLOT_Y = 64;

    public ProviderEnhancementSubContainer(int xPos, int yPos, BlockState blockState, TileEntity tileEntity)
    {
        super(xPos, yPos);
        trackIntArray(directionsValue);
        trackInt(whiteListEnabled);
        trackInt(filterType);
        trackStr(tagFilters);
        this.blockState = blockState;

        for(int i = 0; i < filter.getSizeInventory(); ++i)
        {
            int column = i % ProviderEnhancement.FILTER_NUM_COLUMNS;
            int row = i / ProviderEnhancement.FILTER_NUM_COLUMNS;
            inventorySlots.add( new FakeSlot(filter, i, xPos + FILTER_SLOT_X + column * Client.SLOT_X_SPACING, yPos + FILTER_SLOT_Y + row * Client.SLOT_Y_SPACING));
        }
        inventorySlots.add( new FakeSlot(tagGetHelper, 0, xPos + TAG_FETCH_HELPER_SLOT_X, yPos + TAG_FETCH_HELPER_SLOT_Y));
    }

    @Override
    public void SetActive(boolean active)
    {
        if(isActive != active)
        {
            if(!active)
            {
                UpdateEnhancement();
            }
            isActive = active;
        }

        boolean filterSlotsActive = filterType.get() == ProviderEnhancement.eFilterType.Item.ordinal();
        for (int i = 0; i < filter.getSizeInventory(); ++i)
        {
            Slot slot = inventorySlots.get(i);
            if (slot instanceof IActivatableSlot)
            {
                ((IActivatableSlot) slot).SetActive(active && filterSlotsActive);
            }
        }
        GetTagFetchHelperSlot().SetActive(active && !filterSlotsActive);
    }

    @Override
    public void HandlePropertyUpdate(int propertyId, int value)
    {
        switch(propertyId)
        {
            case WHITELIST_PROPERTY_ID:
                whiteListEnabled.set(value);
                break;
            case FILTER_TYPE_PROPERTY_ID:
                filterType.set(value);
                break;
        }
    }

    @Override
    public void HandleStringPropertyUpdate(int propertyId, String value)
    {
        switch (propertyId)
        {
            case FILTER_TAGS_STRING_PROPERTY_ID:
                tagFilters.set(value);
                break;
        }
    }

    @Override
    public void OnClose()
    {
        super.OnClose();
        UpdateEnhancement();
        currentEnhancement = null;
    }

    private void UpdateEnhancement()
    {
        if(currentEnhancement != null)
        {
            for(int i = 0; i < 6; ++i)
            {
                currentEnhancement.SetDirectionProvided(Direction.byIndex(i), directionsValue.GetBool(i));
            }
            currentEnhancement.SetWhitelistEnabled(whiteListEnabled.get() != 0);
            for(int i = 0; i < filter.getSizeInventory(); ++i)
            {
                currentEnhancement.GetFilter().setInventorySlotContents(i, filter.getStackInSlot(i));
            }
            currentEnhancement.SetTagFilters( GetFilterTags() );
            currentEnhancement.SetFilterType(ProviderEnhancement.eFilterType.values()[filterType.get()]);
        }
    }

    @Override
    public SubScreen CreateScreen(MultiScreen<?> parentScreen)
    {
        return new ProviderSubScreen(this, parentScreen);
    }

    @Override
    public void SetEnhancement(IEnhancement enhancement)
    {
        if(enhancement != null)
        {
            if(enhancement instanceof ProviderEnhancement)
            {
                currentEnhancement = (ProviderEnhancement)enhancement;
                for(int i = 0; i < 6; ++i)
                {
                    directionsValue.SetBool(i, currentEnhancement.IsDirectionSet(Direction.byIndex(i)));
                }

                whiteListEnabled.set(currentEnhancement.IsWhitelistEnabled() ? 1 : 0);
                for(int i = 0; i < filter.getSizeInventory(); ++i)
                {
                    filter.setInventorySlotContents(i, currentEnhancement.GetFilter().getStackInSlot(i));
                }
                if(currentEnhancement.GetTagFilters() != null)
                {
                    ArrayList<String> tags = new ArrayList<>();
                    for (ResourceLocation tag : currentEnhancement.GetTagFilters())
                    {
                        tags.add(tag.toString());
                    }
                    tagFilters.setArray(tags);
                }
                filterType.set(currentEnhancement.GetFilterType().ordinal());
            }
            else
            {
                IgnesFatui.LOGGER.warn("Setting a non-provider enhancement to provider enhancement sub container");
            }
        }
        else
        {
            currentEnhancement = null;
            filter.clear();
        }
    }

    public void SetDirectionProvided(Direction direction, boolean isSet)
    {
        directionsValue.SetBool( direction.ordinal(), isSet );
    }

    public BoolArray GetDirectionsProvided()
    {
        return directionsValue;
    }
    public BlockState GetBlockState() { return blockState; }

    public boolean GetWhitelistEnabled()
    {
        return whiteListEnabled.get() != 0;
    }
    public void SetWhitelistEnabled(boolean enabled)
    {
        whiteListEnabled.set(enabled ? 1 : 0);
    }

    public FakeSlot GetTagFetchHelperSlot()
    {
        return (FakeSlot)inventorySlots.get(filter.getSizeInventory());
    }

    public ArrayList<String> GetFilterTags()
    {
        return tagFilters.getArray();
    }

    public String SetFilterTags(ArrayList<String> tags)
    {
        tagFilters.setArray(tags);
        return tagFilters.get();
    }

    public ProviderEnhancement.eFilterType GetFilterType()
    {
        return ProviderEnhancement.eFilterType.values()[filterType.get()];
    }

    public void SetFilterType(ProviderEnhancement.eFilterType filterType)
    {
        this.filterType.set(filterType.ordinal());
        //make sure to refresh this
        SetActive(isActive);
    }
}
