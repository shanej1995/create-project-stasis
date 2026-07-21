package com.shane.stasismod.event;

import com.shane.stasismod.StasisMod;
import com.shane.stasismod.dimension.StasisDimensions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Prevents players from breaking barrier blocks in the simulation dimension.
 * Barrier blocks are used for plot boundaries and should not be modifiable.
 */
@EventBusSubscriber(modid = StasisMod.MODID)
public class BarrierBlockProtection {

    @SubscribeEvent
    public static void onBlockBreak(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.getEntity();

        // Only protect in stasis simulation dimension
        if (!player.level().dimension().equals(StasisDimensions.SIMULATION_LEVEL)) {
            return;
        }

        // Check if the block being broken is a barrier block
        if (event.getLevel().getBlockState(event.getPos()).getBlock() == Blocks.BARRIER) {
            event.setCanceled(true);
            player.displayClientMessage(
                Component.literal("§cCannot break plot boundary blocks"), true);
            StasisMod.LOGGER.info("Prevented player {} from breaking barrier block at {}",
                player.getName().getString(), event.getPos());
        }
    }
}
