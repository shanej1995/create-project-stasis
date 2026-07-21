package com.shane.stasismod.player;

import com.shane.stasismod.StasisMod;
import com.shane.stasismod.item.StasisItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import net.minecraft.resources.ResourceLocation;

/**
 * Manages complete player state capture and restoration using direct field manipulation.
 * Also backs up full NBT (including Curios attachments) for complete restoration.
 *
 * Flow:
 * 1. captureState() - Copy all player fields to snapshot + backup full NBT for Curios
 * 2. prepareForSimulation() - Clear player inventory, effects, and Curios
 * 3. (player spends time in simulation)
 * 4. restoreFromSimulation() - Restore all fields from snapshot + Curios from NBT backup
 */
public class PlayerStateManager {
    private static final Map<UUID, PlayerStateSnapshot> savedStates = new HashMap<>();
    private static final Map<UUID, CompoundTag> curiosBackups = new HashMap<>();
    private static Path stateStorageDir = null;

    // Items that enable cross-dimensional exploits - removed on exit
    private static final Set<String> EXPLOIT_ITEM_BLACKLIST = Set.of(
            // AE2 - Quantum components
            "ae2:quantum_entangled_singularity",
            // Flux Networks - Energy transfer
            "fluxnetworks:flux_plug",
            "fluxnetworks:flux_point",
            "fluxnetworks:flux_core",
            // Waystones - Teleportation
            "waystones:waystone",
            "waystones:warp_scroll",
            // Immersive Portals
            "immersiveportals:end_frame",
            // Chunkloaders
            "ftbchunks:chunk_loader",
            "chunkloaders:chunk_loader",
            // Draconic Evolution
            "draconicevolution:draconic_warp_core",
            // Vanilla exploits
            "minecraft:command_block",
            "minecraft:structure_block"
    );

    /**
     * Captures the complete player state by directly copying all relevant fields.
     * Also backs up full NBT to capture Curios attachment data.
     *
     * @param player The player to capture
     * @return A snapshot of the player's state, or null if capture failed
     */
    public static PlayerStateSnapshot captureState(ServerPlayer player) {
        try {
            UUID playerUUID = player.getUUID();

            // Backup full player NBT for Curios restoration
            CompoundTag fullNBT = new CompoundTag();
            player.saveWithoutId(fullNBT);
            curiosBackups.put(playerUUID, fullNBT);
            StasisMod.LOGGER.info("Backed up full player NBT (with Curios) for {}", player.getName().getString());

            // Capture inventory (36 slots: 0-8 hotbar, 9-35 main inventory)
            ItemStack[] inventorySnapshot = new ItemStack[36];
            for (int i = 0; i < 36; i++) {
                ItemStack originalItem = player.getInventory().getItem(i);
                inventorySnapshot[i] = originalItem.isEmpty() ? ItemStack.EMPTY : originalItem.copy();
            }

            // Capture armor (4 slots: boots, leggings, chestplate, helmet)
            ItemStack[] armorSnapshot = new ItemStack[4];
            for (int i = 0; i < 4; i++) {
                ItemStack originalArmor = player.getInventory().getArmor(i);
                armorSnapshot[i] = originalArmor.isEmpty() ? ItemStack.EMPTY : originalArmor.copy();
            }

            // Capture offhand
            ItemStack offhandSnapshot = player.getInventory().getItem(40);
            if (offhandSnapshot.isEmpty()) {
                offhandSnapshot = ItemStack.EMPTY;
            } else {
                offhandSnapshot = offhandSnapshot.copy();
            }

            // Capture active effects
            List<MobEffectInstance> effectsSnapshot = new ArrayList<>();
            for (MobEffectInstance effect : player.getActiveEffects()) {
                effectsSnapshot.add(new MobEffectInstance(effect));
            }

            // Capture health and hunger
            float health = player.getHealth();
            int foodLevel = player.getFoodData().getFoodLevel();
            float foodSaturation = player.getFoodData().getSaturationLevel();
            float foodExhaustion = player.getFoodData().getExhaustionLevel();

            // Capture XP
            int xpLevel = player.experienceLevel;
            float xpProgress = player.experienceProgress;

            // Capture position and rotation
            Vec3 position = player.position();
            float yaw = player.getYRot();
            float pitch = player.getXRot();

            // Capture game mode
            GameType gameMode = player.gameMode.getGameModeForPlayer();

            // Create snapshot
            PlayerStateSnapshot snapshot = new PlayerStateSnapshot(
                    playerUUID,
                    inventorySnapshot,
                    armorSnapshot,
                    offhandSnapshot,
                    health,
                    foodLevel,
                    foodSaturation,
                    foodExhaustion,
                    xpLevel,
                    xpProgress,
                    effectsSnapshot,
                    gameMode,
                    position,
                    yaw,
                    pitch
            );

            StasisMod.LOGGER.info("Captured player state for {} (health: {}, inventory: {}, effects: {})",
                    player.getName().getString(),
                    health,
                    countNonEmptySlots(inventorySnapshot),
                    effectsSnapshot.size());

            return snapshot;

        } catch (Exception e) {
            StasisMod.LOGGER.error("Failed to capture player state", e);
            return null;
        }
    }

