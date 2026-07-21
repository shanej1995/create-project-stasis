package com.shane.stasismod.dimension;

import com.shane.stasismod.StasisMod;
import com.shane.stasismod.plot.PlotData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Handles teleportation between overworld and stasis dimension.
 */
public class DimensionTeleporter {

    /**
     * Teleport player to stasis dimension at plot location
     */
    public static boolean teleportToStasis(ServerPlayer player, ServerLevel overworld, PlotData plot) {
        try {
            // Get the stasis dimension
            ServerLevel stasisDim = overworld.getServer().getLevel(StasisDimensions.SIMULATION_LEVEL);
            if (stasisDim == null) {
                StasisMod.LOGGER.error("Stasis dimension not loaded!");
                return false;
            }

            // Calculate spawn position based on plot center
            int centerX = (plot.getMinX() + plot.getMaxX()) / 2;
            int centerZ = (plot.getMinZ() + plot.getMaxZ()) / 2;

            double spawnX = centerX + 0.5;
            double spawnY = 65.0;  // Default height
            double spawnZ = centerZ + 0.5;

            Vec3 spawnPos = new Vec3(spawnX, spawnY, spawnZ);

            StasisMod.LOGGER.info("Teleporting {} to stasis dimension at ({}, {}, {})",
                player.getName().getString(), spawnX, spawnY, spawnZ);

            // Teleport using ServerPlayer.teleportTo
            player.teleportTo(stasisDim, spawnX, spawnY, spawnZ, player.getYRot(), player.getXRot());

            return true;
        } catch (Exception e) {
            StasisMod.LOGGER.error("Failed to teleport player to stasis dimension", e);
            return false;
        }
    }

    /**
     * Teleport player back to overworld from stasis dimension
     */
    public static boolean teleportToOverworld(ServerPlayer player, ServerLevel stasisDim, Vec3 returnPos) {
        try {
            // Get the overworld
            ServerLevel overworld = stasisDim.getServer().overworld();

            StasisMod.LOGGER.info("Teleporting {} back to overworld at ({}, {}, {})",
                player.getName().getString(), returnPos.x, returnPos.y, returnPos.z);

            // Teleport back to original position
            player.teleportTo(overworld, returnPos.x, returnPos.y, returnPos.z, player.getYRot(), player.getXRot());

            return true;
        } catch (Exception e) {
            StasisMod.LOGGER.error("Failed to teleport player back to overworld", e);
            return false;
        }
    }
}
