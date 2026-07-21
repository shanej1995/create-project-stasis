package com.shane.stasismod.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Immutable snapshot of player state for restoration.
 * Uses direct field copying, not NBT serialization.
 * Works with all mods including Curios.
 */
public class PlayerStateSnapshot {
    public final UUID playerUUID;

    // Inventory
    public final ItemStack[] mainInventory;
    public final ItemStack[] armor;
    public final ItemStack offhand;

    // Health and hunger
    public final float health;
    public final int foodLevel;
    public final float foodSaturation;
    public final float foodExhaustion;

    // XP
    public final int xpLevel;
    public final float xpProgress;

    // Effects
    public final List<MobEffectInstance> activeEffects;

    // Game state
    public final GameType gameMode;

    // Position and rotation
    public final Vec3 position;
    public final float yaw;
    public final float pitch;

    // Timestamp for debugging
    public final long capturedAt;

    public PlayerStateSnapshot(
            UUID playerUUID,
            ItemStack[] mainInventory,
            ItemStack[] armor,
            ItemStack offhand,
            float health,
            int foodLevel,
            float foodSaturation,
            float foodExhaustion,
            int xpLevel,
            float xpProgress,
            List<MobEffectInstance> activeEffects,
            GameType gameMode,
            Vec3 position,
            float yaw,
            float pitch) {
        this.playerUUID = playerUUID;
        this.mainInventory = mainInventory;
        this.armor = armor;
        this.offhand = offhand;
        this.health = health;
        this.foodLevel = foodLevel;
        this.foodSaturation = foodSaturation;
        this.foodExhaustion = foodExhaustion;
        this.xpLevel = xpLevel;
        this.xpProgress = xpProgress;
        this.activeEffects = new ArrayList<>(activeEffects);
        this.gameMode = gameMode;
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
        this.capturedAt = System.currentTimeMillis();
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("UUID", playerUUID);
        tag.putFloat("Health", health);
        tag.putInt("FoodLevel", foodLevel);
        tag.putFloat("FoodSaturation", foodSaturation);
        tag.putFloat("FoodExhaustion", foodExhaustion);
        tag.putInt("XpLevel", xpLevel);
        tag.putFloat("XpProgress", xpProgress);
        tag.putString("GameMode", gameMode.getName());
        tag.putDouble("PosX", position.x);
        tag.putDouble("PosY", position.y);
        tag.putDouble("PosZ", position.z);
        tag.putFloat("Yaw", yaw);
        tag.putFloat("Pitch", pitch);
        tag.putLong("CapturedAt", capturedAt);
        // NOTE: Armor and offhand are restored from the full NBT backup (Curios backup)
        // This snapshot is just a lightweight in-memory representation

        return tag;
    }

    public static PlayerStateSnapshot deserialize(CompoundTag tag) {
        try {
            // Validate snapshot before attempting deserialization
            if (!PlayerStateSerializer.isValidSnapshot(tag)) {
                return null;
            }

            UUID playerUUID = tag.getUUID("UUID");
            float health = tag.getFloat("Health");
            int foodLevel = tag.getInt("FoodLevel");
            float foodSaturation = tag.getFloat("FoodSaturation");
            float foodExhaustion = tag.getFloat("FoodExhaustion");
            int xpLevel = tag.getInt("XpLevel");
            float xpProgress = tag.getFloat("XpProgress");
            GameType gameMode = GameType.byName(tag.getString("GameMode"), GameType.SURVIVAL);
            Vec3 position = new Vec3(
                    tag.getDouble("PosX"),
                    tag.getDouble("PosY"),
                    tag.getDouble("PosZ")
            );
            float yaw = tag.getFloat("Yaw");
            float pitch = tag.getFloat("Pitch");

            // Inventory will be restored from Curios backup NBT which has full player data
            ItemStack[] inventory = new ItemStack[36];
            for (int i = 0; i < 36; i++) {
                inventory[i] = ItemStack.EMPTY;
            }

            // Armor and offhand are restored from the full NBT backup (Curios backup)
            // Initialize as empty here - they'll be restored during prepareForSimulation
            ItemStack[] armorSlots = new ItemStack[4];
            for (int i = 0; i < 4; i++) {
                armorSlots[i] = ItemStack.EMPTY;
            }

            ItemStack offhandStack = ItemStack.EMPTY;

            // Effects not persisted
            List<MobEffectInstance> effects = new ArrayList<>();

            return new PlayerStateSnapshot(
                    playerUUID,
                    inventory,
                    armorSlots,
                    offhandStack,
                    health,
                    foodLevel,
                    foodSaturation,
                    foodExhaustion,
                    xpLevel,
                    xpProgress,
                    effects,
                    gameMode,
                    position,
                    yaw,
                    pitch
            );
        } catch (Exception e) {
            return null;
        }
    }
}
