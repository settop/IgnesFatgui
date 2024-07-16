package settop.IgnesFatui;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import settop.IgnesFatui.BlockEntities.WispNodeBlockEntity;
import settop.IgnesFatui.Blocks.WispNodeBlock;
import settop.IgnesFatui.Capabilities.ExternalWispNodeCapabilityProvider;
import settop.IgnesFatui.Client.Screen.WispNodeScreen;
import settop.IgnesFatui.Client.Tooltip.ItemTooltip;
import settop.IgnesFatui.Client.Tooltip.ClientItemTooltip;
import settop.IgnesFatui.Items.DirectionalBlockItem;
import settop.IgnesFatui.Items.WispExternalNodeItem;
import settop.IgnesFatui.Items.WispStaff;
import settop.IgnesFatui.Menu.ExternalWispNodeMenu;
import settop.IgnesFatui.Menu.WispNodeMenu;
import settop.IgnesFatui.Menu.WispStaffMenuContainer;
import settop.IgnesFatui.Network.PacketHandler;

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
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientTooltipSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::capabilitiesSetup);

        Blocks.BLOCKS.register( FMLJavaModLoadingContext.get().getModEventBus() );
        BlockEntities.BLOCK_ENTITIES.register( FMLJavaModLoadingContext.get().getModEventBus() );
        Items.ITEMS.register( FMLJavaModLoadingContext.get().getModEventBus() );
        ContainerMenus.MENUS.register( FMLJavaModLoadingContext.get().getModEventBus() );
        DataComponents.COMPONENTS.register( FMLJavaModLoadingContext.get().getModEventBus() );
        CreativeTab.CREATIVE_TABS.register( FMLJavaModLoadingContext.get().getModEventBus() );

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(IgnesFatguiServerEvents.class);
        MinecraftForge.EVENT_BUS.register(ExternalWispNodeCapabilityProvider.class);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        PacketHandler.Register();
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

    private void clientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(
            () ->
            {
                MenuScreens.register(ContainerMenus.WISP_STAFF_MENU.get(), ContainerScreen::new);
                MenuScreens.register(ContainerMenus.WISP_NODE_MENU.get(), WispNodeScreen::new);
                //MenuScreens.register(ContainerMenus.EXTERNAL_WISP_NODE_MENU.get(), ContainerScreen::new);
            }
        );
    }

    private void clientTooltipSetup(RegisterClientTooltipComponentFactoriesEvent event)
    {
        event.register(ItemTooltip.class, ClientItemTooltip::new);
    }

    private void capabilitiesSetup(RegisterCapabilitiesEvent registerCaps)
    {
        registerCaps.register(ExternalWispNodeCapabilityProvider.Cap.class);
    }

    public static class Capabilities
    {
        public static final Capability<ExternalWispNodeCapabilityProvider.Cap> EXTERNAL_WISP_NODE_HANDLER = CapabilityManager.get(new CapabilityToken<ExternalWispNodeCapabilityProvider.Cap>() {
        });
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


        public static boolean IsPartOfMod(BlockEntityType<?> type)
        {
            return WISP_NODE_BLOCK_ENTITY.get() == type;
        }
    }

    public static class Items
    {
        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IgnesFatui.MOD_ID);

        // Block Items
        //public static final RegistryObject<Item> WISP_CORE_ITEM = ITEMS.register("wisp_core", ()->{ return new BlockItem( Blocks.WISP_CORE.get(), new Item.Properties()/*.group(ItemGroup.MISC)*/ ); });
        public static final RegistryObject<Item> WISP_NODE_ITEM = ITEMS.register("wisp_node", ()->{ return new DirectionalBlockItem( Blocks.WISP_NODE.get(), new Item.Properties()/*.group(ItemGroup.MISC)*/, WispNodeBlock.FACING ); });

        // Items
        public static final RegistryObject<Item> WISP_STAFF = ITEMS.register("wisp_staff", ()->new WispStaff(new Item.Properties().stacksTo(1)) );
        public static final RegistryObject<Item> WISP_EXTERNAL_NODE = ITEMS.register("wisp_external_node", ()->new WispExternalNodeItem(new Item.Properties().stacksTo(64)) );
        //public static final RegistryObject<Item> WISP_PROVIDER_ENHANCEMENT_ITEM = ITEMS.register("wisp_provider_enhancement", () -> new WispEnhancementItem(EnhancementTypes.PROVIDER) );

    }

    public static class ContainerMenus
    {
        public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, IgnesFatui.MOD_ID);

        public static final RegistryObject<MenuType<WispStaffMenuContainer>> WISP_STAFF_MENU = MENUS.register
        (
                "wisp_staff_menu",
                () -> new MenuType<>(WispStaffMenuContainer::CreateMenuClient, FeatureFlags.DEFAULT_FLAGS)
        );
        public static final RegistryObject<MenuType<WispNodeMenu>> WISP_NODE_MENU = MENUS.register
        (
                "wisp_node_menu",
                () -> IForgeMenuType.create(WispNodeMenu::CreateMenuClient)
        );
        public static final RegistryObject<MenuType<ExternalWispNodeMenu>> EXTERNAL_WISP_NODE_MENU = MENUS.register
        (
                "external_wisp_node_menu",
                () -> new MenuType<>(ExternalWispNodeMenu::CreateMenuClient, FeatureFlags.DEFAULT_FLAGS)
        );
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
            output.accept(Items.WISP_EXTERNAL_NODE.get());
          })
          .build()
        );
    }
}
