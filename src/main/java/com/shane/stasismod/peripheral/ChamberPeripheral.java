package com.shane.stasismod.peripheral;

import com.shane.stasismod.StasisMod;
import com.shane.stasismod.blockentity.ChamberTile;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Peripheral interface for the Stasis Chamber.
 * Allows CC: Tweaked computers to interact with the chamber and start simulations.
 */
public class ChamberPeripheral implements IDynamicPeripheral {
    private static final String TYPE = "stasis_chamber";
    private static final String[] METHODS = {"startSimulation", "getEnergy", "getActivationCost"};

    private final ChamberTile chamber;
    private final Level level;
    private IComputerAccess computerAccess;

    public ChamberPeripheral(ChamberTile chamber, Level level) {
        this.chamber = chamber;
        this.level = level;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String[] getMethodNames() {
        return METHODS;
    }

    @Override
    public void attach(IComputerAccess computer) {
        this.computerAccess = computer;
    }

    @Override
    public void detach(IComputerAccess computer) {
        this.computerAccess = null;
    }

    @Override
    public MethodResult callMethod(IComputerAccess computer, ILuaContext context, int method, IArguments arguments) throws LuaException {
        if (method == 0) { // startSimulation
            return callStartSimulation(computer);
        } else if (method == 1) { // getEnergy
            return callGetEnergy();
        } else if (method == 2) { // getActivationCost
            return MethodResult.of(chamber.getEnergyHandler().getActivationCost());
        }
        throw new LuaException("Unknown method");
    }

    private MethodResult callGetEnergy() throws LuaException {
        try {
            int current = chamber.getEnergy();
            int max = chamber.getMaxEnergy();
            return MethodResult.of(current, max);
        } catch (Exception e) {
            StasisMod.LOGGER.error("Failed to get energy", e);
            return MethodResult.of(0, 0);
        }
    }

    private MethodResult callStartSimulation(IComputerAccess computer) throws LuaException {
        if (level.isClientSide) {
            return MethodResult.of(false, "Cannot activate from client");
        }

        try {
            // Get the player who is running this computer
            // CC: Tweaked doesn't directly expose the player, so we find the closest player
            // who is within range of the computer's position
            var blockPos = chamber.getBlockPos();
            var nearestPlayer = level.getNearestPlayer(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 100.0, false);

            if (!(nearestPlayer instanceof ServerPlayer player)) {
                return MethodResult.of(false, "No player found nearby");
            }

            StasisMod.LOGGER.info("Chamber activation triggered by computer for player {}", player.getName().getString());

            // Activate the chamber for this player
            if (chamber.handleActivation(player)) {
                return MethodResult.of(true, "Simulation started");
            } else {
                // Chamber displays the error message to the player, we just say it failed
                return MethodResult.of(false, "Insufficient power or chamber not ready");
            }
        } catch (Exception e) {
            StasisMod.LOGGER.error("Failed to start simulation", e);
            return MethodResult.of(false, "Error: " + e.getMessage());
        }
    }

    @Override
    public boolean equals(IPeripheral other) {
        return this == other;
    }

    @Override
    public Object getTarget() {
        return chamber;
    }
}
