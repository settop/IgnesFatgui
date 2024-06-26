package settop.IgnesFatui.Utils;


import net.minecraft.world.inventory.ContainerData;

public class BoolArray implements ContainerData
{
    private final boolean[] array;

    public BoolArray(int size) {
        array = new boolean[size];
    }

    @Override
    public int get(int index) { return array[index] ? 1 : 0; }
    @Override
    public void set(int index, int value) {
        array[index] = value != 0;
    }

    public boolean GetBool(int index){ return array[index]; }
    public void SetBool(int index, boolean value){ array[index] = value; }

    @Override
    public int getCount() {
        return array.length;
    }
}
