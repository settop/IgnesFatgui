package settop.IgnesFatui.WispNetwork;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public abstract class WispNodeUpgrade
{
    private WispNode parentNode;

    //these are only called when the upgrade is already attached to a node
    public abstract void OnParentNodeConnectToNetwork();
    public abstract void OnParentNodeDisconnectFromNetwork();
    public abstract void OnParentNodeLinkedToBlockEntity();
    public abstract void OnParentNodeUnlinkedFromBlockEntity();

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
