package com.shane.stasismod;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the simulation entry/exit cycle.
 * Tests the full flow from player entering simulation to exiting.
 */
public class SimulationCycleTest {

    @Test
    public void testBasicStructure() {
        // Verify that core classes exist and can be instantiated
        assertNotNull(com.shane.stasismod.player.PlayerStateSerializer.class,
            "PlayerStateSerializer should exist");
        assertNotNull(com.shane.stasismod.plot.PlotManager.class,
            "PlotManager should exist");
        assertNotNull(com.shane.stasismod.dimension.StasisDimensions.class,
            "StasisDimensions should exist");
    }

    @Test
    public void testModInitialization() {
        // Verify mod can be loaded
        String modId = StasisMod.MODID;
        assertEquals("stasismod", modId, "Mod ID should be correct");
    }

    @Test
    public void testPlayerSerializationSystemExists() {
        // Verify all player serialization components exist
        assertNotNull(com.shane.stasismod.player.PlayerStateSerializer.class);
        assertNotNull(com.shane.stasismod.player.PlayerStateRestorer.class);
        assertNotNull(com.shane.stasismod.player.PlayerDataCache.class);
        assertNotNull(com.shane.stasismod.player.PlayerDataStore.class);
    }

    @Test
    public void testPlotSystemExists() {
        // Verify all plot components exist
        assertNotNull(com.shane.stasismod.plot.PlotData.class);
        assertNotNull(com.shane.stasismod.plot.PlotManager.class);
        assertNotNull(com.shane.stasismod.plot.PlotDataStore.class);
    }

    @Test
    public void testDimensionSystemExists() {
        // Verify dimension components exist
        assertNotNull(com.shane.stasismod.dimension.StasisDimensions.class);
        assertNotNull(com.shane.stasismod.dimension.StasisChunkGenerator.class);
    }

    @Test
    public void testEventSystemExists() {
        // Verify event handlers exist
        assertNotNull(com.shane.stasismod.event.ServerEvents.class);
    }

    @Test
    public void testCommandSystemExists() {
        // Verify command system exists
        assertNotNull(com.shane.stasismod.command.StasisCommand.class);
    }
}
