package settop.IgnesFatui.WispNetwork;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public abstract class WispNodeUpgrade
{
    private final WispNode parentNode;

    public WispNodeUpgrade(@NotNull WispNode parentNode)
    {
        this.parentNode = parentNode;
    }

    public abstract void OnParentNodeConnectToNetwork();
    public abstract void OnParentNodeDisconnectFromNetwork();

    protected WispNode GetParentNode()
    {
        return parentNode;
    }
}