    /**
     * Initialize state storage directory during server startup.
     * Must be called from ServerStartedEvent or similar.
     */
    public static void initializeStorageDir(Path worldDir) {
        try {
            stateStorageDir = worldDir.resolve("stasis").resolve("player_states");
            Files.createDirectories(stateStorageDir);
            StasisMod.LOGGER.info("Initialized player state storage at {}", stateStorageDir);

            // Load any persisted states from disk
            loadPersistedStates();
        } catch (IOException e) {
            StasisMod.LOGGER.error("Failed to initialize player state storage directory", e);
        }
    }

    /**
     * Prepares a player for simulation entry by clearing their state and storing the snapshot.
     *
     * @param player The player entering simulation
     * @return true if preparation successful
     */
    public static boolean prepareForSimulation(ServerPlayer player) {
        try {
            UUID playerUUID = player.getUUID();

            // Step 1: Capture current state
            PlayerStateSnapshot snapshot = captureState(player);
            if (snapshot == null) {
                StasisMod.LOGGER.error("Failed to capture player state for {}", player.getName().getString());
                return false;
            }

            // Store snapshot for later restoration
            savedStates.put(playerUUID, snapshot);

            // Persist to disk immediately
            persistState(playerUUID, snapshot);
            persistCuriosBackup(playerUUID, curiosBackups.get(playerUUID));

            // Step 2: Reset player to fresh/empty state
            resetToFreshState(player);

            StasisMod.LOGGER.info("Player {} prepared for simulation (state saved and cleared)",
                    player.getName().getString());

            return true;

        } catch (Exception e) {
            StasisMod.LOGGER.error("Failed to prepare player for simulation", e);
            return false;
        }
    }

    /**
     * Resets a player to a fresh/empty state suitable for simulation.
     * This clears inventory, effects, health, hunger, XP, and Curios.
     *
     * @param player The player to reset
     */
    public static void resetToFreshState(ServerPlayer player) {
        try {
            // Clear inventory
            player.getInventory().clearContent();

            // Clear Curios using the /curios clear command
            try {
                player.getServer().getCommands().performPrefixedCommand(
                        player.createCommandSourceStack().withSuppressedOutput(),
                        "curios clear @s"
                );
                StasisMod.LOGGER.info("Cleared Curios from player {} via command", player.getName().getString());
            } catch (Exception e) {
                StasisMod.LOGGER.warn("Could not clear Curios from player via command", e);
            }

            // Clear all active effects
            player.removeAllEffects();

            // Reset health
            player.setHealth(20.0f);

            // Reset hunger
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(5.0f);
            player.getFoodData().setExhaustion(0.0f);

            // Reset XP
            player.setExperienceLevels(0);
            player.experienceProgress = 0.0f;

            // Switch to Creative mode
            player.setGameMode(GameType.CREATIVE);

            // Give player the exit beacon
            ItemStack exitBeacon = new ItemStack(StasisItems.EXIT_BEACON.get(), 1);
            player.getInventory().add(exitBeacon);

            StasisMod.LOGGER.info("Reset player {} to fresh state and gave exit beacon", player.getName().getString());

        } catch (Exception e) {
            StasisMod.LOGGER.error("Failed to reset player to fresh state", e);
        }
    }

