package com.shane.stasismod.dimension;

import com.shane.stasismod.StasisMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers the Stasis Simulation dimension.
 *
 * Note: Dimension registration is deferred to world loading via DimensionDataPackProvider.
 * This allows us to use the world generation settings from datapacks/world data.
 *
 * Phase 1: Register dimension resource key only
 * Phase 2: Custom dimension type with fixed time and no spawning
 */
public class StasisDimensions {
    // Will be registered via datapack when server starts
    public static final DeferredRegister<DimensionType> DIMENSION_TYPES =
        DeferredRegister.create(Registries.DIMENSION_TYPE, StasisMod.MODID);

    // Resource key for accessing the dimension at runtime
    public static final ResourceKey<Level> SIMULATION_LEVEL =
        ResourceKey.create(Registries.DIMENSION, StasisMod.modLoc("simulation"));
}
