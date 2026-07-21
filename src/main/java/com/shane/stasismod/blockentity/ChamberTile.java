package com.shane.stasismod.blockentity;

import com.shane.stasismod.config.ConfigCache;
import com.shane.stasismod.StasisMod;
import com.shane.stasismod.player.PlayerStateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Block entity for the Activation Core chamber control block.
 * Simple activation-only device - no state tracking needed.
 * Player state is saved and restored externally.
 */
public class ChamberTile extends BlockEntity implements IEnergyStorage {
    public final ChamberEnergyHandler energyHandler = new ChamberEnergyHandler();
    private final ChamberLifecycleManager lifecycleManager = new ChamberLifecycleManager(this);

    private int teleportDelay = 0;
    private ServerPlayer delayedPlayer = null;
    private com.shane.stasismod.plot.PlotData delayedPlot = null;

    public ChamberTile(BlockPos pos, BlockState state) {
        super(ChamberBlockEntities.CHAMBER_TILE.get(), pos, state);
    }

    public boolean handleActivation(ServerPlayer player) {
        if (!energyHandler.canActivate()) {
            int current = energyHandler.getEnergy();
            int needed = ConfigCache.getActivationCost();
            player.displayClientMessage(
                Component.literal("§cInsufficient power (have " + current + " FE, need " + needed + " FE)"), true);
            return false;
        }

        // Consume energy
        energyHandler.consumeActivation();

        // Prepare player for simulation (save state)
        try {
            if (!PlayerStateManager.prepareForSimulation(player)) {
                StasisMod.LOGGER.error("Failed to prepare player for simulation");
                player.displayClientMessage(
                    Component.literal("§cChamber malfunction - state save failed. Please try again. If this persists, report bug."), true);
                return false;
            }
        } catch (RuntimeException e) {
            StasisMod.LOGGER.error("State save failed during chamber entry: {}", e.getMessage(), e);
            player.displayClientMessage(
                Component.literal("§cChamber malfunction - state save failed. Please try again. If this persists, report bug."), true);
            return false;
        }

        // Verify state was saved
        if (PlayerStateManager.getSavedState(player.getUUID()) == null) {
            StasisMod.LOGGER.error("State save verification failed for player {}", player.getName().getString());
            player.displayClientMessage(
                Component.literal("§cChamber malfunction - state save failed. Please try again. If this persists, report bug."), true);
            return false;
        }

        // Get plot and schedule teleport
        com.shane.stasismod.plot.PlotData plot = com.shane.stasismod.plot.PlotManager.getInstance().getOrAllocatePlot(player.getUUID());
        if (plot == null) {
            StasisMod.LOGGER.error("Failed to allocate plot for {}", player.getName().getString());
            player.displayClientMessage(
                Component.literal("§cChamber malfunction - plot allocation failed. Please try again."), true);
            return false;
        }

        // Schedule delayed teleport (for fade animation)
        delayedPlayer = player;
        delayedPlot = plot;
        teleportDelay = 120;  // 6 seconds

        StasisMod.LOGGER.info("Player {} activation initiated", player.getName().getString());
        markUpdated();
        return true;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ChamberTile be) {
        be.serverTick();
    }

    private void serverTick() {
        if (level == null || level.isClientSide) return;

        // Handle delayed teleports (for fade animation)
        if (delayedPlayer != null && teleportDelay > 0) {
            teleportDelay--;

            // Apply darkness effect at the start
            if (teleportDelay == 119) {
                delayedPlayer.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.DARKNESS,
                    170,
                    1,
                    false,
                    false
                ));
            }

            if (teleportDelay == 0) {
                // Perform the teleport
                ServerLevel overworld = delayedPlayer.serverLevel();
                boolean teleportSuccess = com.shane.stasismod.dimension.DimensionTeleporter.teleportToStasis(delayedPlayer, overworld, delayedPlot);

                if (!teleportSuccess) {
                    StasisMod.LOGGER.error("Failed to teleport player to stasis dimension");
                } else {
                    StasisMod.LOGGER.info("Player {} teleported to simulation", delayedPlayer.getName().getString());
                }

                delayedPlayer = null;
                delayedPlot = null;
                markUpdated();
            }
        }
    }

    public void onRemoved() {
        // No special cleanup needed
    }

    // Getters
    public int getEnergy() { return energyHandler.getEnergy(); }
    public int getMaxEnergy() { return energyHandler.getMaxEnergy(); }

    // Energy handler access
    public ChamberEnergyHandler getEnergyHandler() {
        return energyHandler;
    }

    // IEnergyStorage implementation - delegates to energyHandler
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = energyHandler.receiveEnergy(maxReceive, simulate);
        if (received > 0 && !simulate) {
            markUpdated();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;  // Don't allow external extraction
    }

    @Override
    public int getEnergyStored() {
        return energyHandler.getEnergy();
    }

    @Override
    public int getMaxEnergyStored() {
        return energyHandler.getMaxEnergy();
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    private void markUpdated() {
        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energyHandler.getEnergy());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("energy")) {
            energyHandler.setEnergy(tag.getInt("energy"));
        }
    }

}
