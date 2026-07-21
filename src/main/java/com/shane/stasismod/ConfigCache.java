package com.shane.stasismod;

/**
 * Cached config values - populated when config loads/changes.
 * These are the values actually used at runtime.
 */
public class ConfigCache {
    public static int CHAMBER_CAPACITY = 100000;
    public static int CHAMBER_ACTIVATION_COST = 2000;
    public static int CHAMBER_MAX_TRANSFER = 500;

    private ConfigCache() {}
}
