package com.shane.stasismod.config;

import com.shane.stasismod.StasisMod;

/**
 * Caches config values for safe runtime access.
 * Updated on ModConfigEvent.Reloading to stay in sync.
 */
public class ConfigCache {
    // Default values (used if config isn't loaded)
    private static int chamberCapacity = 100000;
    private static int activationCost = 10000;
    private static int maxTransferRate = 500;

    public static void reload() {
        try {
            chamberCapacity = ServerConfig.CHAMBER_CAPACITY.get();
            activationCost = ServerConfig.ACTIVATION_COST.get();
            maxTransferRate = ServerConfig.MAX_TRANSFER_RATE.get();
            StasisMod.LOGGER.info("Config reloaded: capacity={}, activation={}, transfer={}",
                chamberCapacity, activationCost, maxTransferRate);
        } catch (Exception e) {
            StasisMod.LOGGER.error("Failed to reload config, using defaults", e);
        }
    }

    public static int getChamberCapacity() {
        return chamberCapacity;
    }

    public static int getActivationCost() {
        return activationCost;
    }

    public static int getMaxTransferRate() {
        return maxTransferRate;
    }
}
