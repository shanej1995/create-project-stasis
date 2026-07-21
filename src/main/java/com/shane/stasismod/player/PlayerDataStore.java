package com.shane.stasismod.player;

import com.shane.stasismod.StasisMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Persists player snapshots to disk.
 * Stores snapshots in world/stasis/players/ directory.
 */
public class PlayerDataStore {
    private final Path playersDir;

    public PlayerDataStore(Path worldDir) {
        this.playersDir = worldDir.resolve("stasis").resolve("players");
        try {
            Files.createDirectories(playersDir);
        } catch (IOException e) {
            StasisMod.LOGGER.error("Failed to create players directory", e);
        }
    }

    /**
     * Saves a player snapshot to disk.
     */
    public void savePlayerSnapshot(UUID playerUUID, CompoundTag snapshot) throws IOException {
        Path snapshotFile = getSnapshotPath(playerUUID);
        Path lockFile = getLockPath(playerUUID);

        try {
            // Atomic write with lock file
            Files.createDirectories(snapshotFile.getParent());
            NbtIo.writeCompressed(snapshot, snapshotFile);

            // Mark as saved
            Files.writeString(lockFile, "");

            StasisMod.LOGGER.debug("Saved player snapshot for {}", playerUUID);
        } catch (IOException e) {
            StasisMod.LOGGER.error("Failed to save snapshot for {}", playerUUID, e);
            throw e;
        }
    }

    /**
     * Loads a player snapshot from disk.
     */
    public CompoundTag loadPlayerSnapshot(UUID playerUUID) throws IOException {
        Path snapshotFile = getSnapshotPath(playerUUID);

        if (!Files.exists(snapshotFile)) {
            throw new IOException("Snapshot not found: " + playerUUID);
        }

        try {
            CompoundTag tag = NbtIo.readCompressed(snapshotFile, new net.minecraft.nbt.NbtAccounter(Long.MAX_VALUE, 512));

            StasisMod.LOGGER.debug("Loaded player snapshot for {}", playerUUID);
            return tag;
        } catch (IOException e) {
            StasisMod.LOGGER.error("Failed to load snapshot for {}", playerUUID, e);
            throw e;
        }
    }

    /**
     * Checks if a snapshot exists.
     */
    public boolean hasSnapshot(UUID playerUUID) {
        return Files.exists(getSnapshotPath(playerUUID));
    }

    /**
     * Deletes a player snapshot.
     */
    public void deleteSnapshot(UUID playerUUID) throws IOException {
        Path snapshotFile = getSnapshotPath(playerUUID);
        Path lockFile = getLockPath(playerUUID);

        try {
            Files.deleteIfExists(snapshotFile);
            Files.deleteIfExists(lockFile);

            StasisMod.LOGGER.debug("Deleted player snapshot for {}", playerUUID);
        } catch (IOException e) {
            StasisMod.LOGGER.error("Failed to delete snapshot for {}", playerUUID, e);
            throw e;
        }
    }

    /**
     * Creates a backup copy of a snapshot.
     */
    public void backupSnapshot(UUID playerUUID) throws IOException {
        Path snapshotFile = getSnapshotPath(playerUUID);
        Path backupFile = getBackupPath(playerUUID);

        if (!Files.exists(snapshotFile)) {
            throw new IOException("Snapshot not found: " + playerUUID);
        }

        try {
            Files.createDirectories(backupFile.getParent());
            Files.copy(snapshotFile, backupFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            StasisMod.LOGGER.debug("Created backup for {}", playerUUID);
        } catch (IOException e) {
            StasisMod.LOGGER.error("Failed to backup snapshot for {}", playerUUID, e);
            throw e;
        }
    }

    private Path getSnapshotPath(UUID playerUUID) {
        return playersDir.resolve(playerUUID.toString() + ".nbt");
    }

    private Path getLockPath(UUID playerUUID) {
        return playersDir.resolve(playerUUID.toString() + ".lock");
    }

    private Path getBackupPath(UUID playerUUID) {
        return playersDir.resolve("backups").resolve(playerUUID.toString() + ".nbt.backup");
    }
}
