package com.shane.stasismod.event;

import com.shane.stasismod.StasisMod;
import com.shane.stasismod.block.ChamberBlocks;
import com.shane.stasismod.command.StasisCommand;
import com.shane.stasismod.dimension.StasisDimensions;
import com.shane.stasismod.player.PlayerStateManager;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = StasisMod.MODID)
public class ServerEvents {
    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();

        // Check if simulation dimension exists, if not create it
        ServerLevel simulationLevel = server.getLevel(StasisDimensions.SIMULATION_LEVEL);

        if (simulationLevel == null) {
            StasisMod.LOGGER.info("Creating Stasis simulation dimension");
            // Dimension will be created by normal world save/load cycle
        } else {
            StasisMod.LOGGER.info("Stasis simulation dimension loaded successfully");
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        StasisCommand.register(event.getDispatcher());
    }

    /**
     * Prevent all mobs from spawning in the Stasis simulation dimension.
     * Time and weather are controlled by SimulationDimensionEffects (no per-tick overhead).
     */
    @SubscribeEvent
    public static void onMobSpawn(FinalizeSpawnEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (serverLevel.dimension() == StasisDimensions.SIMULATION_LEVEL) {
                event.setSpawnCancelled(true);
            }
        }
    }

    /**
     * On server start, verify dimension is loaded and initialize player state storage.
     */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        ServerLevel simulationLevel = server.getLevel(StasisDimensions.SIMULATION_LEVEL);

        if (simulationLevel != null) {
            StasisMod.LOGGER.info("Stasis simulation dimension loaded: {}",
                simulationLevel.dimension().location());
        }

        // Initialize player state storage and load persisted states
        // Use a directory relative to the server's root
        java.nio.file.Path stateDir = java.nio.file.Paths.get(".").toAbsolutePath().resolve("stasis_player_states");
        PlayerStateManager.initializeStorageDir(stateDir);
    }

    /**
     * Handle player login - if they're in simulation dimension with a saved state, restore them.
     * This fixes the issue where quitting in the simulation and rejoining leaves them stuck.
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Check if player is in simulation dimension
            if (player.level().dimension() == StasisDimensions.SIMULATION_LEVEL) {
                // Check if there's a saved state for this player
                if (PlayerStateManager.hasSavedState(player.getUUID())) {
                    // Restore them from the saved state
                    StasisMod.LOGGER.info("Player {} rejoined in simulation dimension, restoring...",
                        player.getName().getString());
                    PlayerStateManager.restoreFromSimulation(player);
                } else {
                    // No saved state - shouldn't happen, but teleport them to overworld as fallback
                    StasisMod.LOGGER.warn("Player {} in simulation but no saved state, teleporting to overworld",
                        player.getName().getString());
                    ServerLevel overworld = player.getServer().overworld();
                    player.teleportTo(overworld, 0, 64, 0, 0, 0);
                }
            }
        }
    }

    /**
     * Handle player disconnect - preserve chamber state if they disconnect while in simulation.
     */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.level().dimension() == StasisDimensions.SIMULATION_LEVEL) {
                StasisMod.LOGGER.info("Player {} disconnected while in simulation - state preserved for recovery",
                    player.getName().getString());
                // State remains in PlayerStateManager and on disk - will be recovered on rejoin
                // Chamber remains in OCCUPIED state - will be cleared when player rejoins and exits
            }
        }
    }

    /**
     * Handle player login after disconnect - recover if crashed during exit sequence.
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // If player has a saved state but isn't in simulation, they may have crashed during exit
            // The exit sequence should complete when they rejoin
            if (PlayerStateManager.hasSavedState(player.getUUID())) {
                StasisMod.LOGGER.info("Player {} rejoined with active saved state - checking recovery status",
                    player.getName().getString());

                // If they're in the overworld, they were probably mid-exit when they crashed
                if (player.level().dimension() != StasisDimensions.SIMULATION_LEVEL) {
                    // They should be at their chamber position based on the state
                    // The state manager will handle full restoration on next chamber interaction
                    StasisMod.LOGGER.info("Player {} state will be used for chamber recovery if needed",
                        player.getName().getString());
                }
            }
        }
    }

    /**
     * SECURITY: Block players from escaping simulation dimension via teleportation.
     * This blocks: Waystones, Draconic Warp, /tp commands, enderpearls, and any other cross-dimensional teleport.
     * EXCEPT: Allow exits to overworld if player has a saved state (legitimate exit via exit beacon).
     */
    @SubscribeEvent
    public static void onEntityTravelDimension(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // If player is in simulation dimension, block exit UNLESS:
        // 1. Teleporting within simulation, OR
        // 2. Exiting to overworld with a saved state (legitimate exit via beacon/command)
        if (player.level().dimension() == StasisDimensions.SIMULATION_LEVEL) {
            if (!event.getDimension().equals(StasisDimensions.SIMULATION_LEVEL)) {
                // Check if this is a legitimate exit (player has saved state)
                if (!PlayerStateManager.hasSavedState(player.getUUID())) {
                    StasisMod.LOGGER.warn("SECURITY: Player {} attempted unauthorized dimension exit via {}",
                        player.getName().getString(), event.getDimension().location());
                    event.setCanceled(true);
                }
                // Allow exit if they have a saved state (legitimate exit sequence)
            }
        }
    }

    /**
     * SECURITY: Force resync block entities after dimension change to prevent client-side spoofing.
     * This prevents block entities from appearing in wrong dimension client-side cache.
     */
    @SubscribeEvent
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Force resync nearby block entities with flag=2 (all properties)
        ChunkPos playerChunk = new ChunkPos(player.blockPosition());
        for (int x = playerChunk.x - 2; x <= playerChunk.x + 2; x++) {
            for (int z = playerChunk.z - 2; z <= playerChunk.z + 2; z++) {
                var chunk = player.level().getChunk(x, z);
                chunk.getBlockEntities().forEach((pos, be) -> {
                    player.level().sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), 2);
                });
            }
        }
    }

    /**
     * Handle Create wrench interactions on Activation Core block.
     * Right-click with wrench = rotate block (horizontal only).
     * Shift+right-click with wrench = pickup block.
     */
    @SubscribeEvent
    public static void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();
        BlockState state = level.getBlockState(event.getPos());

        // Check if block is activation core
        if (state.getBlock() != ChamberBlocks.ACTIVATION_CORE.get()) {
            return;
        }

        // Check if player is holding a wrench
        ItemStack held = player.getItemInHand(event.getHand());
        String itemId = held.getItem().builtInRegistryHolder().key().location().toString();
        if (!itemId.contains("wrench")) {
            return;
        }

        event.setCanceled(true);

        if (player.isShiftKeyDown()) {
            // Shift + wrench = pickup block to inventory
            ItemStack blockItem = new ItemStack(ChamberBlocks.ACTIVATION_CORE_ITEM.get());
            if (!player.getInventory().add(blockItem)) {
                // Inventory full - drop as item
                net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(level, event.getPos().getX() + 0.5, event.getPos().getY() + 0.5, event.getPos().getZ() + 0.5, blockItem);
                level.addFreshEntity(itemEntity);
            }
            level.destroyBlock(event.getPos(), false);
            StasisMod.LOGGER.info("Player {} picked up Activation Core via wrench", player.getName().getString());
        } else {
            // Wrench = rotate block (horizontal only: N, E, S, W)
            Direction currentFacing = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING);
            Direction newFacing = getNextHorizontalFacing(currentFacing);
            level.setBlock(event.getPos(), state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING, newFacing), 3);
            StasisMod.LOGGER.info("Player {} rotated Activation Core to face {}", player.getName().getString(), newFacing);
        }
    }

    private static Direction getNextHorizontalFacing(Direction current) {
        return switch(current) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            case UP, DOWN -> Direction.NORTH; // Default to north if somehow on vertical
        };
    }
}
