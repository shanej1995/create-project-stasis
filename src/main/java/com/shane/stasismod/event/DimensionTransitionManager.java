package com.shane.stasismod.event;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * Manages dimension transition effects (darkness effect + messages) on client side.
 *
 * Phases:
 * 1. Darkness fade in (20 ticks): Messages appear, darkness gradually darkens
 * 2. Overlay fade in (60 ticks): Overlay fades to black on top of darkness
 * 3. Full black (variable): Wait for teleport, stay fully black
 * 4. Overlay fade out (60 ticks): Overlay fades to transparent
 * 5. Darkness fade out (60 ticks): Darkness naturally fades back to normal
 */
public class DimensionTransitionManager {
    private static int elapsedTicks = 0;
    private static boolean isEntering = false;
    private static boolean inTransition = false;
    private static int darknessAppliedTick = 0;  // For overlay calculations
    private static int messageTimingTick = 0;    // Separate counter for message timing
    private static int transitionTimeoutCounter = 0;

    private static final int DARKNESS_FADE_IN = 20;      // Phase 1 (0-20)
    private static final int OVERLAY_FADE_IN = 40;       // Phase 2 (20-60)
    private static final int FULL_BLACK_HOLD = 60;       // Phase 3 (60-120) - before teleport
    private static final int OVERLAY_FADE_OUT = 140;     // Phase 4 - very slow fade on exit
    private static final int FULL_BLACK_BEFORE_FADE = 70; // Hold black for 70 ticks before fade

    /**
     * Detect darkness effect applied and start transition sequence.
     */
    public static void onDarknessApplied() {
        if (inTransition) return;

        inTransition = true;
        elapsedTicks = 0;
        darknessAppliedTick = 0;
        messageTimingTick = 0;
        transitionTimeoutCounter = 0;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.level() != null) {
            // Check if we're in simulation dimension - if so, this is an EXIT
            // If not in simulation, this is an ENTRY
            net.minecraft.resources.ResourceLocation currentDim = minecraft.player.level().dimension().location();
            net.minecraft.resources.ResourceLocation simDim = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(com.shane.stasismod.StasisMod.MODID, "simulation");
            isEntering = !currentDim.equals(simDim);

            // Show first message immediately
            if (isEntering) {
                minecraft.player.displayClientMessage(Component.literal("§6Initializing neural synchronization..."), true);
            } else {
                // For exit: apply darkness effect locally on client since server sync isn't reliable
                minecraft.player.addEffect(new MobEffectInstance(
                    MobEffects.DARKNESS,
                    300,
                    1,
                    false,
                    false
                ));
            }
        }
    }

    /**
     * Called every client tick to update effects and messages.
     */
    public static void tick() {
        if (!inTransition) return;

        Minecraft minecraft = Minecraft.getInstance();
        boolean hasDarkness = minecraft.player != null && minecraft.player.hasEffect(MobEffects.DARKNESS);

        if (hasDarkness) {
            darknessAppliedTick++;
            messageTimingTick++;
        }

        // Enter phase messages - spaced out with long display times
        if (isEntering) {
            // Message 1 already shown in onDarknessApplied() - "Initializing neural synchronization..."

            // Message 2: Mid-sequence (around 40 ticks)
            if (messageTimingTick == 40) {
                if (minecraft.player != null) {
                    minecraft.player.displayClientMessage(Component.literal("§6Suspending biological functions..."), true);
                }
            }

            // Message 3: Late in sequence (around 100 ticks) - final message before teleport
            if (messageTimingTick == 100) {
                if (minecraft.player != null) {
                    minecraft.player.displayClientMessage(Component.literal("§6Consciousness transfer complete."), true);
                }
            }
        }

        // Exit phase messages - show BEFORE and AFTER teleport
        if (!isEntering && messageTimingTick > 0) {
            // BEFORE teleport: Message 1 early in exit sequence (shown when darkness is applied)
            if (messageTimingTick == 5) {
                if (minecraft.player != null) {
                    minecraft.player.displayClientMessage(Component.literal("§6Restoring neural pathways..."), true);
                }
            }

            // AFTER teleport: Message 2 & 3 (after dimension change is detected, well after tick 20 teleport)
            // Message 2: Much later to ensure it's after teleport (at ~60 ticks after darkness applied)
            if (messageTimingTick == 80) {
                if (minecraft.player != null) {
                    minecraft.player.displayClientMessage(Component.literal("§6Biological functions restored."), true);
                }
            }
            // Message 3: Very late in fade back (at ~140 ticks after darkness applied)
            if (messageTimingTick == 150) {
                if (minecraft.player != null) {
                    minecraft.player.displayClientMessage(Component.literal("§6Welcome back."), true);
                }
            }
        }

        // Phase 4: Track exit phase completion
        if (!isEntering && inTransition) {
            transitionTimeoutCounter++;

            // Complete transition when:
            // 1. Darkness effect has completely faded, OR
            // 2. Timeout reached (fallback for when darkness detection fails)
            boolean darknessGone = !hasDarkness;
            boolean timeoutReached = transitionTimeoutCounter > 250;  // ~12 seconds max

            if (darknessGone || timeoutReached) {
                inTransition = false;
                transitionTimeoutCounter = 0;
            }
        }
    }

    /**
     * Mark that teleport just happened (switch to exit phase).
     */
    public static void onTeleported() {
        isEntering = false;
        darknessAppliedTick = 0;  // Reset for overlay calculations in exit phase
        // Keep messageTimingTick for message timing that spans both before and after teleport
        transitionTimeoutCounter = 0;  // Reset timeout for exit phase

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            // Don't reapply darkness - let the current darkness effect fade naturally
            // It will sync from server and fade out on its own
        }
    }

    /**
     * Check if we're currently in a transition.
     */
    public static boolean isInTransition() {
        return inTransition;
    }

    /**
     * Get the GUI overlay alpha for fade effect on top of darkness effect.
     */
    public static float getGuiOverlayAlpha() {
        if (!inTransition) {
            return 0.0f;
        }

        if (isEntering) {
            // ENTERING PHASE: Overlay fades to black AFTER darkness is fully in
            if (darknessAppliedTick < DARKNESS_FADE_IN) {
                // Phase 1: Darkness fading in, overlay hidden
                return 0.0f;
            } else if (darknessAppliedTick < DARKNESS_FADE_IN + OVERLAY_FADE_IN) {
                // Phase 2: Overlay fading to black
                int ticksInPhase = darknessAppliedTick - DARKNESS_FADE_IN;
                float progress = ticksInPhase / (float) OVERLAY_FADE_IN;
                return Math.min(1.0f, progress);
            } else {
                // Phase 3+: Full black
                return 1.0f;
            }
        } else {
            // EXITING PHASE: Stay fully black longer, then fade slowly to transparent
            if (darknessAppliedTick < FULL_BLACK_BEFORE_FADE) {
                // First 40 ticks: Stay fully black
                return 1.0f;
            } else if (darknessAppliedTick < FULL_BLACK_BEFORE_FADE + OVERLAY_FADE_OUT) {
                // Next 100 ticks: Slowly fade from black to transparent
                int ticksInFade = darknessAppliedTick - FULL_BLACK_BEFORE_FADE;
                float progress = ticksInFade / (float) OVERLAY_FADE_OUT;
                return Math.max(0.0f, 1.0f - progress);
            } else {
                // Full transparency once overlay is done
                return 0.0f;
            }
        }
    }

    /**
     * Check if we should render the GUI overlay.
     */
    public static boolean shouldRenderOverlay() {
        return inTransition && getGuiOverlayAlpha() > 0.0f;
    }
}
