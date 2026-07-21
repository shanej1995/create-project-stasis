package com.shane.stasismod.player;

import com.shane.stasismod.StasisMod;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory cache for player serialized states.
 * Holds snapshots while players are in simulation.
 */
public class PlayerDataCache {
    private static final PlayerDataCache INSTANCE = new PlayerDataCache();
    private final Map<UUID, CachedPlayerData> cache = new HashMap<>();

    public static PlayerDataCache getInstance() {
        return INSTANCE;
    }

    /**
     * Stores a player snapshot in cache.
     */
    public void cachePlayerSnapshot(UUID playerUUID, CompoundTag snapshot) {
        CachedPlayerData data = new CachedPlayerData(snapshot, System.currentTimeMillis());
        cache.put(playerUUID, data);

        StasisMod.LOGGER.debug("Cached player snapshot for {}", playerUUID);
    }

    /**
     * Retrieves a cached snapshot.
     */
    public CompoundTag getPlayerSnapshot(UUID playerUUID) {
        CachedPlayerData data = cache.get(playerUUID);
        if (data == null) {
            return null;
        }

        return data.snapshot;
    }

    /**
     * Checks if a snapshot exists in cache.
     */
    public boolean hasSnapshot(UUID playerUUID) {
        return cache.containsKey(playerUUID);
    }

    /**
     * Removes a snapshot from cache after use.
     */
    public void removeSnapshot(UUID playerUUID) {
        cache.remove(playerUUID);
        StasisMod.LOGGER.debug("Removed cached snapshot for {}", playerUUID);
    }

    /**
     * Clears all cached data.
     */
    public void clearCache() {
        cache.clear();
        StasisMod.LOGGER.debug("Cleared player snapshot cache");
    }

    /**
     * Gets all cached player UUIDs.
     */
    public java.util.Set<UUID> getCachedPlayers() {
        return new java.util.HashSet<>(cache.keySet());
    }

    private static class CachedPlayerData {
        final CompoundTag snapshot;
        final long cachedAt;

        CachedPlayerData(CompoundTag snapshot, long cachedAt) {
            this.snapshot = snapshot.copy();
            this.cachedAt = cachedAt;
        }
    }
}
