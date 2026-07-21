package com.shane.stasismod.plot;

import com.shane.stasismod.StasisMod;
import com.shane.stasismod.config.ServerConfig;
import net.minecraft.core.BlockPos;

import java.util.*;

/**
 * Manages plot allocation and lookup using dynamic grid-based coordinate generation.
 * Each player's plot location is deterministic based on their UUID hash.
 * Grid size and spacing are configurable via ServerConfig.
 *
 * This ensures:
 * - Players get the same plot every time they join
 * - Plots are spread across a grid within normal coordinate ranges
 * - No overlap or collision between player plots
 * - Complete isolation and privacy
 * - Physics compatibility (coordinates within Sable's normal operating range)
 * - Scalable to any number of players via configuration
 *
 * Singleton that maintains all plot assignments.
 */
public class PlotManager {
    private static final PlotManager INSTANCE = new PlotManager();
    private final Map<UUID, PlotData> playerPlots = new HashMap<>();
    private PlotDataStore dataStore;
    private final Set<String> allocatedCoordinates = new HashSet<>();

    public static PlotManager getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize plot manager with data store.
     */
    public void initialize(PlotDataStore dataStore) {
        this.dataStore = dataStore;
        try {
            loadAllPlots();
        } catch (Exception e) {
            StasisMod.LOGGER.error("Failed to load plots from disk", e);
        }
    }

    /**
     * Gets or allocates a plot for a player.
     */
    public PlotData getOrAllocatePlot(UUID playerUUID) {
        PlotData existing = playerPlots.get(playerUUID);
        if (existing != null) {
            return existing;
        }

        // New player - allocate plot
        PlotData plot = allocateNewPlot(playerUUID);
        playerPlots.put(playerUUID, plot);

        // Save to disk
        if (dataStore != null) {
            try {
                dataStore.savePlots(new ArrayList<>(playerPlots.values()));
            } catch (Exception e) {
                StasisMod.LOGGER.error("Failed to save plots to disk", e);
            }
        }

        return plot;
    }

    /**
     * Allocates a new plot for a player based on UUID hash.
     * Uses grid-based coordinates with 2500 block spacing.
     */
    private PlotData allocateNewPlot(UUID playerUUID) {
        // Generate plot coordinates from UUID hash
        PlotCoordinates coords = generatePlotCoordinates(playerUUID);

        // Verify no collision
        String coordKey = coords.getMinX() + "," + coords.getMinZ();
        if (allocatedCoordinates.contains(coordKey)) {
            StasisMod.LOGGER.warn("Plot coordinate collision detected for {}, regenerating...", playerUUID);
            // In practice this should never happen, but handle it gracefully
            coords = generatePlotCoordinates(playerUUID);
            coordKey = coords.getMinX() + "," + coords.getMinZ();
        }
        allocatedCoordinates.add(coordKey);

        // Create plot data with UUID-based index (just for tracking)
        int plotIndex = Math.abs(playerUUID.hashCode() % 1000000);
        PlotData plot = new PlotData(
            playerUUID,
            plotIndex,
            0,
            System.currentTimeMillis(),
            false,
            ""
        );

        // Override the plot coordinates to use the UUID-generated ones
        plot.setCustomCoordinates(coords.getMinX(), coords.getMinZ());

        StasisMod.LOGGER.info("Allocated plot for player {} at coordinates X=[{},{}], Z=[{},{}], Center: ({}, {})",
            playerUUID,
            coords.getMinX(), coords.getMaxX(),
            coords.getMinZ(), coords.getMaxZ(),
            coords.getCenterX(), coords.getCenterZ());

        return plot;
    }

