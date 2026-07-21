package com.shane.stasismod.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Utility class for validating and inspecting PlayerStateSnapshot NBT data.
 * Provides methods to verify snapshot integrity and version information.
 */
public class PlayerStateSerializer {

    /**
     * Validates that a CompoundTag contains all required fields for a PlayerStateSnapshot.
     * Returns true if the snapshot is valid and can be deserialized.
     *
     * @param tag The NBT tag to validate
     * @return true if the tag contains all required fields
     */
    public static boolean isValidSnapshot(CompoundTag tag) {
        if (tag == null) {
            return false;
        }

        // Check required fields
        return tag.contains("UUID", Tag.TAG_INT_ARRAY) &&
               tag.contains("Health", Tag.TAG_FLOAT) &&
               tag.contains("FoodLevel", Tag.TAG_INT) &&
               tag.contains("FoodSaturation", Tag.TAG_FLOAT) &&
               tag.contains("FoodExhaustion", Tag.TAG_FLOAT) &&
               tag.contains("XpLevel", Tag.TAG_INT) &&
               tag.contains("XpProgress", Tag.TAG_FLOAT) &&
               tag.contains("GameMode", Tag.TAG_STRING) &&
               tag.contains("PosX", Tag.TAG_DOUBLE) &&
               tag.contains("PosY", Tag.TAG_DOUBLE) &&
               tag.contains("PosZ", Tag.TAG_DOUBLE) &&
               tag.contains("Yaw", Tag.TAG_FLOAT) &&
               tag.contains("Pitch", Tag.TAG_FLOAT) &&
               tag.contains("CapturedAt", Tag.TAG_LONG);
    }

    /**
     * Gets the version of the snapshot format from the NBT tag.
     * Returns -1 if the version field is not present (legacy snapshot without version).
     * Returns the version number if present.
     *
     * @param tag The NBT tag to inspect
     * @return The snapshot version, or -1 if not versioned
     */
    public static int getSnapshotVersion(CompoundTag tag) {
        if (tag == null) {
            return -1;
        }

        if (tag.contains("Version", Tag.TAG_INT)) {
            return tag.getInt("Version");
        }

        // Legacy snapshots without version field
        return -1;
    }

    /**
     * Checks if a snapshot appears to have armor and offhand data saved.
     * This is useful for determining if a snapshot is from the new format or an old format.
     *
     * @param tag The NBT tag to inspect
     * @return true if the snapshot contains armor and offhand data
     */
    public static boolean hasArmorAndOffhandData(CompoundTag tag) {
        if (tag == null) {
            return false;
        }

        return tag.contains("Armor", Tag.TAG_LIST) &&
               tag.contains("Offhand", Tag.TAG_COMPOUND);
    }
}
