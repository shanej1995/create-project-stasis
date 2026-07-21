package com.shane.stasismod.blockentity;

import com.shane.stasismod.StasisMod;
import com.shane.stasismod.dimension.DimensionTeleporter;
import com.shane.stasismod.dimension.StasisDimensions;
import com.shane.stasismod.player.PlayerDataCache;
import com.shane.stasismod.player.PlayerChamberMapping;
import com.shane.stasismod.player.PlayerStateManager;
import com.shane.stasismod.player.PlayerStateSnapshot;
import com.shane.stasismod.plot.PlotData;
import com.shane.stasismod.plot.PlotManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * Handles player exit from the simulation via the exit beacon.
 * Entry is now handled directly in ChamberTile.
 */
public class ChamberLifecycleManager {
    private final ChamberTile chamber;

    public ChamberLifecycleManager(ChamberTile chamber) {
        this.chamber = chamber;
    }

    /**
     * Called when player requests exit via the exit beacon.
     */
    public void onPlayerExit(ServerPlayer simPlayer) {
        try {
            // Restore the player from saved state
            boolean success = PlayerStateManager.restoreFromSimulation(simPlayer);

            if (success) {
                // Verify player is now out of simulation dimension before clearing backup
                if (!simPlayer.level().dimension().equals(StasisDimensions.SIMULATION_LEVEL)) {
                    // Only clear snapshot AFTER we confirm they left the dimension
                    PlayerDataCache.getInstance().removeSnapshot(simPlayer.getUUID());
                    PlayerChamberMapping.getInstance().removeChamber(simPlayer.getUUID());

                    StasisMod.LOGGER.info("Player {} successfully exited simulation", simPlayer.getName().getString());
                } else {
                    // Player still in simulation, preserve snapshot for recovery
                    StasisMod.LOGGER.error("Player {} still in simulation after restore attempt", simPlayer.getName().getString());
                    simPlayer.displayClientMessage(
                        Component.literal("§cWarning: Exit may have failed. Snapshot preserved for recovery."), true);
                }
            } else {
                StasisMod.LOGGER.error("Failed to restore player state for {}", simPlayer.getName().getString());
                simPlayer.displayClientMessage(
                    Component.literal("§cFailed to restore state! Snapshot preserved. Try exiting again."), true);
                // Snapshot is preserved if restore fails - player can retry
            }

        } catch (Exception e) {
            StasisMod.LOGGER.error("Error during player exit", e);
        }
    }

    /**
     * Called if player disconnects while in simulation.
     */
    public void onPlayerDisconnect(ServerPlayer player) {
        StasisMod.LOGGER.info("Player {} disconnected from simulation (snapshot preserved)",
            player.getName().getString());
        // Snapshot remains - player can reconnect and continue
    }
}
