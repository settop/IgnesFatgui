package settop.IgnesFatui.WispNetwork;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public abstract class WispNodeUpgrade
{
    private WispNode parentNode;

    public abstract void OnParentNodeConnectToNetwork();
    public abstract void OnParentNodeDisconnectFromNetwork();

    public void OnAddToNode(@NotNull WispNode parentNode)
    {
        this.parentNode = parentNode;
    }

    public void OnRemoveFromNode(@NotNull WispNode parentNode)
    {
        assert this.parentNode == parentNode;
        this.parentNode = null;
    }

    public WispNode GetParentNode()
    {
        return parentNode;
    }
}
