package com.shane.stasismod.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for player state serialization and restoration.
 */
public class PlayerSerializationTest {

    @Test
    public void testSnapshotValidation() {
        // Test valid snapshot
        CompoundTag validSnapshot = createValidSnapshot();
        assertTrue(PlayerStateSerializer.isValidSnapshot(validSnapshot),
            "Valid snapshot should pass validation");

        // Test invalid snapshot (missing root)
        CompoundTag invalidSnapshot = new CompoundTag();
        assertFalse(PlayerStateSerializer.isValidSnapshot(invalidSnapshot),
            "Invalid snapshot should fail validation");

        // Test invalid snapshot (wrong version)
        CompoundTag wrongVersionSnapshot = new CompoundTag();
        CompoundTag stasisRoot = new CompoundTag();
        stasisRoot.putInt("version", 99);
        wrongVersionSnapshot.put("StasisPlayerSnapshot", stasisRoot);
        assertFalse(PlayerStateSerializer.isValidSnapshot(wrongVersionSnapshot),
            "Wrong version snapshot should fail validation");
    }

    @Test
    public void testSnapshotVersionDetection() {
        CompoundTag snapshot = createValidSnapshot();
        assertEquals(1, PlayerStateSerializer.getSnapshotVersion(snapshot),
            "Should detect correct version");

        CompoundTag invalidSnapshot = new CompoundTag();
        assertEquals(-1, PlayerStateSerializer.getSnapshotVersion(invalidSnapshot),
            "Should return -1 for missing version");
    }

    private CompoundTag createValidSnapshot() {
        CompoundTag root = new CompoundTag();
        CompoundTag stasisRoot = new CompoundTag();

        stasisRoot.putInt("version", 1);
        stasisRoot.putLong("timestamp", System.currentTimeMillis());
        stasisRoot.putString("player_uuid", "12345678-1234-1234-1234-123456789012");
        stasisRoot.putString("player_name", "TestPlayer");

        // Add minimal player data
        CompoundTag playerData = new CompoundTag();
        playerData.put("Pos", new net.minecraft.nbt.ListTag());
        playerData.put("Rotation", new net.minecraft.nbt.ListTag());
        stasisRoot.put("player_data", playerData);

        CompoundTag metadata = new CompoundTag();
        stasisRoot.put("metadata", metadata);

        root.put("StasisPlayerSnapshot", stasisRoot);
        return root;
    }
}
