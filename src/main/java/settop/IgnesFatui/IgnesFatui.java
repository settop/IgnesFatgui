package settop.IgnesFatui;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settop.IgnesFatui.Blocks.WispCore;
import settop.IgnesFatui.Client.Renderers.WispCoreTileRenderer;
import settop.IgnesFatui.Items.BasicWispItem;
import settop.IgnesFatui.TileEntities.WispCoreTileEntity;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("sif1")
public class IgnesFatui
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "sif1";

    public IgnesFatui()
    {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);

        RegistryHandler.init();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
    }

    private void setupClient(final FMLClientSetupEvent event)
    {
        ClientRegistry.bindTileEntityRenderer(RegistryHandler.WISP_CORE_TILE_ENTITY.get(), WispCoreTileRenderer::new );
    }


    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent)
        {
            /*blockRegistryEvent.getRegistry().registerAll
                    (
                            testBlock
                    );*/
        }

        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent)
        {
            /*itemRegistryEvent.getRegistry().registerAll
                    (
                            new BlockItem( testBlock, new Item.Properties().group(ItemGroup.MISC) )
                    );*/
        }
    }
    public static class RegistryHandler
    {
        public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, IgnesFatui.MOD_ID);
        public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, IgnesFatui.MOD_ID);
        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IgnesFatui.MOD_ID);

        public static void init()
        {
            BLOCKS.register( FMLJavaModLoadingContext.get().getModEventBus() );
            TILE_ENTITIES.register( FMLJavaModLoadingContext.get().getModEventBus() );
            ITEMS.register( FMLJavaModLoadingContext.get().getModEventBus() );
        }


        // Blocks
        public static final RegistryObject<Block> WISP_CORE = BLOCKS.register("wisp_core", WispCore::new );

        // Block Items
        public static final RegistryObject<Item> WISP_CORE_ITEM = ITEMS.register("wisp_core", ()->{ return new BlockItem( WISP_CORE.get(), new Item.Properties().group(ItemGroup.MISC) ); });

        // Tile Entities
        public static final RegistryObject<TileEntityType<WispCoreTileEntity>> WISP_CORE_TILE_ENTITY = TILE_ENTITIES.register("wisp_core",
                ()->{ return TileEntityType.Builder.create(WispCoreTileEntity::new, WISP_CORE.get() ).build(null); });

        // Items
        public static final RegistryObject<Item> WISP_ITEM = ITEMS.register("wisp", ()->{ return new BasicWispItem(); });


    }
}
