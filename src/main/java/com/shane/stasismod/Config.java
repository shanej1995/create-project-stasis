package com.shane.stasismod;

import com.shane.stasismod.config.ConfigCache;
import com.shane.stasismod.config.ServerConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    // Re-export ServerConfig spec for registration in StasisMod
    static final ModConfigSpec SPEC = ServerConfig.SPEC;

    /**
     * Event listener to sync config values to cache when config is reloaded.
     */
    @EventBusSubscriber(modid = StasisMod.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class ConfigEvents {
        @SubscribeEvent
        public static void onLoad(final ModConfigEvent.Loading event) {
            if (event.getConfig().getModId().equals(StasisMod.MODID)) {
                ConfigCache.reload();
            }
        }

        @SubscribeEvent
        public static void onReload(final ModConfigEvent.Reloading event) {
            if (event.getConfig().getModId().equals(StasisMod.MODID)) {
                ConfigCache.reload();
            }
        }
    }
}