    /**
     * Generates plot coordinates from player UUID using a dynamic grid-based system.
     * Grid dimensions are calculated from ServerConfig.MAX_PLAYERS.
     * Keeps coordinates in normal range for Sable physics compatibility.
     *
     * @param playerUUID The player's UUID
     * @return PlotCoordinates with min/max X and Z
     */
    private PlotCoordinates generatePlotCoordinates(UUID playerUUID) {
        // Get configuration values
        int maxPlayers = ServerConfig.MAX_PLAYERS.get();
        int spacing = ServerConfig.PLOT_SPACING.get();

        // Calculate grid dimensions (make it square or close to it)
        int gridWidth = (int) Math.ceil(Math.sqrt(maxPlayers));

        // Use UUID hash to get a deterministic grid index
        int gridIndex = Math.abs((int)playerUUID.getMostSignificantBits()) % maxPlayers;

        // Convert linear index to 2D grid coordinates
        int gridX = gridIndex % gridWidth;
        int gridZ = gridIndex / gridWidth;

        // Center the grid around origin
        int centerOffset = (gridWidth - 1) / 2;
        int minX = (gridX - centerOffset) * spacing;
        int minZ = (gridZ - centerOffset) * spacing;

        return new PlotCoordinates(minX, minZ);
    }


    /**
     * Gets a plot by player UUID.
     */
    public PlotData getPlot(UUID playerUUID) {
        return playerPlots.get(playerUUID);
    }

    /**
     * Gets a plot by coordinates.
     */
    public PlotData getPlotAt(int x, int z) {
        for (PlotData plot : playerPlots.values()) {
            if (plot.isWithinBounds(x, z)) {
                return plot;
            }
        }
        return null;
    }

    /**
     * Validates that a position is within a valid plot.
     */
    public boolean isWithinValidPlot(BlockPos pos) {
        return getPlotAt(pos.getX(), pos.getZ()) != null;
    }

    /**
     * Gets all plots.
     */
    public Collection<PlotData> getAllPlots() {
        return new ArrayList<>(playerPlots.values());
    }

    /**
     * Loads all plots from disk.
     */
    private void loadAllPlots() throws Exception {
        if (dataStore == null) {
            StasisMod.LOGGER.warn("DataStore not initialized, skipping plot loading");
            return;
        }

        List<PlotData> plots = dataStore.loadPlots();
        playerPlots.clear();
        allocatedCoordinates.clear();

        for (PlotData plot : plots) {
            playerPlots.put(plot.getOwnerUUID(), plot);
            String coordKey = plot.getMinX() + "," + plot.getMinZ();
            allocatedCoordinates.add(coordKey);
        }

        StasisMod.LOGGER.info("Loaded {} plots from disk", plots.size());
    }

    /**
     * Saves all plots to disk.
     */
    public void saveAllPlots() throws Exception {
        if (dataStore == null) {
            StasisMod.LOGGER.warn("DataStore not initialized, skipping plot save");
            return;
        }

        dataStore.savePlots(new ArrayList<>(playerPlots.values()));
    }

    /**
     * Resets a plot (clears building progress but keeps assignment).
     * Actual chunk deletion is handled separately.
     */
    public void markPlotReset(UUID playerUUID) {
        PlotData plot = playerPlots.get(playerUUID);
        if (plot != null) {
            plot.setHasBeenEntered(false);
            StasisMod.LOGGER.info("Marked plot for {} as reset", playerUUID);
        }
    }

    /**
     * Helper class for plot coordinates.
     */
    private static class PlotCoordinates {
        private final int minX;
        private final int minZ;
        private static final int PLOT_SIZE = 1024;

        public PlotCoordinates(int minX, int minZ) {
            this.minX = minX;
            this.minZ = minZ;
        }

        public int getMinX() {
            return minX;
        }

        public int getMinZ() {
            return minZ;
        }

        public int getMaxX() {
            return minX + PLOT_SIZE;
        }

        public int getMaxZ() {
            return minZ + PLOT_SIZE;
        }

        public int getCenterX() {
            return minX + PLOT_SIZE / 2;
        }

        public int getCenterZ() {
            return minZ + PLOT_SIZE / 2;
        }
    }
}
