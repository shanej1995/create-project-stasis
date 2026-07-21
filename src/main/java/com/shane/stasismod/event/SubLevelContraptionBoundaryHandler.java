package com.shane.stasismod.event;

import com.shane.stasismod.StasisMod;
import com.shane.stasismod.dimension.StasisDimensions;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * Handles sublevel contraption positioning and boundary enforcement.
 * Uses the sublevel's bounding box which accounts for movement.
 */
// Disabled - Sable handles sublevel boundary enforcement
// @EventBusSubscriber(modid = StasisMod.MODID)
public class SubLevelContraptionBoundaryHandler {

    /**
     * Monitor sublevels for movement
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer() == null) {
            return;
        }

        try {
            ServerLevel simLevel = event.getServer().getLevel(StasisDimensions.SIMULATION_LEVEL);
            if (simLevel == null) {
                return;
            }

            ServerSubLevelContainer container = ServerSubLevelContainer.getContainer(simLevel);
            if (container == null) {
                return;
            }

            java.lang.reflect.Method getAllMethod = container.getClass().getMethod("getAllSubLevels");
            java.lang.Iterable<?> subLevels = (java.lang.Iterable<?>) getAllMethod.invoke(container);

            for (Object subLevel : subLevels) {
                // Just monitor - don't interfere with movement
            }
        } catch (Exception e) {
            // Error monitoring sublevels - ignore
        }
    }

    /**
     * Validates player interactions with sublevel contraptions.
     * Checks the sublevel's bounding box which accounts for movement.
     */
    @SubscribeEvent
    public static void onPlayerInteractWithBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ServerPlayer)) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.getEntity();

        if (!player.level().dimension().equals(StasisDimensions.SIMULATION_LEVEL)) {
            return;
        }

        try {
            ServerLevel simLevel = (ServerLevel) player.level();
            ServerSubLevelContainer container = ServerSubLevelContainer.getContainer(simLevel);

            if (container == null) {
                return;
            }

            BlockPos blockPos = event.getPos();
            boolean isWithinAnySubLevel = false;

            java.lang.reflect.Method getAllMethod = container.getClass().getMethod("getAllSubLevels");
            java.lang.Iterable<?> subLevels = (java.lang.Iterable<?>) getAllMethod.invoke(container);

            for (Object subLevel : subLevels) {
                // Use the sublevel's bounding box which accounts for current position/movement
                java.lang.reflect.Method getBoundsMethod = subLevel.getClass().getMethod("boundingBox");
                Object bounds = getBoundsMethod.invoke(subLevel);

                if (bounds != null) {
                    java.lang.reflect.Method containsMethod = bounds.getClass().getMethod("contains", double.class, double.class, double.class);
                    boolean isContained = (boolean) containsMethod.invoke(bounds, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
                    if (isContained) {
                        isWithinAnySubLevel = true;
                        break;
                    }
                }
            }

            // If there are sublevels and block isn't in any, block interaction
            if (!isWithinAnySubLevel) {
                java.lang.reflect.Method countMethod = container.getClass().getMethod("getLoadedCount");
                int count = (int) countMethod.invoke(container);
                if (count > 0) {
                    event.setCanceled(true);
                    player.displayClientMessage(
                        Component.literal("§cThis contraption has drifted outside its bounds!"),
                        true);
                }
            }
        } catch (Exception e) {
            // Sable not available - allow interaction
        }
    }

    /**
     * Prevents players from breaking contraption blocks outside sublevels.
     */
    @SubscribeEvent
    public static void onPlayerBreakBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ServerPlayer)) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.getEntity();

        if (!player.level().dimension().equals(StasisDimensions.SIMULATION_LEVEL)) {
            return;
        }

        try {
            ServerLevel simLevel = (ServerLevel) player.level();
            ServerSubLevelContainer container = ServerSubLevelContainer.getContainer(simLevel);

            if (container == null) {
                return;
            }

            BlockPos blockPos = event.getPos();
            boolean isWithinAnySubLevel = false;

            java.lang.reflect.Method getAllMethod = container.getClass().getMethod("getAllSubLevels");
            java.lang.Iterable<?> subLevels = (java.lang.Iterable<?>) getAllMethod.invoke(container);

            for (Object subLevel : subLevels) {
                // Use the sublevel's bounding box which accounts for current position/movement
                java.lang.reflect.Method getBoundsMethod = subLevel.getClass().getMethod("boundingBox");
                Object bounds = getBoundsMethod.invoke(subLevel);

                if (bounds != null) {
                    java.lang.reflect.Method containsMethod = bounds.getClass().getMethod("contains", double.class, double.class, double.class);
                    boolean isContained = (boolean) containsMethod.invoke(bounds, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
                    if (isContained) {
                        isWithinAnySubLevel = true;
                        break;
                    }
                }
            }

            // If there are sublevels and block isn't in any, prevent breaking
            if (!isWithinAnySubLevel) {
                java.lang.reflect.Method countMethod = container.getClass().getMethod("getLoadedCount");
                int count = (int) countMethod.invoke(container);
                if (count > 0) {
                    event.setCanceled(true);
                    player.displayClientMessage(
                        Component.literal("§cCannot break blocks outside sublevel bounds!"),
                        true);
                }
            }
        } catch (Exception e) {
            // Sable not available - allow breaking
        }
    }

    /**
     * Helper method to get the practical position of a kinematic contraption using reflection.
     */
    public static Vector3dc getPracticalContraptionPosition(Object contraption) {
        try {
            java.lang.reflect.Method getPositionMethod = contraption.getClass().getMethod("sable$getPosition");
            return (Vector3dc) getPositionMethod.invoke(contraption);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the clamped position for a contraption that's drifted outside its plot.
     */
    public static Vector3dc getClampedContraptionPosition(
            Vector3dc pos,
            int minX, int maxX,
            int minZ, int maxZ) {

        double clampedX = Math.max(minX + 0.5, Math.min(maxX - 0.5, pos.x()));
        double clampedZ = Math.max(minZ + 0.5, Math.min(maxZ - 0.5, pos.z()));
        double clampedY = pos.y();

        return new Vector3d(clampedX, clampedY, clampedZ);
    }
}
