package settop.IgnesFatui;

import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settop.IgnesFatui.Blocks.WispConnectionNode;
import settop.IgnesFatui.Blocks.WispCore;
import settop.IgnesFatui.GUI.Network.GUIClientMessageHandler;
import settop.IgnesFatui.GUI.Network.GUIServerMessageHandler;
import settop.IgnesFatui.GUI.Network.Packets.*;
import settop.IgnesFatui.Items.BasicWispItem;
import settop.IgnesFatui.Items.WispEnhancementItem;
import settop.IgnesFatui.TileEntities.WispConnectionNodeTileEntity;
import settop.IgnesFatui.TileEntities.WispCoreTileEntity;
import settop.IgnesFatui.GUI.BasicWispContainer;
import settop.IgnesFatui.Wisps.Enhancements.EnhancementTypes;
import settop.IgnesFatui.Wisps.Enhancements.IEnhancement;

import java.util.Optional;

import static net.minecraftforge.fml.network.NetworkDirection.PLAY_TO_SERVER;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("sif1")
public class IgnesFatui
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "sif1";
    public static final String MULTI_SCREEN_CHANNEL_VERSION = "1.0.0";
    public static final SimpleChannel MULTI_SCREEN_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(MOD_ID, "multi_screen"), () -> MULTI_SCREEN_CHANNEL_VERSION,
            MULTI_SCREEN_CHANNEL_VERSION::equals,
            MULTI_SCREEN_CHANNEL_VERSION::equals);

    public IgnesFatui()
    {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);


        Blocks.BLOCKS.register( FMLJavaModLoadingContext.get().getModEventBus() );
        TileEntities.TILE_ENTITIES.register( FMLJavaModLoadingContext.get().getModEventBus() );
        Items.ITEMS.register( FMLJavaModLoadingContext.get().getModEventBus() );

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        MULTI_SCREEN_CHANNEL.registerMessage(1, CContainerTabSelected.class,
                CContainerTabSelected::encode, CContainerTabSelected::decode,
                GUIServerMessageHandler::OnMessageReceived,
                Optional.of(PLAY_TO_SERVER));
        MULTI_SCREEN_CHANNEL.registerMessage(2, CSubContainerDirectionChange.class,
                CSubContainerDirectionChange::encode, CSubContainerDirectionChange::decode,
                GUIServerMessageHandler::OnMessageReceived,
                Optional.of(PLAY_TO_SERVER));
        MULTI_SCREEN_CHANNEL.registerMessage(3, CScrollWindowPacket.class,
                CScrollWindowPacket::encode, CScrollWindowPacket::decode,
                GUIServerMessageHandler::OnMessageReceived,
                Optional.of(PLAY_TO_SERVER));
        MULTI_SCREEN_CHANNEL.registerMessage(4, CSubWindowPropertyUpdatePacket.class,
                CSubWindowPropertyUpdatePacket::encode, CSubWindowPropertyUpdatePacket::decode,
                GUIServerMessageHandler::OnMessageReceived,
                Optional.of(PLAY_TO_SERVER));
        MULTI_SCREEN_CHANNEL.registerMessage(5, CSubWindowStringPropertyUpdatePacket.class,
                CSubWindowStringPropertyUpdatePacket::encode, CSubWindowStringPropertyUpdatePacket::decode,
                GUIServerMessageHandler::OnMessageReceived,
                Optional.of(PLAY_TO_SERVER));
        MULTI_SCREEN_CHANNEL.registerMessage(6, SWindowStringPropertyPacket.class,
                SWindowStringPropertyPacket::encode, SWindowStringPropertyPacket::decode,
                GUIClientMessageHandler::OnMessageReceived,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CapabilityManager.INSTANCE.register(
                IEnhancement.class,
                new WispEnhancementItem.CapabilityProviderEnhancementStorage(),
                ()->null);
    }

    public static class Capabilities
    {
        @CapabilityInject(IEnhancement.class)
        public static Capability<IEnhancement> CAPABILITY_ENHANCEMENT = null;
    }

    public static class Containers
    {
        public static ContainerType<BasicWispContainer> BASIC_WISP_CONTAINER;
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


        @SubscribeEvent
        public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
        {
            Containers.BASIC_WISP_CONTAINER = IForgeContainerType.create(BasicWispContainer::CreateContainer);
            Containers.BASIC_WISP_CONTAINER.setRegistryName("basic_wisp_container");
            event.getRegistry().register(Containers.BASIC_WISP_CONTAINER);
        }
    }

    public static class Blocks
    {
        public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, IgnesFatui.MOD_ID);

        public static final RegistryObject<Block> WISP_CORE = BLOCKS.register("wisp_core", WispCore::new );
        public static final RegistryObject<Block> WISP_CONNECTION_NODE  = BLOCKS.register("wisp_connection_node", WispConnectionNode::new );
    }

    public static class TileEntities
    {
        public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, IgnesFatui.MOD_ID);

        public static final RegistryObject<TileEntityType<WispCoreTileEntity>> WISP_CORE_TILE_ENTITY = TILE_ENTITIES.register("wisp_core",
                ()->{ return TileEntityType.Builder.create(WispCoreTileEntity::new, Blocks.WISP_CORE.get() ).build(null); });

        public static final RegistryObject<TileEntityType<WispConnectionNodeTileEntity>> WISP_CONNECTION_NODE_TILE_ENTITY = TILE_ENTITIES.register("wisp_connection_node",
                ()->{ return TileEntityType.Builder.create(WispConnectionNodeTileEntity::new, Blocks.WISP_CONNECTION_NODE.get() ).build(null); });
    }

    public static class Items
    {
        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IgnesFatui.MOD_ID);

        // Block Items
        public static final RegistryObject<Item> WISP_CORE_ITEM = ITEMS.register("wisp_core", ()->{ return new BlockItem( Blocks.WISP_CORE.get(), new Item.Properties().group(ItemGroup.MISC) ); });
        public static final RegistryObject<Item> WISP_CONNECTION_NODE_ITEM = ITEMS.register("wisp_connection_node", ()->{ return new BlockItem( Blocks.WISP_CONNECTION_NODE.get(), new Item.Properties().group(ItemGroup.MISC) ); });

        // Items
        public static final RegistryObject<Item> WISP_ITEM = ITEMS.register("wisp", BasicWispItem::new );
        public static final RegistryObject<Item> WISP_PROVIDER_ENHANCEMENT_ITEM = ITEMS.register("wisp_provider_enhancement", () -> new WispEnhancementItem(EnhancementTypes.PROVIDER) );

    }
}
