package com.shane.stasismod.plot;

import com.shane.stasismod.StasisMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists plot allocation data to disk.
 * Stores all plot assignments in world/stasis/plots.nbt
 */
public class PlotDataStore {
    private final Path plotsFile;
    private static final int CURRENT_VERSION = 1;

    public PlotDataStore(Path worldDir) {
        this.plotsFile = worldDir.resolve("stasis").resolve("plots.nbt");
        try {
            Files.createDirectories(plotsFile.getParent());
        } catch (IOException e) {
            StasisMod.LOGGER.error("Failed to create plots directory", e);
        }
    }

    /**
     * Saves all plots to disk.
     */
    public void savePlots(List<PlotData> plots) throws IOException {
        CompoundTag root = new CompoundTag();
        CompoundTag data = new CompoundTag();

        data.putInt("version", CURRENT_VERSION);
        data.putLong("created", System.currentTimeMillis());

        ListTag plotList = new ListTag();
        for (PlotData plot : plots) {
            CompoundTag plotTag = plot.serializeNBT();
            plotList.add(plotTag);
        }

        data.put("plots", plotList);
        root.put("PlotData", data);

        try {
            Files.createDirectories(plotsFile.getParent());
            NbtIo.writeCompressed(root, plotsFile);

            StasisMod.LOGGER.debug("Saved {} plots to disk", plots.size());
        } catch (IOException e) {
            StasisMod.LOGGER.error("Failed to save plots to disk", e);
            throw e;
        }
    }

    /**
     * Loads all plots from disk.
     */
    public List<PlotData> loadPlots() throws IOException {
        if (!Files.exists(plotsFile)) {
            StasisMod.LOGGER.info("No plots file found, starting fresh");
            return new ArrayList<>();
        }

        try {
            CompoundTag root = NbtIo.readCompressed(plotsFile, new net.minecraft.nbt.NbtAccounter(Long.MAX_VALUE, 512));
            CompoundTag plotData = root.getCompound("PlotData");

            int version = plotData.getInt("version");
            if (version > CURRENT_VERSION) {
                StasisMod.LOGGER.warn("Plot data version {} is newer than current {}", version, CURRENT_VERSION);
            }

            List<PlotData> plots = new ArrayList<>();
            ListTag plotList = plotData.getList("plots", Tag.TAG_COMPOUND);

            for (int i = 0; i < plotList.size(); i++) {
                CompoundTag plotTag = plotList.getCompound(i);
                try {
                    PlotData plot = PlotData.deserializeNBT(plotTag);
                    plots.add(plot);
                } catch (Exception e) {
                    StasisMod.LOGGER.error("Failed to deserialize plot {}", i, e);
                }
            }

            StasisMod.LOGGER.info("Loaded {} plots from disk", plots.size());
            return plots;
        } catch (IOException e) {
            StasisMod.LOGGER.error("Failed to load plots from disk", e);
            throw e;
        }
    }

    /**
     * Checks if plots file exists.
     */
    public boolean exists() {
        return Files.exists(plotsFile);
    }
}