    /**
     * Restores a player from a previously captured snapshot.
     * Applies all saved state: inventory, effects, health, position, etc.
     * Also restores Curios from the backed-up NBT.
     *
     * @param player The player to restore
     * @return true if restoration successful
     */
    public static boolean restoreFromSimulation(ServerPlayer player) {
        try {
            UUID playerUUID = player.getUUID();

            // Get the saved snapshot (DON'T remove yet - need it for teleport event check)
            PlayerStateSnapshot snapshot = savedStates.get(playerUUID);
            if (snapshot == null) {
                StasisMod.LOGGER.error("No saved state for player {}", playerUUID);
                return false;
            }

            // Get the Curios backup NBT (DON'T remove yet)
            CompoundTag curiosBackup = curiosBackups.get(playerUUID);

            // Restore everything from snapshot first
            player.getInventory().clearContent();
            for (int i = 0; i < Math.min(36, snapshot.mainInventory.length); i++) {
                ItemStack item = snapshot.mainInventory[i];
                if (!item.isEmpty()) {
                    player.getInventory().setItem(i, item.copy());
                }
            }

            // Restore armor (slots 36-39 in inventory)
            for (int i = 0; i < 4 && i < snapshot.armor.length; i++) {
                ItemStack armorItem = snapshot.armor[i];
                if (!armorItem.isEmpty()) {
                    player.getInventory().setItem(36 + i, armorItem.copy());
                }
            }

            // Restore offhand
            if (!snapshot.offhand.isEmpty()) {
                player.getInventory().setItem(40, snapshot.offhand.copy());
            }

            // Restore effects
            player.removeAllEffects();
            for (MobEffectInstance effect : snapshot.activeEffects) {
                player.addEffect(new MobEffectInstance(effect));
            }

            // Restore health and hunger
            player.setHealth(snapshot.health);
            player.getFoodData().setFoodLevel(snapshot.foodLevel);
            player.getFoodData().setSaturation(snapshot.foodSaturation);
            player.getFoodData().setExhaustion(snapshot.foodExhaustion);

            // Restore XP
            player.setExperienceLevels(snapshot.xpLevel);
            player.experienceProgress = snapshot.xpProgress;

            // Restore game mode
            player.setGameMode(snapshot.gameMode);

            // Restore from backup NBT (Curios + inventory if snapshot is empty)
            if (curiosBackup != null) {
                try {
                    // Get current state
                    CompoundTag currentNBT = new CompoundTag();
                    player.saveWithoutId(currentNBT);

                    // If snapshot inventory is empty, restore from backup NBT
                    if (countNonEmptySlots(snapshot.mainInventory) == 0 && curiosBackup.contains("Inventory")) {
                        currentNBT.put("Inventory", curiosBackup.get("Inventory"));
                        StasisMod.LOGGER.info("Restored inventory for player {} from backup NBT", player.getName().getString());
                    }

                    // Copy attachment data from backup (Curios)
                    if (curiosBackup.contains("neoforge:attachments")) {
                        currentNBT.put("neoforge:attachments", curiosBackup.get("neoforge:attachments"));
                        StasisMod.LOGGER.info("Restoring Curios attachments for player {}", player.getName().getString());
                    }

                    // Load the merged NBT to apply everything back
                    player.load(currentNBT);
                } catch (Exception e) {
                    StasisMod.LOGGER.warn("Could not restore full state from backup NBT", e);
                }
            }

            // Restore position - teleport to OVERWORLD, not current dimension
            ServerLevel overworld = player.getServer().overworld();
            StasisMod.LOGGER.info("Teleporting player {} to overworld at ({}, {}, {})",
                    player.getName().getString(),
                    snapshot.position.x,
                    snapshot.position.y,
                    snapshot.position.z);
            player.teleportTo(
                    overworld,
                    snapshot.position.x,
                    snapshot.position.y,
                    snapshot.position.z,
                    snapshot.yaw,
                    snapshot.pitch
            );
            StasisMod.LOGGER.info("Teleport completed for player {}", player.getName().getString());

            // Remove exploit items that could be used to abuse outside simulation
            removeExploitItems(player);

            // NOW remove the saved states after teleport is complete
            savedStates.remove(playerUUID);
            curiosBackups.remove(playerUUID);

            // Delete persisted state files from disk
            deletePersistedState(playerUUID);

            long elapsed = System.currentTimeMillis() - snapshot.capturedAt;
            StasisMod.LOGGER.info("Restored player {} from simulation (elapsed: {}ms, items: {}, effects: {})",
                    player.getName().getString(),
                    elapsed,
                    countNonEmptySlots(snapshot.mainInventory),
                    snapshot.activeEffects.size());

            return true;

        } catch (Exception e) {
            StasisMod.LOGGER.error("Failed to restore player from simulation", e);
            return false;
        }
    }

