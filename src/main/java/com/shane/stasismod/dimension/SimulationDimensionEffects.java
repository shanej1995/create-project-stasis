package com.shane.stasismod.dimension;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

/**
 * Custom dimension effects for the Stasis simulation dimension.
 * Makes the sky always appear as if it's noon (constant daylight).
 */
public class SimulationDimensionEffects extends DimensionSpecialEffects {
    public static final SimulationDimensionEffects INSTANCE = new SimulationDimensionEffects();

    public SimulationDimensionEffects() {
        super(Float.NaN, true, DimensionSpecialEffects.SkyType.NORMAL, false, false);
    }

    /**
     * Calculate if position has fog (always false for simulation dimension).
     */
    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }

    /**
     * Get the fog color based on brightness (keep bright daylight).
     */
    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 cameraPos, float brightness) {
        return new Vec3(0.7, 0.8, 1.0);  // Blue sky color at noon
    }
}
