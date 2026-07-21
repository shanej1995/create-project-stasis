package com.shane.stasismod.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;

/**
 * Tracks which chamber each player is currently occupying.
 * Avoids expensive block searches during exit.
 */
public class PlayerChamberMapping {
    private static final PlayerChamberMapping INSTANCE = new PlayerChamberMapping();
    private final Map<UUID, BlockPos> playerToChamber = new HashMap<>();

    public static PlayerChamberMapping getInstance() {
        return INSTANCE;
    }

    public void setChamber(UUID playerUUID, BlockPos chamberPos) {
        playerToChamber.put(playerUUID, chamberPos);
    }

    public BlockPos getChamber(UUID playerUUID) {
        return playerToChamber.get(playerUUID);
    }

    public void removeChamber(UUID playerUUID) {
        playerToChamber.remove(playerUUID);
    }
}
