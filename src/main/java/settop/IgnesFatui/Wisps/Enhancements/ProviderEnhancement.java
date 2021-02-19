package settop.IgnesFatui.Wisps.Enhancements;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import settop.IgnesFatui.GUI.SubContainers.ProviderEnhancementSubContainer;
import settop.IgnesFatui.GUI.SubContainers.SubContainer;
import settop.IgnesFatui.Utils.FakeInventory;

import java.util.ArrayList;

public class ProviderEnhancement implements IEnhancement
{
    public static class Factory implements IEnhancement.IFactory
    {
        @Override
        public IEnhancement Create()
        {
            return new ProviderEnhancement();
        }

        @Override
        public SubContainer CreateSubContainer(int xPos, int yPos, BlockState blockState, TileEntity tileEntity) { return new ProviderEnhancementSubContainer(xPos, yPos, blockState, tileEntity); }
    }

    public enum eFilterType
    {
        Item,
        Tag
    }

    public static final Factory FACTORY = new Factory();
    public static final int FILTER_NUM_COLUMNS = 8;
    public static final int FILTER_NUM_ROWS = 4;
    public static final int FILTER_SIZE = FILTER_NUM_COLUMNS * FILTER_NUM_ROWS;

    private boolean providedDirections[];
    private boolean whitelistEnabled = false;
    private FakeInventory filter = new FakeInventory(FILTER_SIZE, false);
    private ArrayList<ResourceLocation> tagFilter;
    private eFilterType filterType = eFilterType.Item;

    @Override
    public CompoundNBT SerializeNBT()
    {
        CompoundNBT nbt = new CompoundNBT();

        if(providedDirections != null)
        {
            boolean anyFalse = false;

            for (boolean b : providedDirections)
            {
                if (!b)
                {
                    anyFalse = true;
                    break;
                }
            }

            if (anyFalse)
            {
                CompoundNBT directionsNBT = new CompoundNBT();
                for (int i = 0; i < 6; ++i)
                {
                    directionsNBT.putBoolean(Direction.byIndex(i).getString(), providedDirections[i]);
                }

                nbt.put("providerDirections", directionsNBT);
            }
        }

        if(whitelistEnabled != false)
        {
            nbt.putBoolean("whitelistEnabled", whitelistEnabled);
        }

        if(!filter.isEmpty())
        {
            CompoundNBT filterNBT = new CompoundNBT();
            filter.Save(filterNBT);
            nbt.put("filter", filterNBT);
        }

        if(tagFilter != null)
        {
            ListNBT tagFilterListNBT = new ListNBT();

            for(ResourceLocation tag : tagFilter)
            {
                tagFilterListNBT.add(StringNBT.valueOf(tag.toString()));
            }

            nbt.put("tagFilter", tagFilterListNBT);
        }

        if(filterType != eFilterType.Item)
        {
            nbt.putInt("filterType", filterType.ordinal());
        }

        return nbt;
    }

    @Override
    public void DeserializeNBT(CompoundNBT nbt)
    {
        if (nbt == null)
        {
            return;
        }
        if (nbt.contains("providerDirections"))
        {
            providedDirections = new boolean[6];
            CompoundNBT directionsNBT = nbt.getCompound("providerDirections");

            for(int i = 0; i < 6; ++i)
            {
                providedDirections[i] = directionsNBT.getBoolean(Direction.byIndex(i).getString());
            }
        }
        if (nbt.contains("whitelistEnabled"))
        {
            whitelistEnabled = nbt.getBoolean("whitelistEnabled");
        }

        if (nbt.contains("filter"))
        {
            CompoundNBT filterNBT = nbt.getCompound("filter");
            filter.Load(filterNBT);
        }

        if (nbt.contains("tagFilter"))
        {
            tagFilter = new ArrayList<>();
            ListNBT tagFilterListNBT = nbt.getList("tagFilter", 8);
            for(INBT tag : tagFilterListNBT)
            {
                StringNBT stringNBT = (StringNBT)tag;
                tagFilter.add(ResourceLocation.create(stringNBT.getString(), ':'));
            }
        }

        if(nbt.contains("filterType"))
        {
            int type = nbt.getInt("filterType");
            if(type >= 0 && type < eFilterType.values().length)
            {
                filterType = eFilterType.values()[type];
            }
        }
    }

    @Override
    public EnhancementTypes GetType()
    {
        return EnhancementTypes.PROVIDER;
    }

    public boolean IsDirectionSet(Direction dir)
    {
        return providedDirections == null || providedDirections[dir.getIndex()];
    }

    public void SetDirectionProvided(Direction dir, boolean isProvided)
    {
        if(providedDirections == null)
        {
            providedDirections = new boolean[6];
            for(int i = 0; i < 6; ++i)
            {
                providedDirections[i] = true;
            }
        }
        providedDirections[dir.getIndex()] = isProvided;
    }

    public boolean IsWhitelistEnabled() { return whitelistEnabled; }
    public void SetWhitelistEnabled( boolean enabled ) { whitelistEnabled = enabled; }

    public FakeInventory GetFilter() { return filter; }

    public ArrayList<ResourceLocation> GetTagFilters() { return tagFilter; }
    public void SetTagFilters(ArrayList<String> tags)
    {
        tagFilter = null;
        if(tags != null && !tags.isEmpty())
        {
            tagFilter = new ArrayList<>();
            for (String tag : tags)
            {
                if(ResourceLocation.isResouceNameValid(tag))
                {
                    tagFilter.add(ResourceLocation.create(tag, ':'));
                }
            }
        }
    }

    public eFilterType GetFilterType() { return filterType; }
    public void SetFilterType(eFilterType filterType) { this.filterType = filterType; }
}
