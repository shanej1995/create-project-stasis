package com.shane.stasismod.blockentity;

import com.shane.stasismod.config.ConfigCache;
import net.neoforged.neoforge.energy.EnergyStorage;

/**
 * FE (Forge Energy) capability handler for the chamber.
 * Manages power input and drain for chamber operations.
 *
 * Values are configurable in stasismod-common.toml:
 * - chamber.capacity: 100000
 * - chamber.activationCost: 2000
 * - chamber.maxTransfer: 500
 */
public class ChamberEnergyHandler extends EnergyStorage {

    public ChamberEnergyHandler() {
        // Use cached config values (populated when config loads)
        super(
            ConfigCache.getChamberCapacity(),
            ConfigCache.getMaxTransferRate(),
            ConfigCache.getMaxTransferRate(),
            0
        );
    }

    /**
     * Adds energy to the chamber (from power input).
     */
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        return received;
    }

    /**
     * Extracts energy from the chamber (internal use only).
     */
    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        // Don't allow external extraction from the chamber
        return 0;
    }

    /**
     * Check if activation is possible (enough power stored).
     */
    public boolean canActivate() {
        return this.energy >= ConfigCache.getActivationCost();
    }

    /**
     * Consume activation cost.
     */
    public void consumeActivation() {
        this.energy -= ConfigCache.getActivationCost();
    }

    /**
     * Get activation cost for display.
     */
    public int getActivationCost() {
        return ConfigCache.getActivationCost();
    }


    /**
     * Get current energy level.
     */
    public int getEnergy() {
        return this.energy;
    }

    /**
     * Get max capacity.
     */
    public int getMaxEnergy() {
        return this.capacity;
    }

    /**
     * Set energy level (for loading from NBT).
     */
    public void setEnergy(int amount) {
        this.energy = Math.min(amount, this.capacity);
    }
}
