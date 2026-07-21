package com.shane.stasismod.event;

import com.shane.stasismod.StasisMod;
import com.shane.stasismod.dimension.StasisDimensions;
import com.shane.stasismod.plot.PlotData;
import com.shane.stasismod.plot.PlotManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Creates an invisible barrier at plot boundaries.
 * Prevents players from leaving their plot.
 */
@EventBusSubscriber(modid = StasisMod.MODID)
public class PlotBoundaryBarrier {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer() == null) {
            return;
        }

        // Check all players in stasis dimension
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!player.level().dimension().equals(StasisDimensions.SIMULATION_LEVEL)) {
                continue;
            }

            PlotData plot = PlotManager.getInstance().getPlot(player.getUUID());
            if (plot == null) {
                continue;
            }

            int minX = plot.getMinX();
            int maxX = plot.getMaxX();
            int minZ = plot.getMinZ();
            int maxZ = plot.getMaxZ();

            // Check if player is outside bounds
            double playerX = player.getX();
            double playerZ = player.getZ();

            if (playerX < minX || playerX >= maxX || playerZ < minZ || playerZ >= maxZ) {
                // Clamp position to plot bounds
                double clampedX = Math.max(minX + 0.5, Math.min(maxX - 0.5, playerX));
                double clampedZ = Math.max(minZ + 0.5, Math.min(maxZ - 0.5, playerZ));

                player.teleportTo(clampedX, player.getY(), clampedZ);
                player.setDeltaMovement(0, player.getDeltaMovement().y, 0);

                // Show message every 20 ticks (1 second)
                if (player.tickCount % 20 == 0) {
                    player.displayClientMessage(
                        Component.literal("§c✖ Plot boundary - you cannot leave your plot"), true);
                }
            }
        }
    }
}
