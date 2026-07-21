package com.shane.stasismod.event;

import com.shane.stasismod.StasisMod;
import com.shane.stasismod.dimension.SimulationDimensionEffects;
import com.shane.stasismod.dimension.StasisDimensions;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Client-side event handlers for the Stasis Mod.
 */
@EventBusSubscriber(modid = StasisMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
    private static ResourceLocation lastDimension = null;
    private static boolean hadDarkness = false;

    /**
     * Register custom dimension effects for the simulation dimension.
     * This makes the sky always appear as if it's noon (constant daylight).
     */
    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        ResourceLocation simDim = ResourceLocation.fromNamespaceAndPath(StasisMod.MODID, "simulation");
        event.register(simDim, SimulationDimensionEffects.INSTANCE);
        StasisMod.LOGGER.info("Registered custom dimension effects for simulation dimension: {}", simDim);
    }


    /**
     * Handle dimension change with messages (legacy - now handled by transition manager).
     */
    private static void handleDimensionChange(ResourceLocation from, ResourceLocation to) {
        // Transition handling is now done entirely by DimensionTransitionManager
        // based on darkness effect detection and onTeleported() calls
    }

    /**
     * Render the black fade overlay on top of the screen.
     * Also detect dimension changes and trigger transition effects.
     */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        // Update fade and messages every frame
        DimensionTransitionManager.tick();

        // Detect darkness effect being applied (from server for fade effect)
        if (minecraft.player != null) {
            boolean hasDarkness = minecraft.player.hasEffect(net.minecraft.world.effect.MobEffects.DARKNESS);

            // If darkness just appeared, start the transition sequence
            if (hasDarkness && !hadDarkness) {
                DimensionTransitionManager.onDarknessApplied();
            }
            hadDarkness = hasDarkness;
        }

        // Detect dimension change (indicates teleport just happened)
        if (minecraft.player != null && minecraft.player.level() != null) {
            ResourceLocation currentDim = minecraft.player.level().dimension().location();

            if (lastDimension != null && !lastDimension.equals(currentDim)) {
                // Teleport just happened - signal the transition manager
                DimensionTransitionManager.onTeleported();
            }
            lastDimension = currentDim;
        }

        // Render GUI overlay on top of darkness effect
        if (!DimensionTransitionManager.shouldRenderOverlay()) {
            return;
        }

        float alpha = DimensionTransitionManager.getGuiOverlayAlpha();
        if (alpha <= 0.0f) {
            return;
        }

        // Draw black overlay on top of darkness effect for full black transition
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, alpha);

        // Fill screen with black
        event.getGuiGraphics().fill(0, 0, screenWidth, screenHeight, (int)(255 * alpha) << 24);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
