package settop.IgnesFatui.Client;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import settop.IgnesFatui.IgnesFatui;

import static net.minecraft.inventory.container.PlayerContainer.LOCATION_BLOCKS_TEXTURE;

@Mod.EventBusSubscriber(modid = IgnesFatui.MOD_ID, bus=Mod.EventBusSubscriber.Bus.MOD)
@OnlyIn(Dist.CLIENT)
public class ClientStartup
{
    @SubscribeEvent
    public static void onModelRegistryEvent(ModelRegistryEvent event)
    {
        ModelLoader.addSpecialModel(Client.WISP_CORE_RING_0);
        ModelLoader.addSpecialModel(Client.WISP_CORE_RING_1);
        ModelLoader.addSpecialModel(Client.WISP_CORE_RING_2);
    }

    @SubscribeEvent
    public static void onTextureStitchEvent(TextureStitchEvent.Pre event)
    {
        if (event.getMap().getTextureLocation() == LOCATION_BLOCKS_TEXTURE)
        {
            event.addSprite(Client.WISP_CORE_RING_TEX);
        }
    }

}
