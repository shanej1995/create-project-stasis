package com.shane.stasismod.event;

import com.shane.stasismod.StasisMod;
import com.shane.stasismod.dimension.StasisDimensions;
import com.shane.stasismod.plot.PlotData;
import com.shane.stasismod.plot.PlotManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Generates invisible barrier walls around plot boundaries.
 * Prevents any entity (player or physics object) from leaving the plot area.
 * Barriers are placed at Y=64 and above (above ground level).
 */
@EventBusSubscriber(modid = StasisMod.MODID)
public class PlotBarrierWallGenerator {

    // Track which plots already have barriers to avoid repeated placement
    private static final Set<String> barriersGenerated = new HashSet<>();

    private static final int BARRIER_START_Y = 64;
    private static final int BARRIER_END_Y = 320;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer() == null) {
            return;
        }

        ServerLevel simLevel = event.getServer().getLevel(StasisDimensions.SIMULATION_LEVEL);
        if (simLevel == null) {
            return;
        }

        // Generate barriers for all existing plots
        for (PlotData plot : PlotManager.getInstance().getAllPlots()) {
            String plotKey = plot.getMinX() + "," + plot.getMinZ();

            // Only generate once per plot
            if (!barriersGenerated.contains(plotKey)) {
                generateBarriersForPlot(simLevel, plot);
                barriersGenerated.add(plotKey);
            }
        }
    }

    /**
     * Generates invisible barrier walls around a plot's perimeter.
     */
    private static void generateBarriersForPlot(ServerLevel level, PlotData plot) {
        int minX = plot.getMinX();
        int maxX = plot.getMaxX();
        int minZ = plot.getMinZ();
        int maxZ = plot.getMaxZ();

        // North wall (minZ edge)
        for (int x = minX; x < maxX; x++) {
            for (int y = BARRIER_START_Y; y <= BARRIER_END_Y; y++) {
                BlockPos pos = new BlockPos(x, y, minZ);
                if (level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 2);
                }
            }
        }

        // South wall (maxZ-1 edge, since maxZ is exclusive)
        for (int x = minX; x < maxX; x++) {
            for (int y = BARRIER_START_Y; y <= BARRIER_END_Y; y++) {
                BlockPos pos = new BlockPos(x, y, maxZ - 1);
                if (level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 2);
                }
            }
        }

        // West wall (minX edge)
        for (int z = minZ; z < maxZ; z++) {
            for (int y = BARRIER_START_Y; y <= BARRIER_END_Y; y++) {
                BlockPos pos = new BlockPos(minX, y, z);
                if (level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 2);
                }
            }
        }

        // East wall (maxX-1 edge, since maxX is exclusive)
        for (int z = minZ; z < maxZ; z++) {
            for (int y = BARRIER_START_Y; y <= BARRIER_END_Y; y++) {
                BlockPos pos = new BlockPos(maxX - 1, y, z);
                if (level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 2);
                }
            }
        }

        StasisMod.LOGGER.info("Generated barrier walls for plot at X=[{},{}], Z=[{},{}]",
            minX, maxX, minZ, maxZ);
    }

    /**
     * Clears cached barriers when plots are reset (optional).
     */
    public static void clearBarrierCache() {
        barriersGenerated.clear();
    }
}
