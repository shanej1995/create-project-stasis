package com.shane.stasismod;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.shane.stasismod.block.ChamberBlocks;
import com.shane.stasismod.blockentity.ChamberBlockEntities;
import com.shane.stasismod.dimension.CheckerboardLevelSource;
import com.shane.stasismod.dimension.StasisDimensions;
import com.shane.stasismod.item.StasisItems;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(StasisMod.MODID)
public class StasisMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "stasismod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "stasismod" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "stasismod" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "stasismod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Stasis Mod creative tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> STASIS_TAB = CREATIVE_MODE_TABS.register("stasis_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.stasismod"))
            .withTabsBefore(CreativeModeTabs.TOOLS_AND_UTILITIES)
            .icon(() -> ChamberBlocks.ACTIVATION_CORE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ChamberBlocks.ACTIVATION_CORE_ITEM);
                output.accept(StasisItems.EXIT_BEACON);
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public StasisMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register chamber blocks and items
        ChamberBlocks.BLOCKS.register(modEventBus);
        ChamberBlocks.ITEMS.register(modEventBus);

        // Register stasis items
        StasisItems.ITEMS.register(modEventBus);

        // Register block entities
        ChamberBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        // Register dimension types
        StasisDimensions.DIMENSION_TYPES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (StasisMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::registerCodecs);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        // Use SERVER type so only server can edit these settings
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Stasis Mod initialized");
    }

    private void registerCodecs(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.CHUNK_GENERATOR)) {
            event.register(Registries.CHUNK_GENERATOR, registry -> {
                registry.register(modLoc("checkerboard"), CheckerboardLevelSource.CODEC);
            });
        }
    }

    // Mod items are added directly to STASIS_TAB, no need to add via event
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    // Helper method to create mod resource locations
    public static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
