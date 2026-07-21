package com.shane.stasismod.plot;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Represents a single player plot in the simulation dimension.
 * Plots are 1024x1024 blocks and never change owner.
 * Plot locations are determined by hashing the player's UUID for "random" spacing.
 */
public class PlotData {
    public static final int PLOT_SIZE = 1024;

    private final UUID ownerUUID;
    private final int plotIndex;
    private final int layer;
    private final long createdTimestamp;
    private boolean hasBeenEntered;
    private String plotName;

    // Custom coordinates for UUID-based plots (overrides grid calculation)
    private Integer customMinX = null;
    private Integer customMinZ = null;

    public PlotData(UUID ownerUUID, int plotIndex, int layer, long createdTimestamp, boolean hasBeenEntered, String plotName) {
        this.ownerUUID = ownerUUID;
        this.plotIndex = plotIndex;
        this.layer = layer;
        this.createdTimestamp = createdTimestamp;
        this.hasBeenEntered = hasBeenEntered;
        this.plotName = plotName != null ? plotName : "";
    }

    /**
     * Calculates minimum X coordinate for this plot.
     * If custom coordinates are set (from UUID hashing), use those.
     */
    public int getMinX() {
        if (customMinX != null) {
            return customMinX;
        }
        int gridSize = getGridSize();
        int gridX = plotIndex % gridSize;
        return gridX * PLOT_SIZE - (gridSize * PLOT_SIZE / 2);
    }

    /**
     * Calculates minimum Z coordinate for this plot.
     * If custom coordinates are set (from UUID hashing), use those.
     */
    public int getMinZ() {
        if (customMinZ != null) {
            return customMinZ;
        }
        int gridSize = getGridSize();
        int gridZ = plotIndex / gridSize;
        return gridZ * PLOT_SIZE - (gridSize * PLOT_SIZE / 2);
    }

    /**
     * Sets custom coordinates for this plot (used by UUID hashing).
     */
    public void setCustomCoordinates(int minX, int minZ) {
        this.customMinX = minX;
        this.customMinZ = minZ;
    }

    /**
     * Gets maximum X coordinate (exclusive).
     */
    public int getMaxX() {
        return getMinX() + PLOT_SIZE;
    }

    /**
     * Gets maximum Z coordinate (exclusive).
     */
    public int getMaxZ() {
        return getMinZ() + PLOT_SIZE;
    }

    /**
     * Gets the spawn point at the center of the plot.
     */
    public BlockPos getSpawnPoint() {
        int centerX = (getMinX() + getMaxX()) / 2;
        int centerZ = (getMinZ() + getMaxZ()) / 2;
        return new BlockPos(centerX, 65, centerZ);
    }

    /**
     * Gets spawn point as Vec3 for teleportation.
     */
    public Vec3 getSpawnPointVec3() {
        BlockPos pos = getSpawnPoint();
        return new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }

    /**
     * Checks if a position is within this plot's boundaries.
     */
    public boolean isWithinBounds(int x, int z) {
        return x >= getMinX() && x < getMaxX() && z >= getMinZ() && z < getMaxZ();
    }

    /**
     * Checks if a position is within this plot's boundaries.
     */
    public boolean isWithinBounds(BlockPos pos) {
        return isWithinBounds(pos.getX(), pos.getZ());
    }

    /**
     * Gets grid size for this layer (determines number of plots).
     */
    private int getGridSize() {
        return layer == 0 ? 8 : (layer == 1 ? 16 : 8 * (layer + 1));
    }

    // Getters
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public int getPlotIndex() {
        return plotIndex;
    }

    public int getLayer() {
        return layer;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public boolean hasBeenEntered() {
        return hasBeenEntered;
    }

    public void setHasBeenEntered(boolean entered) {
        this.hasBeenEntered = entered;
    }

    public String getPlotName() {
        return plotName;
    }

    public void setPlotName(String name) {
        this.plotName = name != null ? name : "";
    }

    /**
     * Serializes plot data to NBT.
     */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("uuid", ownerUUID.toString());
        tag.putInt("plot_index", plotIndex);
        tag.putInt("layer", layer);
        tag.putLong("created_time", createdTimestamp);
        tag.putBoolean("entered", hasBeenEntered);
        tag.putString("name", plotName);
        return tag;
    }

    /**
     * Deserializes plot data from NBT.
     */
    public static PlotData deserializeNBT(CompoundTag tag) {
        UUID uuid = UUID.fromString(tag.getString("uuid"));
        int plotIndex = tag.getInt("plot_index");
        int layer = tag.getInt("layer");
        long createdTime = tag.getLong("created_time");
        boolean entered = tag.getBoolean("entered");
        String name = tag.getString("name");

        return new PlotData(uuid, plotIndex, layer, createdTime, entered, name);
    }

    @Override
    public String toString() {
        return String.format("PlotData{uuid=%s, index=%d, layer=%d, entered=%s}",
            ownerUUID.toString().substring(0, 8), plotIndex, layer, hasBeenEntered);
    }
}
