package com.shane.stasismod.plot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for plot allocation system.
 */
public class PlotAllocationTest {
    private PlotManager plotManager;

    @BeforeEach
    public void setUp() {
        plotManager = PlotManager.getInstance();
        // Reset the manager state by creating a new instance
        // In production, this would involve properly clearing the singleton
    }

    @Test
    public void testPlotAllocation() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();

        PlotData plot1 = plotManager.getOrAllocatePlot(player1);
        PlotData plot2 = plotManager.getOrAllocatePlot(player2);

        assertNotNull(plot1, "Plot should be allocated");
        assertNotNull(plot2, "Plot should be allocated");
        assertNotEquals(plot1.getPlotIndex(), plot2.getPlotIndex(),
            "Different players should get different plot indices");
    }

    @Test
    public void testPlotBoundaries() {
        UUID player = UUID.randomUUID();
        PlotData plot = plotManager.getOrAllocatePlot(player);

        // Check that plot size is correct
        int width = plot.getMaxX() - plot.getMinX();
        int depth = plot.getMaxZ() - plot.getMinZ();

        assertEquals(PlotData.PLOT_SIZE, width, "Plot width should be correct");
        assertEquals(PlotData.PLOT_SIZE, depth, "Plot depth should be correct");
    }

    @Test
    public void testCoordinatesWithinBounds() {
        UUID player = UUID.randomUUID();
        PlotData plot = plotManager.getOrAllocatePlot(player);

        int minX = plot.getMinX();
        int maxX = plot.getMaxX();
        int minZ = plot.getMinZ();
        int maxZ = plot.getMaxZ();

        // Test coordinates
        assertTrue(plot.isWithinBounds(minX, minZ), "Min corner should be in bounds");
        assertTrue(plot.isWithinBounds(maxX - 1, maxZ - 1), "Max corner (exclusive) should be in bounds");
        assertFalse(plot.isWithinBounds(maxX, maxZ), "Exclusive max should be out of bounds");
        assertFalse(plot.isWithinBounds(minX - 1, minZ - 1), "Before min should be out of bounds");
    }

    @Test
    public void testSpawnPoint() {
        UUID player = UUID.randomUUID();
        PlotData plot = plotManager.getOrAllocatePlot(player);

        int spawnX = plot.getSpawnPoint().getX();
        int spawnZ = plot.getSpawnPoint().getZ();

        assertTrue(plot.isWithinBounds(spawnX, spawnZ),
            "Spawn point should be within plot bounds");
    }

    @Test
    public void testPlotLookupByCoordinates() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();

        PlotData plot1 = plotManager.getOrAllocatePlot(player1);
        PlotData plot2 = plotManager.getOrAllocatePlot(player2);

        int x1 = plot1.getMinX() + 10;
        int z1 = plot1.getMinZ() + 10;

        PlotData retrieved = plotManager.getPlotAt(x1, z1);
        assertNotNull(retrieved, "Plot should be retrievable by coordinates");
        assertEquals(plot1.getOwnerUUID(), retrieved.getOwnerUUID(),
            "Retrieved plot should match");
    }

    @Test
    public void testPlotSerialization() {
        UUID player = UUID.randomUUID();
        PlotData original = new PlotData(
            player,
            5,
            0,
            System.currentTimeMillis(),
            true,
            "Test Plot"
        );

        // Serialize
        net.minecraft.nbt.CompoundTag tag = original.serializeNBT();

        // Deserialize
        PlotData restored = PlotData.deserializeNBT(tag);

        assertEquals(original.getOwnerUUID(), restored.getOwnerUUID(),
            "UUID should match");
        assertEquals(original.getPlotIndex(), restored.getPlotIndex(),
            "Plot index should match");
        assertEquals(original.getLayer(), restored.getLayer(),
            "Layer should match");
        assertEquals(original.getPlotName(), restored.getPlotName(),
            "Plot name should match");
        assertTrue(restored.hasBeenEntered(), "Entered flag should match");
    }

    @Test
    public void testNoPlotOverlap() {
        // Allocate multiple plots and verify no coordinate overlap
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        UUID player3 = UUID.randomUUID();

        PlotData plot1 = plotManager.getOrAllocatePlot(player1);
        PlotData plot2 = plotManager.getOrAllocatePlot(player2);
        PlotData plot3 = plotManager.getOrAllocatePlot(player3);

        // Check that no plots overlap
        for (int x = plot1.getMinX(); x < plot1.getMaxX(); x++) {
            for (int z = plot1.getMinZ(); z < plot1.getMaxZ(); z++) {
                PlotData atCoord = plotManager.getPlotAt(x, z);
                assertEquals(plot1.getOwnerUUID(), atCoord.getOwnerUUID(),
                    "Coordinate should belong to plot1");
            }
        }
    }
}
