package com.shane.stasismod.plot;

import com.shane.stasismod.dimension.StasisDimensions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Renders plot borders using particles.
 * Shows players the boundaries of their plot.
 */
public class PlotBorderRenderer {

    /**
     * Renders border particles for a player's plot.
     * Called periodically while player is in the stasis dimension.
     */
    public static void renderPlotBorder(ServerPlayer player, PlotData plot) {
        Level level = player.level();
        if (!level.dimension().equals(StasisDimensions.SIMULATION_LEVEL)) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        int minX = plot.getMinX();
        int maxX = plot.getMaxX();
        int minZ = plot.getMinZ();
        int maxZ = plot.getMaxZ();
        int borderY = 65;

        // Render border lines using glow particles with denser spacing
        // North edge (positive Z)
        renderLine(serverLevel, minX, borderY, maxZ - 1, maxX, borderY, maxZ - 1, 1);
        // South edge (negative Z)
        renderLine(serverLevel, minX, borderY, minZ, maxX, borderY, minZ, 1);
        // East edge (positive X)
        renderLine(serverLevel, maxX - 1, borderY, minZ, maxX - 1, borderY, maxZ, 1);
        // West edge (negative X)
        renderLine(serverLevel, minX, borderY, minZ, minX, borderY, maxZ, 1);
    }

    /**
     * Renders a line of particles from start to end coordinates.
     */
    private static void renderLine(ServerLevel level, int x1, int y, int z1, int x2, int y2, int z2, int spacing) {
        double dx = x2 - x1;
        double dy = y2 - y;
        double dz = z2 - z1;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance < 1) {
            return;
        }

        int steps = (int) (distance / spacing);
        for (int i = 0; i <= steps; i++) {
            double progress = steps > 0 ? (double) i / steps : 0;
            double px = x1 + dx * progress + 0.5;
            double py = y + dy * progress + 0.5;
            double pz = z1 + dz * progress + 0.5;

            level.sendParticles(ParticleTypes.GLOW, px, py, pz, 1, 0, 0, 0, 0.1);
        }
    }

    /**
     * Renders all 4 vertical corners of the plot.
     */
    public static void renderPlotCorners(ServerLevel level, PlotData plot) {
        int minX = plot.getMinX();
        int maxX = plot.getMaxX();
        int minZ = plot.getMinZ();
        int maxZ = plot.getMaxZ();

        // Render vertical lines at corners
        renderVerticalLine(level, minX, minZ, 1);
        renderVerticalLine(level, maxX - 1, minZ, 1);
        renderVerticalLine(level, minX, maxZ - 1, 1);
        renderVerticalLine(level, maxX - 1, maxZ - 1, 1);
    }

    /**
     * Renders a vertical line of particles.
     */
    private static void renderVerticalLine(ServerLevel level, int x, int z, int spacing) {
        for (int y = 0; y <= 63; y += spacing) {
            level.sendParticles(ParticleTypes.END_ROD, x + 0.5, y + 0.5, z + 0.5, 1, 0, 0, 0, 0.05);
        }
    }
}
