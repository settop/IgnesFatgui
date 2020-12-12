package settop.IgnesFatui.Wisps.Enhancements;

import java.util.List;

public enum EnhancementTypes
{
    PROVIDER(ProviderEnhancement.FACTORY, "sif1.enhancement.provider");

    public static final int NUM = EnhancementTypes.values().length;

    private final IEnhancement.IFactory factory;
    private final String name;
    EnhancementTypes(IEnhancement.IFactory factory, String name)
    {
        this.factory = factory;
        this.name = name;
    }

    public IEnhancement.IFactory GetFactory() { return factory; }
    public String GetName() { return name; }
}