    /**
     * Checks if a saved state exists for a player.
     */
    public static boolean hasSavedState(UUID playerUUID) {
        return savedStates.containsKey(playerUUID);
    }

    /**
     * Gets a saved state without removing it (for inspection).
     */
    public static PlayerStateSnapshot getSavedState(UUID playerUUID) {
        return savedStates.get(playerUUID);
    }

    /**
     * Removes a saved state (if player disconnects permanently).
     */
    public static void clearSavedState(UUID playerUUID) {
        savedStates.remove(playerUUID);
        curiosBackups.remove(playerUUID);
    }

    /**
     * Gets all currently saved player UUIDs.
     */
    public static Set<UUID> getSavedPlayerUUIDs() {
        return new HashSet<>(savedStates.keySet());
    }

    /**
     * Clears all saved states (for server shutdown, etc).
     */
    public static void clearAllStates() {
        savedStates.clear();
        curiosBackups.clear();
        StasisMod.LOGGER.info("Cleared all player state snapshots");
    }

    // ===== HELPER METHODS =====

    private static int countNonEmptySlots(ItemStack[] inventory) {
        int count = 0;
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Remove exploit items from player inventory when exiting simulation.
     * Items that enable cross-dimensional abuse are blacklisted and removed.
     */
    private static void removeExploitItems(ServerPlayer player) {
        int removed = 0;
        ItemStack[] inventory = player.getInventory().items.toArray(new ItemStack[0]);

        for (int i = 0; i < inventory.length; i++) {
            ItemStack stack = inventory[i];
            if (!stack.isEmpty()) {
                String itemId = stack.getItem().builtInRegistryHolder().key().location().toString();

                if (EXPLOIT_ITEM_BLACKLIST.contains(itemId)) {
                    StasisMod.LOGGER.info("Removing exploit item {} from player {}",
                            itemId, player.getName().getString());
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                    removed++;
                }
            }
        }

        if (removed > 0) {
            StasisMod.LOGGER.info("Removed {} exploit items from exiting player {}",
                    removed, player.getName().getString());
        }
    }

    // ===== DISK PERSISTENCE =====

    /**
     * Persist a player state snapshot to disk so it survives server restart.
     * Includes retry logic (up to 3 attempts) and verification that file was actually written.
     */
    private static void persistState(UUID playerUUID, PlayerStateSnapshot snapshot) {
        if (stateStorageDir == null) {
            StasisMod.LOGGER.warn("State storage dir not initialized, cannot persist state");
            return;
        }

        Path stateFile = stateStorageDir.resolve(playerUUID + "_snapshot.dat");
        CompoundTag tag = snapshot.serialize();

        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                NbtIo.writeCompressed(tag, stateFile);

                // Verify file was written and is not empty
                if (!Files.exists(stateFile)) {
                    throw new IOException("File was not created after write");
                }

                long fileSize = Files.size(stateFile);
                if (fileSize == 0) {
                    throw new IOException("File was created but is empty");
                }

                StasisMod.LOGGER.info("Persisted player state for {} to disk (size: {} bytes)", playerUUID, fileSize);
                return;  // Success - exit immediately

            } catch (IOException e) {
                if (attempt < maxAttempts) {
                    StasisMod.LOGGER.warn("Failed to persist player state (attempt {}/{}), retrying...", attempt, maxAttempts, e);
                    try {
                        Thread.sleep(100);  // Brief delay before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    StasisMod.LOGGER.error("Failed to persist player state after {} attempts", maxAttempts, e);
                    throw new RuntimeException("Failed to persist player state for " + playerUUID + ": " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Persist Curios backup NBT to disk.
     * Includes retry logic (up to 3 attempts) and verification that file was actually written.
     */
    private static void persistCuriosBackup(UUID playerUUID, CompoundTag curiosBackup) {
        if (stateStorageDir == null || curiosBackup == null) {
            return;
        }

        Path backupFile = stateStorageDir.resolve(playerUUID + "_curios.dat");

        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                NbtIo.writeCompressed(curiosBackup, backupFile);

                // Verify file was written and is not empty
                if (!Files.exists(backupFile)) {
                    throw new IOException("Backup file was not created after write");
                }

                long fileSize = Files.size(backupFile);
                if (fileSize == 0) {
                    throw new IOException("Backup file was created but is empty");
                }

                StasisMod.LOGGER.info("Persisted Curios backup for {} to disk (size: {} bytes)", playerUUID, fileSize);
                return;  // Success - exit immediately

            } catch (IOException e) {
                if (attempt < maxAttempts) {
                    StasisMod.LOGGER.warn("Failed to persist Curios backup (attempt {}/{}), retrying...", attempt, maxAttempts, e);
                    try {
                        Thread.sleep(100);  // Brief delay before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    StasisMod.LOGGER.error("Failed to persist Curios backup after {} attempts", maxAttempts, e);
                    throw new RuntimeException("Failed to persist Curios backup for " + playerUUID + ": " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Load all persisted player states from disk into memory on server startup.
     */
    private static void loadPersistedStates() {
        if (stateStorageDir == null || !Files.exists(stateStorageDir)) {
            return;
        }

        try {
            Files.list(stateStorageDir)
                    .filter(path -> path.getFileName().toString().endsWith("_snapshot.dat"))
                    .forEach(stateFile -> {
                        try {
                            String fileName = stateFile.getFileName().toString();
                            UUID playerUUID = UUID.fromString(fileName.replace("_snapshot.dat", ""));
                            CompoundTag tag = NbtIo.readCompressed(stateFile, NbtAccounter.unlimitedHeap());
                            PlayerStateSnapshot snapshot = PlayerStateSnapshot.deserialize(tag);

                            if (snapshot != null) {
                                savedStates.put(playerUUID, snapshot);
                                StasisMod.LOGGER.info("Loaded persisted state for player {}", playerUUID);
                            }
                        } catch (Exception e) {
                            StasisMod.LOGGER.error("Failed to load persisted state from {}", stateFile, e);
                        }
                    });

            // Load Curios backups
            Files.list(stateStorageDir)
                    .filter(path -> path.getFileName().toString().endsWith("_curios.dat"))
                    .forEach(backupFile -> {
                        try {
                            String fileName = backupFile.getFileName().toString();
                            UUID playerUUID = UUID.fromString(fileName.replace("_curios.dat", ""));
                            CompoundTag tag = NbtIo.readCompressed(backupFile, NbtAccounter.unlimitedHeap());
                            curiosBackups.put(playerUUID, tag);
                            StasisMod.LOGGER.info("Loaded persisted Curios backup for player {}", playerUUID);
                        } catch (Exception e) {
                            StasisMod.LOGGER.error("Failed to load persisted Curios backup from {}", backupFile, e);
                        }
                    });
        } catch (IOException e) {
            StasisMod.LOGGER.error("Failed to load persisted player states from disk", e);
        }
    }

    /**
     * Delete persisted state files after successful restoration.
     */
    private static void deletePersistedState(UUID playerUUID) {
        if (stateStorageDir == null) {
            return;
        }

        try {
            Path stateFile = stateStorageDir.resolve(playerUUID + "_snapshot.dat");
            Path backupFile = stateStorageDir.resolve(playerUUID + "_curios.dat");
            Files.deleteIfExists(stateFile);
            Files.deleteIfExists(backupFile);
            StasisMod.LOGGER.info("Deleted persisted state files for player {}", playerUUID);
        } catch (IOException e) {
            StasisMod.LOGGER.error("Failed to delete persisted state files for player {}", playerUUID, e);
        }
    }
}
