package com.shane.stasismod.event;

import com.shane.stasismod.StasisMod;
import com.shane.stasismod.dimension.StasisDimensions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Phase 2 Security: Mod-specific exploit blocking.
 * Prevents dangerous mod blocks/items from being used in simulation dimension.
 */
@EventBusSubscriber(modid = StasisMod.MODID)
public class ModSpecificBlockingHandler {

    /**
     * SECURITY: Block Waystone placement in simulation dimension.
     * Prevents creating waypoints that could be used for dimensional travel.
     */
    @SubscribeEvent
    public static void onBlockPlace(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Check if player is in simulation dimension
        if (player.level().dimension() != StasisDimensions.SIMULATION_LEVEL) {
            return;
        }

        // Get the block being placed from player's hand
        String blockId = player.getMainHandItem().getItem()
                .builtInRegistryHolder().key().location().toString();

        // Block dangerous mod blocks
        if (isBlockingItem(blockId)) {
            event.setCanceled(true);
            player.displayClientMessage(
                    Component.literal("§c" + getBlockingMessage(blockId)),
                    true);
            StasisMod.LOGGER.warn("SECURITY: Player {} attempted to place {} in simulation dimension",
                    player.getName().getString(), blockId);
        }
    }

    /**
     * Check if an item/block should be blocked in simulation dimension.
     */
    private static boolean isBlockingItem(String itemId) {
        return itemId.equals("waystones:waystone") ||
                itemId.equals("waystones:waystone_rusty") ||
                itemId.equals("waystones:waystone_sandy") ||
                itemId.equals("waystones:waystone_mossy") ||
                itemId.equals("waystones:waystone_deepslate") ||
                itemId.equals("ftbchunks:chunk_loader") ||
                itemId.equals("chunkloaders:chunk_loader") ||
                itemId.equals("immersiveportals:end_frame");
    }

    /**
     * Get user-friendly blocking message.
     */
    private static String getBlockingMessage(String itemId) {
        if (itemId.contains("waystone")) {
            return "Waystones cannot be placed in the simulation dimension";
        } else if (itemId.contains("chunk_loader")) {
            return "Chunk loaders cannot operate in the simulation dimension";
        } else if (itemId.contains("end_frame")) {
            return "Dimensional portals cannot be created in the simulation dimension";
        }
        return "This item cannot be used in the simulation dimension";
    }
}
