package com.shane.stasismod.item;

import com.shane.stasismod.StasisMod;
import com.shane.stasismod.dimension.StasisDimensions;
import com.shane.stasismod.player.PlayerStateManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

/**
 * Item given to players entering the simulation dimension.
 * Right-clicking initiates the exit sequence back to the overworld.
 */
public class ExitBeacon extends Item {
    public ExitBeacon(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Only work on server side
        if (level.isClientSide) {
            return InteractionResultHolder.pass(itemStack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(itemStack);
        }

        // Check if player is in simulation dimension
        if (serverPlayer.level().dimension() != StasisDimensions.SIMULATION_LEVEL) {
            return InteractionResultHolder.fail(itemStack);
        }

        // Check if player has a saved state (entered sim via core)
        if (!PlayerStateManager.hasSavedState(player.getUUID())) {
            return InteractionResultHolder.fail(itemStack);
        }

        // Apply darkness effect for transition
        serverPlayer.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.DARKNESS,
            20,
            0,
            false,
            false
        ));

        // Restore player to saved state (position, inventory, effects, etc.)
        PlayerStateManager.restoreFromSimulation(serverPlayer);

        return InteractionResultHolder.success(itemStack);
    }
}
