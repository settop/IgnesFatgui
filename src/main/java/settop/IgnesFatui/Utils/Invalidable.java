package settop.IgnesFatui.Utils;

public class Invalidable
{
    private boolean isValid = true;

    public void SetInvalid() { isValid = false; }
    public boolean IsValid() { return isValid; }
}
