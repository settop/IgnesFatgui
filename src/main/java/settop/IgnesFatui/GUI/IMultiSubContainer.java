package settop.IgnesFatui.GUI;

import settop.IgnesFatui.GUI.MultiScreenContainer;

import java.lang.ref.WeakReference;

public interface IMultiSubContainer
{
    void SetParent(WeakReference<MultiScreenContainer> parentContainer, int subWindowId);


}
