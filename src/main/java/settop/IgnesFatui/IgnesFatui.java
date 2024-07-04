package settop.IgnesFatui;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settop.IgnesFatui.BlockEntities.WispNodeBlockEntity;
import settop.IgnesFatui.Blocks.WispNodeBlock;
import settop.IgnesFatui.Items.DirectionalBlockItem;
import settop.IgnesFatui.Items.WispStaff;

import java.util.Optional;

@Mod("sif1")
public class IgnesFatui
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "sif1";
    public static final String MULTI_SCREEN_CHANNEL_VERSION = "1.0.0";
    //public static final SimpleChannel MULTI_SCREEN_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(MOD_ID, "multi_screen"), () -> MULTI_SCREEN_CHANNEL_VERSION,
    //        MULTI_SCREEN_CHANNEL_VERSION::equals,
    //        MULTI_SCREEN_CHANNEL_VERSION::equals);

    public IgnesFatui()
    {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);


        Blocks.BLOCKS.register( FMLJavaModLoadingContext.get().getModEventBus() );
        BlockEntities.BLOCK_ENTITIES.register( FMLJavaModLoadingContext.get().getModEventBus() );
        Items.ITEMS.register( FMLJavaModLoadingContext.get().getModEventBus() );
        DataComponents.COMPONENTS.register( FMLJavaModLoadingContext.get().getModEventBus() );
        CreativeTab.CREATIVE_TABS.register( FMLJavaModLoadingContext.get().getModEventBus() );

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        /*
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

         */
    }

    public static class Capabilities
    {
        //@CapabilityInject(IEnhancement.class)
        //public static Capability<IEnhancement> CAPABILITY_ENHANCEMENT = null;
    }

    public static class Containers
    {
        //public static ContainerType<BasicWispContainer> BASIC_WISP_CONTAINER;
    }


    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents
    {
        /*
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent)
        {
            //blockRegistryEvent.getRegistry().registerAll
                    (
                            testBlock
                    );
        }

        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent)
        {
            //itemRegistryEvent.getRegistry().registerAll
                    (
                            new BlockItem( testBlock, new Item.Properties().group(ItemGroup.MISC) )
                    );
        }


        @SubscribeEvent
        public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
        {
            Containers.BASIC_WISP_CONTAINER = IForgeContainerType.create(BasicWispContainer::CreateContainer);
            Containers.BASIC_WISP_CONTAINER.setRegistryName("basic_wisp_container");
            event.getRegistry().register(Containers.BASIC_WISP_CONTAINER);
        }
        */
    }

    public static class Blocks
    {
        public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, IgnesFatui.MOD_ID);

       // public static final RegistryObject<Block> WISP_CORE = BLOCKS.register("wisp_core", WispCore::new );
        public static final RegistryObject<Block> WISP_NODE  = BLOCKS.register("wisp_node", WispNodeBlock::new );
    }

    public static class BlockEntities
    {
        public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, IgnesFatui.MOD_ID);

        /*
        public static final RegistryObject<BlockEntityType<WispCoreTileEntity>> WISP_CORE_TILE_ENTITY = BLOCK_ENTITIES.register("wisp_core",
                ()->{ return BlockEntityType.Builder.of(WispCoreTileEntity::new, Blocks.WISP_CORE.get() ).build(null); });
        */
        public static final RegistryObject<BlockEntityType<WispNodeBlockEntity>> WISP_NODE_BLOCK_ENTITY = BLOCK_ENTITIES.register("wisp_node",
                ()->{ return BlockEntityType.Builder.of(WispNodeBlockEntity::new, Blocks.WISP_NODE.get()).build(null); });


    }

    public static class Items
    {
        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IgnesFatui.MOD_ID);

        // Block Items
        //public static final RegistryObject<Item> WISP_CORE_ITEM = ITEMS.register("wisp_core", ()->{ return new BlockItem( Blocks.WISP_CORE.get(), new Item.Properties()/*.group(ItemGroup.MISC)*/ ); });
        public static final RegistryObject<Item> WISP_NODE_ITEM = ITEMS.register("wisp_node", ()->{ return new DirectionalBlockItem( Blocks.WISP_NODE.get(), new Item.Properties()/*.group(ItemGroup.MISC)*/, WispNodeBlock.FACING ); });

        // Items
        public static final RegistryObject<Item> WISP_STAFF = ITEMS.register("wisp_staff", ()->new WispStaff(new Item.Properties().stacksTo(1)) );
        //public static final RegistryObject<Item> WISP_PROVIDER_ENHANCEMENT_ITEM = ITEMS.register("wisp_provider_enhancement", () -> new WispEnhancementItem(EnhancementTypes.PROVIDER) );

    }

    public static class DataComponents
    {
        public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MOD_ID);

        public static final RegistryObject<DataComponentType<GlobalPos>> BOUND_GLOBAL_POS = COMPONENTS.register
        (
                "bound_global_pos",
                () -> DataComponentType.<GlobalPos>builder()
                        .persistent(GlobalPos.CODEC)
                        .networkSynchronized(GlobalPos.STREAM_CODEC)
                        .build()
        );

    }

    public static class CreativeTab
    {
        public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

        public static final RegistryObject<CreativeModeTab> WISPS_TAB = CREATIVE_TABS.register("example", () -> CreativeModeTab.builder()
          // Set name of tab to display
          .title(Component.translatable("item_group." + MOD_ID + ".wisps"))
          // Set icon of creative tab
          .icon(() -> new ItemStack(Items.WISP_STAFF.get()))
          // Add default items to tab
          .displayItems((params, output) -> {
            output.accept(Items.WISP_STAFF.get());
            output.accept(Items.WISP_NODE_ITEM.get());
          })
          .build()
        );
    }
}
