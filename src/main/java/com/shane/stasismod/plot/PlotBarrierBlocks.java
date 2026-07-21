package com.shane.stasismod.plot;

import com.shane.stasismod.StasisMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/**
 * Generates invisible barrier blocks around plot boundaries.
 * Blocks vision and prevents players from seeing beyond their plot.
 */
public class PlotBarrierBlocks {

    /**
     * Generate barrier blocks around a plot's perimeter.
     */
    public static void generateBarriers(ServerLevel level, PlotData plot) {
        int minX = plot.getMinX();
        int maxX = plot.getMaxX();
        int minZ = plot.getMinZ();
        int maxZ = plot.getMaxZ();

        // Generate barriers from Y=0 to Y=256 (full height)
        // North and South walls (along Z axis)
        for (int x = minX - 1; x < maxX + 1; x++) {
            for (int y = 0; y <= 256; y++) {
                // South wall (at minZ)
                BlockPos southPos = new BlockPos(x, y, minZ - 1);
                level.setBlockAndUpdate(southPos, Blocks.BARRIER.defaultBlockState());

                // North wall (at maxZ-1)
                BlockPos northPos = new BlockPos(x, y, maxZ);
                level.setBlockAndUpdate(northPos, Blocks.BARRIER.defaultBlockState());
            }
        }

        // East and West walls (along X axis)
        for (int z = minZ; z < maxZ; z++) {
            for (int y = 0; y <= 256; y++) {
                // West wall (at minX)
                BlockPos westPos = new BlockPos(minX - 1, y, z);
                level.setBlockAndUpdate(westPos, Blocks.BARRIER.defaultBlockState());

                // East wall (at maxX-1)
                BlockPos eastPos = new BlockPos(maxX, y, z);
                level.setBlockAndUpdate(eastPos, Blocks.BARRIER.defaultBlockState());
            }
        }

        StasisMod.LOGGER.info("Generated barrier blocks for plot {}", plot.getPlotIndex());
    }

    /**
     * Clear barrier blocks around a plot (for cleanup/reset).
     */
    public static void clearBarriers(ServerLevel level, PlotData plot) {
        int minX = plot.getMinX();
        int maxX = plot.getMaxX();
        int minZ = plot.getMinZ();
        int maxZ = plot.getMaxZ();

        // Clear north and south walls
        for (int x = minX - 1; x < maxX + 1; x++) {
            for (int y = 0; y <= 256; y++) {
                BlockPos southPos = new BlockPos(x, y, minZ - 1);
                if (level.getBlockState(southPos).getBlock() == Blocks.BARRIER) {
                    level.setBlockAndUpdate(southPos, Blocks.AIR.defaultBlockState());
                }

                BlockPos northPos = new BlockPos(x, y, maxZ);
                if (level.getBlockState(northPos).getBlock() == Blocks.BARRIER) {
                    level.setBlockAndUpdate(northPos, Blocks.AIR.defaultBlockState());
                }
            }
        }

        // Clear east and west walls
        for (int z = minZ; z < maxZ; z++) {
            for (int y = 0; y <= 256; y++) {
                BlockPos westPos = new BlockPos(minX - 1, y, z);
                if (level.getBlockState(westPos).getBlock() == Blocks.BARRIER) {
                    level.setBlockAndUpdate(westPos, Blocks.AIR.defaultBlockState());
                }

                BlockPos eastPos = new BlockPos(maxX, y, z);
                if (level.getBlockState(eastPos).getBlock() == Blocks.BARRIER) {
                    level.setBlockAndUpdate(eastPos, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }
}
