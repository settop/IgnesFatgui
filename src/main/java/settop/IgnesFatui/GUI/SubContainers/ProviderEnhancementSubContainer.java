package settop.IgnesFatui.GUI.SubContainers;

import net.minecraft.util.Direction;
import net.minecraft.util.IntArray;
import settop.IgnesFatui.Client.Screens.SubScreens.ProviderSubScreen;
import settop.IgnesFatui.Client.Screens.SubScreens.SubScreen;
import settop.IgnesFatui.IgnesFatui;
import settop.IgnesFatui.Utils.BoolArray;
import settop.IgnesFatui.Wisps.Enhancements.IEnhancement;
import settop.IgnesFatui.Wisps.Enhancements.ProviderEnhancement;

public class ProviderEnhancementSubContainer extends SubContainer implements IEnhancementSubContainer
{
    private ProviderEnhancement currentEnhancement;
    private BoolArray directionsValue = new BoolArray(6);

    public ProviderEnhancementSubContainer(int xPos, int yPos)
    {
        super(xPos, yPos);
        trackIntArray(directionsValue);
    }

    @Override
    public void SetActive(boolean active)
    {
        super.SetActive(active);
        if(!active)
        {
            UpdateEnhancement();
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
        }
    }

    @Override
    public SubScreen CreateScreen()
    {
        return new ProviderSubScreen(this);
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
            }
            else
            {
                IgnesFatui.LOGGER.warn("Setting a non-provider enhancement to provider enhancement sub container");
            }
        }
        else
        {
            currentEnhancement = null;
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
}
